package com.ruoyi.contentcore.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.file.FileExUtils;
import com.ruoyi.contentcore.config.CMSConfig;
import com.ruoyi.contentcore.core.IPublishPipeProp;
import com.ruoyi.contentcore.core.IPublishPipeProp.PublishPipePropUseType;
import com.ruoyi.contentcore.domain.CmsPublishPipe;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.dto.PublishPipeProp;
import com.ruoyi.contentcore.mapper.CmsPublishPipeMapper;
import com.ruoyi.contentcore.service.IPublishPipeService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.SiteUtils;
import com.ruoyi.system.fixed.dict.EnableOrDisable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublishPipeServiceImpl extends ServiceImpl<CmsPublishPipeMapper, CmsPublishPipe>
		implements IPublishPipeService {

	private final ISiteService siteService;

	private final List<IPublishPipeProp> publishPipeProps;

	/**
	 * 获取站点下指定使用场景的发布通道数据列表
	 * 
	 * @param siteId
	 * @param useType
	 * @param props
	 * @return
	 */
	@Override
	public List<PublishPipeProp> getPublishPipeProps(Long siteId, PublishPipePropUseType useType,
			Map<String, Map<String, Object>> props) {
		List<IPublishPipeProp> list = this.publishPipeProps.stream().filter(p -> p.getUseTypes().contains(useType))
				.toList();
		return this.getPublishPipes(siteId).stream().map(pp -> {
			PublishPipeProp prop = PublishPipeProp.newInstance(pp.getCode(), pp.getName(),
					Objects.isNull(props) ? null : props.get(pp.getCode()));
			list.forEach(p -> {
				if (!prop.getProps().containsKey(p.getKey())) {
					prop.getProps().put(p.getKey(), p.getDefaultValue());
				}
			});
			return prop;
		}).toList();
	}

	@Override
	public String getPublishPipePropValue(String propKey, String publishPipeCode,
			Map<String, Map<String, Object>> props) {
		if (props != null) {
			return MapUtils.getString(props.get(publishPipeCode), propKey);
		}
		Optional<IPublishPipeProp> opt = this.publishPipeProps.stream().filter(p -> p.getKey().equals(propKey)).findFirst();
		if (opt.isPresent()) {
			return opt.get().getDefaultValue();
		} else {
			log.warn("Unknow publish-pipe prop: {}", propKey);
		}
		return null;
	}

	@Override
	public List<CmsPublishPipe> getAllPublishPipes(Long siteId) {
		LambdaQueryWrapper<CmsPublishPipe> q = new LambdaQueryWrapper<CmsPublishPipe>()
				.eq(CmsPublishPipe::getSiteId, siteId).orderByAsc(CmsPublishPipe::getSort);
		return this.list(q);
	}

	@Override
	public List<CmsPublishPipe> getPublishPipes(Long siteId) {
		LambdaQueryWrapper<CmsPublishPipe> q = new LambdaQueryWrapper<CmsPublishPipe>()
				.eq(CmsPublishPipe::getSiteId, siteId).eq(CmsPublishPipe::getState, EnableOrDisable.ENABLE)
				.orderByAsc(CmsPublishPipe::getSort);
		return this.list(q);
	}

	/**
	 * 站点首页模板初始默认内容
	 */
	private static final String DEFAULT_INDEX_TEMPLATE_CONTENT = """
			<html>
				<head>
					<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">
					<title>${site.name}</title>
				</head>
				<body>
					<h1>${site.name}</h1>
					<h3>${site.description!}</h3>
					<h3>${site.createTime!}</h3>
				</body>
			</html>
			 		""";

	@Override
	public void addPublishPipe(CmsPublishPipe publishPipe) throws IOException {
		boolean checkCodeUnique = checkCodeUnique(publishPipe.getCode(), publishPipe.getSiteId(),
				publishPipe.getPublishpipeId());
		Assert.isTrue(checkCodeUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("code: " + publishPipe.getCode()));

		publishPipe.setPublishpipeId(IdUtils.getSnowflakeId());
		publishPipe.setCreateTime(LocalDateTime.now());
		this.save(publishPipe);

		// 创建发布通道目录
		CmsSite site = this.siteService.getSite(publishPipe.getSiteId());
		String siteRoot = SiteUtils.getSiteRoot(site, publishPipe.getCode());
		FileExUtils.mkdirs(siteRoot + "js"); // js文件目录
		FileExUtils.mkdirs(siteRoot + "css"); // css文件目录
		FileExUtils.mkdirs(siteRoot + "images"); // 图片资源目录
		FileExUtils.mkdirs(siteRoot + "include"); // 区块文件目录
		FileExUtils.mkdirs(siteRoot + "html"); // 静态文件目录
		FileExUtils.mkdirs(siteRoot + "template"); // 模板文件目录
		// 默认首页模板
		String indexTemplateFilePath = siteRoot + site.getIndexTemplate(publishPipe.getCode());
		File indexTemplateFile = new File(indexTemplateFilePath);
		if (!indexTemplateFile.exists()) {
			FileUtils.writeStringToFile(indexTemplateFile, DEFAULT_INDEX_TEMPLATE_CONTENT, StandardCharsets.UTF_8);
		}
	}

	@Override
	public void savePublishPipe(CmsPublishPipe publishPipe) throws IOException {
		boolean checkCodeUnique = checkCodeUnique(publishPipe.getCode(), publishPipe.getSiteId(),
				publishPipe.getPublishpipeId());
		Assert.isTrue(checkCodeUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("code: " + publishPipe.getCode()));

		CmsPublishPipe dbPublishPipe = this.getById(publishPipe.getPublishpipeId());
		String oldCode = dbPublishPipe.getCode();
		BeanUtils.copyProperties(publishPipe, dbPublishPipe);
		dbPublishPipe.updateBy(publishPipe.getUpdateBy());
		this.updateById(dbPublishPipe);

		if (!oldCode.equals(dbPublishPipe.getCode())) {
			// 编码变更需要修改目录
			CmsSite site = this.siteService.getSite(publishPipe.getSiteId());
			String source = SiteUtils.getSiteRoot(site, oldCode);
			String dest = SiteUtils.getSiteRoot(site, dbPublishPipe.getCode());
			File sourceFile = new File(source);
			if (sourceFile.exists()) {
				FileUtils.moveDirectory(sourceFile, new File(dest));
			}
		}
	}

	private boolean checkCodeUnique(String code, Long siteId, Long publishpipeId) {
		LambdaQueryWrapper<CmsPublishPipe> q = new LambdaQueryWrapper<CmsPublishPipe>()
				.eq(CmsPublishPipe::getCode, code).eq(CmsPublishPipe::getSiteId, siteId)
				.ne(publishpipeId != null && publishpipeId > 0, CmsPublishPipe::getPublishpipeId, publishpipeId);
		return this.count(q) == 0;
	}

	@Override
	public void deletePublishPipe(List<Long> publishPipeIds) {
		List<CmsPublishPipe> pipes = this.listByIds(publishPipeIds);
		for (CmsPublishPipe pipe : pipes) {
			// 删除数据库记录
			if (this.removeById(pipe)) {
				CmsSite site = siteService.getSite(pipe.getSiteId());
				this.onPublishPipeDelete(pipe, site);
			}
		}
	}

	@Override
	public void deletePublishPipeBySite(CmsSite site) {
		List<CmsPublishPipe> publishPipes = this
				.list(new LambdaQueryWrapper<CmsPublishPipe>().eq(CmsPublishPipe::getSiteId, site.getSiteId()));
		for (CmsPublishPipe pipe : publishPipes) {
			this.removeById(pipe);
			this.onPublishPipeDelete(pipe, site);
		}
	}

	private void onPublishPipeDelete(CmsPublishPipe pipe, CmsSite site) {
		// 删除发布通道目录，实际备份目录
		String siteRoot = SiteUtils.getSiteRoot(site, pipe.getCode());
		String bakDir = CMSConfig.getResourceRoot() + site.getPath() + "_" + pipe.getCode() + "_bak_"
				+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		File siteRootFile = new File(siteRoot);
		if (siteRootFile.exists()) {
			try {
				FileUtils.moveDirectory(siteRootFile, new File(bakDir));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
