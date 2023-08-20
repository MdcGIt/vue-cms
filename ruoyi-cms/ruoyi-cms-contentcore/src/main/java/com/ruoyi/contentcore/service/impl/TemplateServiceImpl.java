package com.ruoyi.contentcore.service.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.contentcore.util.TemplateUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileExUtils;
import com.ruoyi.contentcore.ContentCoreConsts;
import com.ruoyi.contentcore.config.CMSConfig;
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

	private final static String TEMPLATE_STATIC_CONTENT_CACHE_KEY_PREFIX = CMSConfig.CachePrefix + "template:";

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
	public void clearTemplateStaticContentCache(String templateId) {
		this.redisCache.deleteObject(TEMPLATE_STATIC_CONTENT_CACHE_KEY_PREFIX + templateId);
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
		List<CmsTemplate> dbTemplates = this.lambdaQuery().eq(CmsTemplate::getSiteId, site.getSiteId()).list();
		this.publishPipeService.getPublishPipes(site.getSiteId()).forEach(pp -> {
			String siteRoot = SiteUtils.getSiteRoot(site, pp.getCode());
			String templateDirectory = siteRoot + ContentCoreConsts.TemplateDirectory;
			// 处理变更模板
			List<File> templateFiles = FileExUtils.loopFiles(templateDirectory,
					f -> f.getName().endsWith(TemplateSuffix.getValue()));
			for (File file : templateFiles) {
				String path = StringUtils.substringAfterLast(FileExUtils.normalizePath(file.getAbsolutePath()),
						ContentCoreConsts.TemplateDirectory);
				Optional<CmsTemplate> opt = dbTemplates.stream()
						.filter(t -> t.getPublishPipeCode().equals(pp.getCode()) && t.getPath().equals(path))
						.findFirst();
				opt.ifPresentOrElse(t -> {
					System.out.println("scan template: " + file.getName() + "|" + file.lastModified() + " = " + t.getModifyTime());
					if (t.getModifyTime() != file.lastModified()) {
						try {
							t.setFilesize(file.length());
							t.setContent(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
							t.setModifyTime(file.lastModified());
							t.updateBy(SysConstants.SYS_OPERATOR);
							updateById(t);
							// 清理include缓存
							this.clearTemplateStaticContentCache(t);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}, () -> {
					try {
						CmsTemplate t = new CmsTemplate();
						t.setTemplateId(IdUtils.getSnowflakeId());
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
			// 处理删除掉的模板
			List<String> templatePaths = templateFiles.stream().map(f -> {
				return StringUtils.substringAfterLast(FileExUtils.normalizePath(f.getAbsolutePath()),
						ContentCoreConsts.TemplateDirectory);
			}).toList();
			List<CmsTemplate> list = this.lambdaQuery().eq(CmsTemplate::getSiteId, site.getSiteId())
					.eq(CmsTemplate::getPublishPipeCode, pp.getCode()).list();
			for (CmsTemplate template : list) {
				if (!templatePaths.contains(template.getPath())) {
					this.removeById(template);
				}
			}

		});
	}

	/**
	 * 模板文件重命名
	 *
	 * @param dto
	 * @throws IOException
	 */
	@Override
	public void renameTemplate(CmsTemplate template, String path, String remark, String operator) throws IOException {
		String newPath = FileExUtils.normalizePath(path);
		if (!template.getPath().equals(newPath)) {
			CmsSite site = this.siteService.getSite(template.getSiteId());
			String siteRoot = SiteUtils.getSiteRoot(site, template.getPublishPipeCode());
			File file = new File(siteRoot + ContentCoreConsts.TemplateDirectory + template.getPath());
			File dest = new File(siteRoot + ContentCoreConsts.TemplateDirectory + newPath);
			FileUtils.moveFile(file, dest);

			template.setPath(newPath);
		}
		template.setRemark(remark);
		template.updateBy(operator);
		this.updateById(template);
	}

	/**
	 * 保存模板内容
	 *
	 * @throws IOException
	 */
	@Override
	public void saveTemplate(CmsTemplate template, TemplateUpdateDTO dto) throws IOException {
		template.setContent(dto.getContent());
		template.setRemark(dto.getRemark());
		// 变更文件内容
		File file = this.getTemplateFile(template);
		file.getParentFile().mkdirs();
		FileUtils.writeStringToFile(file, dto.getContent(), StandardCharsets.UTF_8);

		template.setModifyTime(file.lastModified());
		template.updateBy(dto.getOperator().getUsername());
		this.updateById(template);
		// 清理include缓存
		this.clearTemplateStaticContentCache(template);
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

		template.setTemplateId(IdUtils.getSnowflakeId());
		template.setContent(StringUtils.EMPTY);
		template.setModifyTime(file.lastModified());
		template.setFilesize(file.length());
		template.createBy(dto.getOperator().getUsername());
		this.save(template);
	}

	@Override
	public void deleteTemplates(CmsSite site, List<Long> templateIds) throws IOException {
		List<CmsTemplate> templates = this.lambdaQuery()
				.eq(CmsTemplate::getSiteId, site.getSiteId())
				.in(CmsTemplate::getTemplateId, templateIds)
				.list();
		for (CmsTemplate template : templates) {
			File f = this.getTemplateFile(template);
			if (f.exists()) {
				FileUtils.delete(f);
			}
			// 清理include缓存
			this.clearTemplateStaticContentCache(template);
		}
		this.removeByIds(templateIds);
	}

	private void clearTemplateStaticContentCache(CmsTemplate template) {
		CmsSite site = this.siteService.getSite(template.getSiteId());
		String templateKey = SiteUtils.getTemplateKey(site, template.getPublishPipeCode(), template.getPath());
		this.clearTemplateStaticContentCache(templateKey);
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
