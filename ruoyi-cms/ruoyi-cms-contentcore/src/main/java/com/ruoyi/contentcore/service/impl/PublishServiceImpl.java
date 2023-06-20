package com.ruoyi.contentcore.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.contentcore.core.impl.*;
import com.ruoyi.contentcore.util.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.async.AsyncTask;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.staticize.StaticizeService;
import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileExUtils;
import com.ruoyi.contentcore.core.IContent;
import com.ruoyi.contentcore.core.IContentType;
import com.ruoyi.contentcore.core.IPageWidget;
import com.ruoyi.contentcore.core.IPublishPipeProp;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.CmsPageWidget;
import com.ruoyi.contentcore.domain.CmsPublishPipe;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.enums.ContentCopyType;
import com.ruoyi.contentcore.exception.ContentCoreErrorCode;
import com.ruoyi.contentcore.listener.event.AfterContentPublishEvent;
import com.ruoyi.contentcore.properties.MaxPageOnContentPublishProperty;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.service.IPublishPipeService;
import com.ruoyi.contentcore.service.IPublishService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.service.ITemplateService;
import com.ruoyi.contentcore.template.ITemplateType;
import com.ruoyi.contentcore.template.impl.CatalogTemplateType;
import com.ruoyi.contentcore.template.impl.ContentTemplateType;
import com.ruoyi.contentcore.template.impl.SiteTemplateType;
import com.ruoyi.system.fixed.dict.YesOrNo;

import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublishServiceImpl implements IPublishService, ApplicationContextAware {

	private static final Logger logger = LoggerFactory.getLogger(PublishServiceImpl.class);

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final IContentService contentService;

	private final ITemplateService templateService;

	private final IPublishPipeService publishPipeService;

	private final StaticizeService staticizeService;

	private final AsyncTaskManager asyncTaskManager;

	private ApplicationContext applicationContext;

	@Override
	public String getSitePageData(CmsSite site, String publishPipeCode, boolean isPreview)
			throws IOException, TemplateException {
		TemplateContext context = this.generateSiteTemplateContext(site, publishPipeCode, isPreview);

		long s = System.currentTimeMillis();
		try (StringWriter writer = new StringWriter()) {
			this.staticizeService.process(context, writer);
			return writer.toString();
		} finally {
			logger.debug("[{}]首页模板解析：{}\t耗时：{}ms", publishPipeCode, site.getName(), System.currentTimeMillis() - s);
		}
	}

	@Override
	public void publishSiteIndex(CmsSite site) throws IOException, TemplateException {
		// 发布所有通道页面
		List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(site.getSiteId());
		Assert.isTrue(!publishPipes.isEmpty(), ContentCoreErrorCode.NO_PUBLISHPIPE::exception);
		this.siteStaticize(site);
	}

	@Override
	public AsyncTask publishAll(CmsSite site, final String contentStatus) {
		String taskId = "publish_site_" + site.getSiteId();
		AsyncTask asyncTask = new AsyncTask() {

			@Override
			public void run0() throws InterruptedException {
				publishPipeService.getPublishPipes(site.getSiteId()).forEach(pp -> {
					try {
						String siteRoot = SiteUtils.getSiteRoot(site, pp.getCode());
						FileUtils.deleteDirectory(new File(siteRoot + "include/"));
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
				
				List<CmsCatalog> catalogList = catalogService
						.list(new LambdaQueryWrapper<CmsCatalog>().eq(CmsCatalog::getSiteId, site.getSiteId()));
				for (CmsCatalog catalog : catalogList) {
					// 先发布内容
					int pageSize = 50;
					LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
							.eq(CmsContent::getCatalogId, catalog.getCatalogId())
							.eq(CmsContent::getStatus, contentStatus);
					long total = contentService.count(q);
					int count = 1;
					for (int i = 0; i * pageSize < total; i++) {
						Page<CmsContent> page = contentService.page(new Page<>(i, pageSize, false), q);
						for (CmsContent xContent : page.getRecords()) {
							this.setProgressInfo((int) (count++ * 100 / total),
									"正在发布内容：" + catalog.getName() + "[" + count + " / " + total + "]");
							contentStaticize(xContent);
							this.checkInterrupt(); // 允许中断
						}
					}
				}
				// 发布栏目
				for (int i = 0; i < catalogList.size(); i++) {
					CmsCatalog catalog = catalogList.get(i);
					this.setProgressInfo((i * 100) / catalogList.size(), "正在发布栏目：" + catalog.getName());
					catalogStaticize(catalog, -1);
					this.checkInterrupt(); // 允许中断
				}
				// 发布站点
				this.setProgressInfo(99, "正在发布首页：" + site.getName());
				siteStaticize(site);
				this.setProgressInfo(100, "发布完成");
			}
		};
		asyncTask.setType("Publish");
		asyncTask.setTaskId(taskId);
		this.asyncTaskManager.execute(asyncTask);
		return asyncTask;
	}

	@Override
	public void siteStaticize(CmsSite site) {
		List<CmsPublishPipe> publishPipeCodes = this.publishPipeService.getPublishPipes(site.getSiteId());
		for (CmsPublishPipe pp : publishPipeCodes) {
			try {
				AsyncTaskManager
						.setTaskMessage(StringUtils.messageFormat("[{0}]正在发布站点首页：{1}", pp.getCode(), site.getName()));
				TemplateContext templateContext = this.generateSiteTemplateContext(site, pp.getCode(), false);

				long s = System.currentTimeMillis();
				templateContext.setDirectory(SiteUtils.getSiteRoot(site, pp.getCode()));
				templateContext.setFirstFileName("index" + StringUtils.DOT + site.getStaticSuffix(pp.getCode()));
				this.staticizeService.process(templateContext);
				logger.debug("[{}]首页模板解析：{}，耗时：{}ms", pp.getCode(), site.getName(), (System.currentTimeMillis() - s));
			} catch (Exception e) {
				logger.warn(AsyncTaskManager.addErrMessage(StringUtils.messageFormat("[{0}][{1}]站点首页解析失败：{2}",
						pp.getCode(), site.getName(), e.getMessage())));
			}
		}
	}

	private TemplateContext generateSiteTemplateContext(CmsSite site, String publishPipeCode, boolean isPreview) {
		String indexTemplate = site.getIndexTemplate(publishPipeCode);
		File templateFile = this.templateService.findTemplateFile(site, indexTemplate, publishPipeCode);
		Assert.notNull(templateFile,
				() -> ContentCoreErrorCode.TEMPLATE_EMPTY.exception(publishPipeCode, indexTemplate));

		// 模板ID = 通道:站点目录:模板文件名
		String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, indexTemplate);
		TemplateContext templateContext = new TemplateContext(templateKey, isPreview, publishPipeCode);
		// init template datamode
		TemplateUtils.initGlobalVariables(site, templateContext);
		// init templateType data to datamode
		ITemplateType templateType = templateService.getTemplateType(SiteTemplateType.TypeId);
		templateType.initTemplateData(site.getSiteId(), templateContext);
		return templateContext;
	}

	@Override
	public String getCatalogPageData(CmsCatalog catalog, int pageIndex, boolean listFlag, String publishPipeCode, boolean isPreview)
			throws IOException, TemplateException {
		if (!catalog.isStaticize() || CatalogType_Link.ID.equals(catalog.getCatalogType())) {
			throw new RuntimeException("栏目设置不静态化或未链接类型栏目：" + catalog.getName());
		}
		String templateFilename = catalog.getListTemplate(publishPipeCode);
		if (!listFlag && pageIndex == 1) {
			// 获取首页模板
			String indexTemplate = catalog.getIndexTemplate(publishPipeCode);
			if (StringUtils.isNotEmpty(indexTemplate)) {
				templateFilename = indexTemplate;
			}
		}
		CmsSite site = this.siteService.getById(catalog.getSiteId());
		if (StringUtils.isEmpty(templateFilename)) {
			// 站点默认模板
			templateFilename = PublishPipeProp_DefaultListTemplate.getValue(publishPipeCode,
					site.getPublishPipeProps());
		}
		final String template = templateFilename;
		File templateFile = this.templateService.findTemplateFile(site, template, publishPipeCode);
		Assert.notNull(templateFile, () -> ContentCoreErrorCode.TEMPLATE_EMPTY.exception(publishPipeCode, template));

		long s = System.currentTimeMillis();
		// 生成静态页面
		String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, template);
		TemplateContext templateContext = new TemplateContext(templateKey, isPreview, publishPipeCode);
		templateContext.setPageIndex(pageIndex);
		// init template variables
		TemplateUtils.initGlobalVariables(site, templateContext);
		// init templateType variables
		ITemplateType templateType = templateService.getTemplateType(CatalogTemplateType.TypeId);
		templateType.initTemplateData(catalog.getCatalogId(), templateContext);
		// 分页链接
		String catalogLink = this.catalogService.getCatalogLink(catalog, 1, publishPipeCode, isPreview);
		templateContext.setFirstFileName(catalogLink);
		templateContext.setOtherFileName(catalogLink + "&pi=" + TemplateContext.PlaceHolder_PageNo);
		try (StringWriter writer = new StringWriter()) {
			this.staticizeService.process(templateContext, writer);
			return writer.toString();
		} finally {
			logger.debug("[{}]栏目页模板解析：{}，耗时：{}ms", publishPipeCode, catalog.getName(),
					(System.currentTimeMillis() - s));
		}
	}

	@Override
	public AsyncTask publishCatalog(CmsCatalog catalog, boolean publishChild, boolean publishDetail,
			final String publishStatus) {
		List<CmsPublishPipe> publishPipes = publishPipeService.getPublishPipes(catalog.getSiteId());
		Assert.isTrue(!publishPipes.isEmpty(), ContentCoreErrorCode.NO_PUBLISHPIPE::exception);

		String taskId = "publish_catalog_" + catalog.getCatalogId();
		AsyncTask asyncTask = new AsyncTask() {

			@Override
			public void run0() throws InterruptedException {
				List<CmsCatalog> catalogs = new ArrayList<>();
				catalogs.add(catalog);
				// 是否包含子栏目
				if (publishChild) {
					LambdaQueryWrapper<CmsCatalog> q = new LambdaQueryWrapper<CmsCatalog>()
							.eq(CmsCatalog::getStaticFlag, YesOrNo.YES) // 可静态化
							.eq(CmsCatalog::getVisibleFlag, YesOrNo.YES) // 可见
							.ne(CmsCatalog::getCatalogType, CatalogType_Link.ID) // 普通栏目
							.likeRight(CmsCatalog::getAncestors, catalog.getAncestors());
					catalogs.addAll(catalogService.list(q));
				}
				// 先发布内容
				for (CmsCatalog catalog : catalogs) {
					int pageSize = 50;
					LambdaQueryWrapper<CmsContent> q2 = new LambdaQueryWrapper<CmsContent>()
							.eq(CmsContent::getCatalogId, catalog.getCatalogId())
							.eq(CmsContent::getStatus, publishStatus).ne(CmsContent::getLinkFlag, YesOrNo.YES); // 非标题内容
					long total = contentService.count(q2);
					int count = 1;
					for (int i = 0; i * pageSize < total; i++) {
						Page<CmsContent> page = contentService.page(new Page<>(i, pageSize, false), q2);
						for (CmsContent xContent : page.getRecords()) {
							this.setProgressInfo((int) (count++ * 100 / total),
									"正在发布内容：" + catalog.getName() + "[" + count + " / " + total + "]");
							contentStaticize(xContent);
							this.checkInterrupt(); // 允许中断
						}
					}
				}
				// 发布栏目
				for (int i = 0; i < catalogs.size(); i++) {
					CmsCatalog catalog = catalogs.get(i);
					this.setProgressInfo((i * 100) / catalogs.size(), "正在发布栏目：" + catalog.getName());
					catalogStaticize(catalog, -1);
					this.checkInterrupt(); // 允许中断
				}
				// 发布站点
				this.setPercent(99);
				siteStaticize(siteService.getSite(catalog.getSiteId()));
				this.setProgressInfo(100, "发布完成");
			}
		};
		asyncTask.setType("Publish");
		asyncTask.setTaskId(taskId);
		this.asyncTaskManager.execute(asyncTask);
		return asyncTask;
	}

	@Override
	public void catalogStaticize(CmsCatalog catalog, int pageMax) {
		if (!catalog.isStaticize()) {
			return;
		}
		List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(catalog.getSiteId());
		for (CmsPublishPipe pp : publishPipes) {
			this.doCatalogStaticize(catalog, pp.getCode(), pageMax);
		}
	}

	private void doCatalogStaticize(CmsCatalog catalog, String publishPipeCode, int pageMax) {
		CmsSite site = this.siteService.getSite(catalog.getSiteId());
		if (!catalog.isStaticize()) {
			logger.warn("【{}】栏目设置不静态化：{}", publishPipeCode, catalog.getName());
			return;
		}
		if (!catalog.isVisible()) {
			logger.warn("【{}】不可见栏目跳过静态化：{}", publishPipeCode, catalog.getName());
			return;
		}
		if (CatalogType_Link.ID.equals(catalog.getCatalogType())) {
			logger.warn("【{}】链接类型栏目跳过静态化：{}", publishPipeCode, catalog.getName());
			return;
		}
		String indexTemplate = PublishPipeProp_IndexTemplate.getValue(publishPipeCode, catalog.getPublishPipeProps());
		String listTemplate = PublishPipeProp_ListTemplate.getValue(publishPipeCode, catalog.getPublishPipeProps());
		if (StringUtils.isEmpty(listTemplate)) {
			listTemplate = PublishPipeProp_DefaultListTemplate.getValue(publishPipeCode, site.getPublishPipeProps()); // 取站点默认模板
		}
		File indexTemplateFile = this.templateService.findTemplateFile(site, indexTemplate, publishPipeCode);
		File listTemplateFile = this.templateService.findTemplateFile(site, listTemplate, publishPipeCode);
		if (indexTemplateFile == null && listTemplateFile == null) {
			logger.warn(AsyncTaskManager.addErrMessage(StringUtils.messageFormat("[{0}]栏目首页模板和列表页模板未配置或不存在：{1}",
					publishPipeCode, catalog.getCatalogId() + "#" + catalog.getName())));
			return;
		}
		String siteRoot = SiteUtils.getSiteRoot(site, publishPipeCode);
		String dirPath = siteRoot + catalog.getPath();
		FileExUtils.mkdirs(dirPath);
		String staticSuffix = site.getStaticSuffix(publishPipeCode); // 静态化文件类型

		// 发布栏目首页
		if (Objects.nonNull(indexTemplateFile)) {
			try {
				String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, indexTemplate);
				TemplateContext templateContext = new TemplateContext(templateKey, false, publishPipeCode);
				templateContext.setDirectory(dirPath);
				templateContext.setFirstFileName("index" + StringUtils.DOT + staticSuffix);
				// init template variables
				TemplateUtils.initGlobalVariables(site, templateContext);
				// init templateType variables
				ITemplateType templateType = templateService.getTemplateType(CatalogTemplateType.TypeId);
				templateType.initTemplateData(catalog.getCatalogId(), templateContext);
				// staticize
				this.staticizeService.process(templateContext);
			} catch (IOException | TemplateException e) {
				e.printStackTrace();
				logger.warn(AsyncTaskManager.addErrMessage(StringUtils.messageFormat("[{0}]栏目首页解析失败：{1}",
						publishPipeCode, catalog.getCatalogId() + "#" + catalog.getName())));
			}
		}
		// 发布栏目列表页
		if (Objects.nonNull(listTemplateFile)) {
			try {
				String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, listTemplate);
				TemplateContext templateContext = new TemplateContext(templateKey, false, publishPipeCode);
				templateContext.setMaxPageNo(pageMax);
				templateContext.setDirectory(dirPath);
				String name = Objects.nonNull(indexTemplateFile) ? "list" : "index";
				templateContext.setFirstFileName(name + StringUtils.DOT + staticSuffix);
				templateContext.setOtherFileName(
						name + "_" + TemplateContext.PlaceHolder_PageNo + StringUtils.DOT + staticSuffix);
				// init template variables
				TemplateUtils.initGlobalVariables(site, templateContext);
				// init templateType variables
				ITemplateType templateType = templateService.getTemplateType(CatalogTemplateType.TypeId);
				templateType.initTemplateData(catalog.getCatalogId(), templateContext);
				// staticize
				this.staticizeService.process(templateContext);
			} catch (Exception e1) {
				logger.warn(AsyncTaskManager.addErrMessage(StringUtils.messageFormat("[{0}]栏目列表页解析失败：{1}",
						publishPipeCode, catalog.getCatalogId() + "#" + catalog.getName())));
				e1.printStackTrace();
			}
		}
	}

	private String getDetailTemplate(CmsSite site, CmsCatalog catalog, CmsContent content, String publishPipeCode) {
		String detailTemplate = PublishPipeProp_ContentTemplate.getValue(publishPipeCode,
				content.getPublishPipeProps());
		if (StringUtils.isEmpty(detailTemplate)) {
			// 无内容独立模板取栏目配置
			detailTemplate = this.publishPipeService.getPublishPipePropValue(
					IPublishPipeProp.DetailTemplatePropPrefix + content.getContentType(), publishPipeCode,
					catalog.getPublishPipeProps());
			if (StringUtils.isEmpty(detailTemplate)) {
				// 无栏目配置去站点默认模板配置
				detailTemplate = this.publishPipeService.getPublishPipePropValue(
						IPublishPipeProp.DefaultDetailTemplatePropPrefix + content.getContentType(), publishPipeCode,
						site.getPublishPipeProps());
			}
		}
		return detailTemplate;
	}

	private String getContentExTemplate(CmsSite site, CmsCatalog catalog, CmsContent content, String publishPipeCode) {
		String exTemplate = PublishPipeProp_ContentExTemplate.getValue(publishPipeCode,
				content.getPublishPipeProps());
		if (StringUtils.isEmpty(exTemplate)) {
			// 无内容独立扩展模板取栏目配置
			exTemplate = PublishPipeProp_ContentExTemplate.getValue(publishPipeCode, catalog.getPublishPipeProps());
		}
		return exTemplate;
	}

	@Override
	public String getContentPageData(CmsContent content, int pageIndex, String publishPipeCode, boolean isPreview)
			throws IOException, TemplateException {
		CmsSite site = this.siteService.getById(content.getSiteId());
		CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
		if (!catalog.isStaticize() ) {
			throw new RuntimeException("栏目设置不静态化：" + content.getTitle());
		}
		if (content.isLinkContent()) {
			throw new RuntimeException("标题内容：" + content.getTitle() + "，跳转链接：" + content.getRedirectUrl());
		}
		// 查找模板
		final String detailTemplate = getDetailTemplate(site, catalog, content, publishPipeCode);
		File templateFile = this.templateService.findTemplateFile(site, detailTemplate, publishPipeCode);
		Assert.notNull(templateFile,
				() -> ContentCoreErrorCode.TEMPLATE_EMPTY.exception(publishPipeCode, detailTemplate));

		long s = System.currentTimeMillis();
		// 生成静态页面
		try (StringWriter writer = new StringWriter()) {
			IContentType contentType = ContentCoreUtils.getContentType(content.getContentType());
			// 模板ID = 通道:站点目录:模板文件名
			String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, detailTemplate);
			TemplateContext templateContext = new TemplateContext(templateKey, isPreview, publishPipeCode);
			templateContext.setPageIndex(pageIndex);
			// init template datamode
			TemplateUtils.initGlobalVariables(site, templateContext);
			// init templateType data to datamode
			ITemplateType templateType = this.templateService.getTemplateType(ContentTemplateType.TypeId);
			templateType.initTemplateData(content.getContentId(), templateContext);
			// 分页链接
			String contentLink = this.contentService.getContentLink(content, 1, publishPipeCode, isPreview);
			templateContext.setFirstFileName(contentLink);
			templateContext.setOtherFileName(contentLink + "&pi=" + TemplateContext.PlaceHolder_PageNo);
			// staticize
			this.staticizeService.process(templateContext, writer);
			logger.debug("[{}][{}]内容模板解析：{}，耗时：{}", publishPipeCode, contentType.getName(), content.getTitle(),
					System.currentTimeMillis() - s);
			return writer.toString();
		}
	}

	/**
	 * 内容发布
	 */
	@Override
	public void publishContent(List<Long> contentIds, LoginUser operator) throws IOException, TemplateException {
		List<CmsContent> list = this.contentService.listByIds(contentIds);
		for (CmsContent cmsContent : list) {
			IContentType contentType = ContentCoreUtils.getContentType(cmsContent.getContentType());
			IContent<?> content = contentType.loadContent(cmsContent);
			content.setOperator(operator);
			if (content.publish()) {
				this.applicationContext.publishEvent(new AfterContentPublishEvent(contentType, content));
			}
		}
	}

	@Override
	public void contentStaticize(IContent<?> content) {
		CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
		if (!catalog.isStaticize()) {
			return;
		}
		List<CmsPublishPipe> publishPipeCodes = this.publishPipeService.getPublishPipes(content.getSiteId());
		if (publishPipeCodes.size() == 0) {
			return;
		}
		AsyncTask asyncTask = new AsyncTask() {

			@Override
			public void run0() throws InterruptedException {
				this.setProgressInfo(5, content.getContentEntity().getTitle());
				// 发布内容
				contentStaticize(content.getContentEntity());
				this.setPercent(10);
				// 发布关联内容，映射的引用内容
				LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
						.eq(CmsContent::getCopyId, content.getContentEntity().getContentId())
						.eq(CmsContent::getCopyType, ContentCopyType.Mapping);
				List<CmsContent> mappingContents = contentService.list(q);
				for (int i = 0; i < mappingContents.size(); i++) {
					this.setProgressInfo(i * 100 / mappingContents.size(),
							"正在发布相关映射内容：" + catalog.getName() + "[" + i + " / " + mappingContents.size() + "]");
					contentStaticize(mappingContents.get(i));
				}
				this.setPercent(20);
				// 发布关联栏目：内容所属栏目及其所有父级栏目
				Map<Long, CmsCatalog> catalogMap = new HashMap<>();
				CmsCatalog catalog = catalogService.getCatalog(content.getCatalogId());
				catalogMap.put(catalog.getCatalogId(), catalog);
				long parentId = catalog.getParentId();
				while (parentId > 0) {
					CmsCatalog parent = catalogService.getCatalog(parentId);
					if (parent == null) {
						break;
					}
					catalogMap.put(parent.getCatalogId(), parent);
					parentId = parent.getParentId();
				}
				CmsSite site = siteService.getSite(content.getSiteId());
				int pageMax = MaxPageOnContentPublishProperty.getValue(site.getConfigProps());
				int index = 0;
				for (Iterator<CmsCatalog> iterator = catalogMap.values().iterator(); iterator.hasNext();) {
					CmsCatalog c = iterator.next();
					this.setPercent(10 + ((++index * 70) / catalogMap.size()));
					catalogStaticize(c, pageMax);
					this.checkInterrupt(); // 允许中断
				}
				this.setPercent(95);
				// 发布站点
				siteStaticize(site);
				this.setProgressInfo(100, "任务完成");
			}
		};
		asyncTask.setType("publish");
		asyncTask.setTaskId("publish_content_" + DateUtils.dateTimeNow("yyyy_MM_dd_HH_mm_ss")); // 使用当前时间做taskId，不限制任务重复校验
		this.asyncTaskManager.execute(asyncTask);
	}

	@Override
	public void contentStaticize(CmsContent cmsContent) {
		List<CmsPublishPipe> publishPipes = publishPipeService.getPublishPipes(cmsContent.getSiteId());
		// 发布内容
		for (CmsPublishPipe pp : publishPipes) {
			doContentStaticize(cmsContent, pp.getCode());
			// 内容扩展模板静态化
			doContentExStaticize(cmsContent, pp.getCode());
		}
	}

	private void doContentStaticize(CmsContent content, String publishPipeCode) {
		CmsSite site = this.siteService.getSite(content.getSiteId());
		CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
		if (!catalog.isStaticize()) {
			logger.warn("【{}】栏目设置不静态化：{} - {}", publishPipeCode, catalog.getName(), content.getTitle());
			return; // 不静态化直接跳过
		}
		if (content.isLinkContent()) {
			return; // 标题内容不需要静态化
		}
		final String detailTemplate = getDetailTemplate(site, catalog, content, publishPipeCode);
		File templateFile = this.templateService.findTemplateFile(site, detailTemplate, publishPipeCode);
		if (templateFile == null) {
			logger.warn(AsyncTaskManager.addErrMessage(
					StringUtils.messageFormat("[{0}]栏目[{1}]详情页模板未设置或文件不存在", publishPipeCode, catalog.getName())));
			return;
		}
		try {
			// 自定义模板上下文
			String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, detailTemplate);
			TemplateContext templateContext = new TemplateContext(templateKey, false, publishPipeCode);
			// init template datamode
			TemplateUtils.initGlobalVariables(site, templateContext);
			// init templateType data to datamode
			ITemplateType templateType = this.templateService.getTemplateType(ContentTemplateType.TypeId);
			templateType.initTemplateData(content.getContentId(), templateContext);
			// 静态化文件地址
			this.setContentStaticPath(site, catalog, content, templateContext);
			// 静态化
			this.staticizeService.process(templateContext);
		} catch (TemplateException | IOException e) {
			logger.warn(AsyncTaskManager.addErrMessage(StringUtils.messageFormat("[{0}]内容详情页解析失败：[{1}]{2}",
					publishPipeCode, catalog.getName(), content.getTitle())));
			e.printStackTrace();
		}
	}

	private void setContentStaticPath(CmsSite site, CmsCatalog catalog, CmsContent content, TemplateContext context) {
		String siteRoot = SiteUtils.getSiteRoot(site, context.getPublishPipeCode());
		if (StringUtils.isNotBlank(content.getStaticPath())) {
			String dir = "";
			String filename = content.getStaticPath();
			if (filename.indexOf("/") > 0) {
				dir = filename.substring(0, filename.lastIndexOf("/") + 1);
				filename = filename.substring(filename.lastIndexOf("/") + 1);
			}
			context.setDirectory(siteRoot + dir);
			context.setFirstFileName(filename);
			String name = filename.substring(0, filename.lastIndexOf("."));
			String suffix = filename.substring(filename.lastIndexOf("."));
			context.setOtherFileName(name + "_" + TemplateContext.PlaceHolder_PageNo + suffix);
		} else {
			context.setDirectory(siteRoot + catalog.getPath());
			String suffix = site.getStaticSuffix(context.getPublishPipeCode());
			context.setFirstFileName(content.getContentId() + StringUtils.DOT + suffix);
			context.setOtherFileName(
					content.getContentId() + "_" + TemplateContext.PlaceHolder_PageNo + StringUtils.DOT + suffix);
		}
	}

	@Override
	public String getContentExPageData(CmsContent content, String publishPipeCode, boolean isPreview)
			throws IOException, TemplateException {
		CmsSite site = this.siteService.getById(content.getSiteId());
		CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
		if (!catalog.isStaticize() ) {
			throw new RuntimeException("栏目设置不静态化：" + content.getTitle());
		}
		if (content.isLinkContent()) {
			throw new RuntimeException("标题内容：" + content.getTitle() + "，跳转链接：" + content.getRedirectUrl());
		}
		String exTemplate = ContentUtils.getContentExTemplate(content, catalog, publishPipeCode);
		// 查找模板
		File templateFile = this.templateService.findTemplateFile(site, exTemplate, publishPipeCode);
		Assert.notNull(templateFile,
				() -> ContentCoreErrorCode.TEMPLATE_EMPTY.exception(publishPipeCode, exTemplate));

		long s = System.currentTimeMillis();
		// 生成静态页面
		try (StringWriter writer = new StringWriter()) {
			IContentType contentType = ContentCoreUtils.getContentType(content.getContentType());
			// 模板ID = 通道:站点目录:模板文件名
			String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, exTemplate);
			TemplateContext templateContext = new TemplateContext(templateKey, isPreview, publishPipeCode);
			// init template data mode
			TemplateUtils.initGlobalVariables(site, templateContext);
			// init templateType data to data mode
			ITemplateType templateType = this.templateService.getTemplateType(ContentTemplateType.TypeId);
			templateType.initTemplateData(content.getContentId(), templateContext);
			// staticize
			this.staticizeService.process(templateContext, writer);
			logger.debug("[{}][{}]内容扩展模板解析：{}，耗时：{}", publishPipeCode, contentType.getName(), content.getTitle(),
					System.currentTimeMillis() - s);
			return writer.toString();
		}
	}

	private void doContentExStaticize(CmsContent content, String publishPipeCode) {
		CmsSite site = this.siteService.getSite(content.getSiteId());
		CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
		if (!catalog.isStaticize()) {
			logger.warn("【{}】栏目设置不静态化：{} - {}", publishPipeCode, catalog.getName(), content.getTitle());
			return; // 不静态化直接跳过
		}
		if (content.isLinkContent()) {
			return; // 标题内容不需要静态化
		}
		String exTemplate = ContentUtils.getContentExTemplate(content, catalog, publishPipeCode);
		File templateFile = this.templateService.findTemplateFile(site, exTemplate, publishPipeCode);
		if (templateFile == null) {
			logger.warn("[{}]栏目[{}]详情页扩展模板未设置或文件不存在", publishPipeCode, catalog.getName());
			return;
		}
		try {
			// 自定义模板上下文
			String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, exTemplate);
			TemplateContext templateContext = new TemplateContext(templateKey, false, publishPipeCode);
			// init template datamode
			TemplateUtils.initGlobalVariables(site, templateContext);
			// init templateType data to datamode
			ITemplateType templateType = this.templateService.getTemplateType(ContentTemplateType.TypeId);
			templateType.initTemplateData(content.getContentId(), templateContext);
			// 静态化文件地址
			String siteRoot = SiteUtils.getSiteRoot(site, publishPipeCode);
			templateContext.setDirectory(siteRoot + catalog.getPath());
			String fileName = ContentUtils.getContextExFileName(content.getContentId(), site.getStaticSuffix(publishPipeCode));
			templateContext.setFirstFileName(fileName);
			// 静态化
			this.staticizeService.process(templateContext);
		} catch (TemplateException | IOException e) {
			logger.warn(AsyncTaskManager.addErrMessage(StringUtils.messageFormat("[{0}]内容扩展模板解析失败：[{1}]{2}",
					publishPipeCode, catalog.getName(), content.getTitle())));
			e.printStackTrace();
		}
	}

	@Override
	public String getPageWidgetPageData(CmsPageWidget pageWidget, boolean isPreview)
			throws IOException, TemplateException {
		CmsSite site = this.siteService.getById(pageWidget.getSiteId());
		File templateFile = this.templateService.findTemplateFile(site, pageWidget.getTemplate(),
				pageWidget.getPublishPipeCode());
		Assert.notNull(templateFile,
				() -> ContentCoreErrorCode.TEMPLATE_EMPTY.exception(pageWidget.getName(), pageWidget.getCode()));

		// 生成静态页面
		try (StringWriter writer = new StringWriter()) {
			long s = System.currentTimeMillis();
			// 模板ID = 通道:站点目录:模板文件名
			String templateKey = SiteUtils.getTemplateKey(site, pageWidget.getPublishPipeCode(),
					pageWidget.getTemplate());
			TemplateContext templateContext = new TemplateContext(templateKey, isPreview,
					pageWidget.getPublishPipeCode());
			// init template global variables
			TemplateUtils.initGlobalVariables(site, templateContext);
			// init templateType data to datamode
			ITemplateType templateType = this.templateService.getTemplateType(SiteTemplateType.TypeId);
			templateType.initTemplateData(site.getSiteId(), templateContext);
			// staticize
			this.staticizeService.process(templateContext, writer);
			logger.debug("[{}]页面部件【{}#{}】模板解析耗时：{}ms", pageWidget.getPublishPipeCode(), pageWidget.getName(),
					pageWidget.getCode(), System.currentTimeMillis() - s);
			return writer.toString();
		}
	}

	@Override
	public void pageWidgetStaticize(IPageWidget pageWidget) throws TemplateException, IOException {
		long s = System.currentTimeMillis();
		CmsPageWidget pw = pageWidget.getPageWidgetEntity();
		CmsSite site = this.siteService.getSite(pw.getSiteId());
		File templateFile = this.templateService.findTemplateFile(site, pw.getTemplate(), pw.getPublishPipeCode());
		Assert.notNull(templateFile, () -> new RuntimeException(
				StringUtils.messageFormat("页面部件【{0}%s#{1}%s】模板未配置或文件不存在", pw.getName(), pw.getCode())));

		// 静态化目录
		String dirPath = SiteUtils.getSiteRoot(site, pw.getPublishPipeCode()) + pw.getPath();
		FileExUtils.mkdirs(dirPath);
		// 自定义模板上下文
		String templateKey = SiteUtils.getTemplateKey(site, pw.getPublishPipeCode(), pw.getTemplate());
		TemplateContext templateContext = new TemplateContext(templateKey, false, pw.getPublishPipeCode());
		templateContext.setDirectory(dirPath);
		String staticFileName = PageWidgetUtils.getStaticFileName(pw, site.getStaticSuffix(pw.getPublishPipeCode()));
		templateContext.setFirstFileName(staticFileName);
		// init template datamode
		TemplateUtils.initGlobalVariables(site, templateContext);
		// init templateType data to datamode
		ITemplateType templateType = templateService.getTemplateType(SiteTemplateType.TypeId);
		templateType.initTemplateData(site.getSiteId(), templateContext);
		// staticize
		this.staticizeService.process(templateContext);
		logger.debug("[{0}]页面部件模板解析：{1}，耗时：{2}ms", pw.getPublishPipeCode(), pw.getCode(),
				System.currentTimeMillis() - s);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
