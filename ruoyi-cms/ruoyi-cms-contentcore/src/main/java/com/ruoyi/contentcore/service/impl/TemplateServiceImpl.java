package com.ruoyi.contentcore.service.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileExUtils;
import com.ruoyi.contentcore.ContentCoreConsts;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.CmsTemplate;
import com.ruoyi.contentcore.domain.dto.TemplateAddDTO;
import com.ruoyi.contentcore.domain.dto.TemplateRenameDTO;
import com.ruoyi.contentcore.domain.dto.TemplateUpdateDTO;
import com.ruoyi.contentcore.exception.ContentCoreErrorCode;
import com.ruoyi.contentcore.fixed.config.TemplateSuffix;
import com.ruoyi.contentcore.mapper.CmsTemplateMapper;
import com.ruoyi.contentcore.service.IPublishPipeService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.service.ITemplateService;
import com.ruoyi.contentcore.template.ITemplateType;
import com.ruoyi.contentcore.util.SiteUtils;
import com.ruoyi.system.SysConstants;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl extends ServiceImpl<CmsTemplateMapper, CmsTemplate> implements ITemplateService {

	private final static String TEMPLATE_STATIC_CONTENT_CACHE_KEY_PREFIX = "cms:template:";

	private final Map<String, ITemplateType> templateTypes;

	private final IPublishPipeService publishPipeService;

	private final RedisCache redisCache;

	private final ISiteService siteService;

	@Override
	public String getTemplateStaticContentCache(String templateId) {
		return this.redisCache.getCacheObject(TEMPLATE_STATIC_CONTENT_CACHE_KEY_PREFIX + templateId);
	}

	@Override
	public void setTemplateStaticContentCache(String templateId, String staticContent) {
		this.redisCache.setCacheObject(TEMPLATE_STATIC_CONTENT_CACHE_KEY_PREFIX + templateId, staticContent, 24,
				TimeUnit.HOURS);
	}

	@Override
	public ITemplateType getTemplateType(String typeId) {
		return this.templateTypes.get(ITemplateType.BEAN_NAME_PREFIX + typeId);
	}

	/**
	 * 扫描模板目录，创建模板数据库记录
	 * 
	 * @param site
	 */
	@Override
	public void scanTemplates(CmsSite site) {
		this.publishPipeService.getPublishPipes(site.getSiteId()).forEach(pp -> {
			String siteRoot = SiteUtils.getSiteRoot(site, pp.getCode());
			String templateDirectory = siteRoot + ContentCoreConsts.TemplateDirectory;
			List<File> templateFiles = FileExUtils.loopFiles(templateDirectory, new FileFilter() {

				@Override
				public boolean accept(File f) {
					return f.getName().endsWith(TemplateSuffix.getValue());
				}
			});
			for (File file : templateFiles) {
				String path = StringUtils.substringAfterLast(FileExUtils.normalizePath(file.getAbsolutePath()),
						ContentCoreConsts.TemplateDirectory);
				this.lambdaQuery().eq(CmsTemplate::getPath, path).oneOpt().ifPresentOrElse(t -> {
					if (t.getModifyTime() != file.lastModified()) {
						try {
							t.setFilesize(file.length());
							t.setContent(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
							t.setModifyTime(file.lastModified());
							t.updateBy(SysConstants.SYS_OPERATOR);
							updateById(t);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}, () -> {
					try {
						CmsTemplate t = new CmsTemplate();
						t.setSiteId(site.getSiteId());
						t.setPublishPipeCode(pp.getCode());
						t.setPath(path);
						t.setFilesize(file.length());
						t.setContent(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
						t.setModifyTime(file.lastModified());
						t.createBy(SysConstants.SYS_OPERATOR);
						save(t);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			}
		});
	}
	
	/**
	 * 模板文件重命名
	 * 
	 * @param templateId
	 * @param newPath
	 * @param operator
	 * @throws IOException
	 */
	@Override
	public void renameTemplate(TemplateRenameDTO dto) throws IOException {
		CmsTemplate template = this.getById(dto.getTemplateId());
		Assert.notNull(template, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("templateId", dto.getTemplateId()));
		
		String newPath = FileExUtils.normalizePath(dto.getPath());
		if (!dto.getPath().equals(newPath)) {
			CmsSite site = this.siteService.getSite(template.getSiteId());
			String siteRoot = SiteUtils.getSiteRoot(site, template.getPublishPipeCode());
			File file = new File(siteRoot + ContentCoreConsts.TemplateDirectory + template.getPath());
			File dest = new File(siteRoot + ContentCoreConsts.TemplateDirectory + newPath);
			FileUtils.moveFile(file, dest);
			
			template.setPath(newPath);
		}
		template.setRemark(dto.getRemark());
		template.updateBy(dto.getOperator().getUsername());
		this.updateById(template);
	}
	
	/**
	 * 保存模板内容
	 * 
	 * @throws IOException 
	 */
	@Override
	public void saveTemplate(TemplateUpdateDTO dto) throws IOException {
		CmsTemplate dbTemplate = this.getById(dto.getTemplateId());
		dbTemplate.setContent(dto.getContent());
		dbTemplate.setRemark(dto.getRemark());
		// 变更文件内容
		File file = this.getTemplateFile(dbTemplate);
		file.getParentFile().mkdirs();
		FileUtils.writeStringToFile(file, dto.getContent(), StandardCharsets.UTF_8);
		
		dbTemplate.setModifyTime(file.lastModified());
		dbTemplate.updateBy(dto.getOperator().getUsername());
		this.updateById(dbTemplate);
	}
	
	/**
	 * 新建模板文件
	 * 
	 * @param dto
	 * @throws IOException
	 */
	@Override
	public void addTemplate(TemplateAddDTO dto) throws IOException {
		CmsTemplate template = new CmsTemplate();
		template.setSiteId(dto.getSiteId());
		template.setPublishPipeCode(dto.getPublishPipeCode());
		template.setPath(FileExUtils.normalizePath(dto.getPath()));
		template.setRemark(dto.getRemark());
		
		File file = this.getTemplateFile(template);
		if (file.exists()) {
			throw ContentCoreErrorCode.TEMPLATE_PATH_EXISTS.exception();
		}
		FileUtils.writeStringToFile(file, StringUtils.EMPTY, StandardCharsets.UTF_8);
		
		template.setContent(StringUtils.EMPTY);
		template.setModifyTime(file.lastModified());
		template.setFilesize(file.length());
		template.createBy(dto.getOperator().getUsername());
		this.save(template);
	}

	@Override
	public void deleteTemplates(List<Long> templateIds) throws IOException {
		List<CmsTemplate> templates = this.listByIds(templateIds);
		for (CmsTemplate template : templates) {
			File f = this.getTemplateFile(template);
			FileUtils.delete(f);
		}
		this.removeByIds(templateIds);
	}

	/**
	 * 获取模板文件
	 * 
	 * @param template
	 * @return
	 */
	@Override
	public File getTemplateFile(CmsTemplate template) {
		CmsSite site = this.siteService.getSite(template.getSiteId());
		String siteRoot = SiteUtils.getSiteRoot(site, template.getPublishPipeCode());
		File templateFile = new File(siteRoot + ContentCoreConsts.TemplateDirectory + template.getPath());
		if (templateFile.exists() && !templateFile.isFile()) {
			throw ContentCoreErrorCode.TEMPLATE_PATH_EXISTS.exception();
		}
		return templateFile;
	}

	@Override
	public File findTemplateFile(CmsSite site, String templatePath, String publishPipeCode) {
		if (StringUtils.isEmpty(templatePath)) {
			return null;
		}
		String siteRoot = SiteUtils.getSiteRoot(site, publishPipeCode);
		File templateFile = new File(siteRoot + ContentCoreConsts.TemplateDirectory + templatePath);
		if (!templateFile.exists() || !templateFile.isFile()) {
			return null;
		}
		return templateFile;
	}
}
