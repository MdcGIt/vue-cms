package com.ruoyi.cms.member.controller.front;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.article.ArticleContent;
import com.ruoyi.article.ArticleContentType;
import com.ruoyi.article.domain.CmsArticleDetail;
import com.ruoyi.article.service.IArticleService;
import com.ruoyi.cms.member.domain.dto.ArticleContributeDTO;
import com.ruoyi.cms.member.properties.EnableContributeProperty;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.*;
import com.ruoyi.contentcore.core.IContent;
import com.ruoyi.contentcore.core.IContentType;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.CmsResource;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.dto.ResourceUploadDTO;
import com.ruoyi.contentcore.enums.ContentOpType;
import com.ruoyi.contentcore.fixed.dict.ContentAttribute;
import com.ruoyi.contentcore.fixed.dict.ContentStatus;
import com.ruoyi.contentcore.listener.event.AfterContentDeleteEvent;
import com.ruoyi.contentcore.listener.event.AfterContentSaveEvent;
import com.ruoyi.contentcore.listener.event.BeforeContentSaveEvent;
import com.ruoyi.contentcore.perms.CatalogPermissionType;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.service.IResourceService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.CatalogUtils;
import com.ruoyi.contentcore.util.ContentCoreUtils;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.member.security.MemberUserType;
import com.ruoyi.member.security.StpMemberUtil;
import com.ruoyi.system.annotation.IgnoreDemoMode;
import com.ruoyi.system.fixed.dict.YesOrNo;
import com.ruoyi.system.permission.PermissionUtils;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.validator.LongId;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * 会员个人中心
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account/contribute")
public class MemberContributeApiController extends BaseRestController implements ApplicationContextAware {

	private final IContentService contentService;

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final IResourceService resourceService;

	private final IArticleService articleService;

	private ApplicationContext applicationContext;

	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
	@DeleteMapping
	public R<?> deleteContribute(@RequestParam("cid") @LongId Long contentId) {
		CmsContent xContent = this.contentService.getById(contentId);
		if (xContent == null) {
			return R.fail("内容不存在");
		}
		if (!ContentStatus.isDraft(xContent.getStatus())) {
			return R.fail("只能删除待审核的初稿");
		}
		if (xContent.getContributorId() != StpMemberUtil.getLoginIdAsLong()) {
			return R.fail("内容ID错误");
		}

		IContentType contentType = ContentCoreUtils.getContentType(xContent.getContentType());
		IContent<?> content = contentType.loadContent(xContent);
		content.setOperator(StpMemberUtil.getLoginUser());
		content.delete();

		applicationContext.publishEvent(new AfterContentDeleteEvent(this, content));
		return R.ok();
	}

	/**
	 * 投稿
	 */
	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
	@PostMapping
	public R<?> articleContribute(@RequestBody @Validated ArticleContributeDTO dto) {
		CmsCatalog catalog = this.catalogService.getCatalog(dto.getCatalogId());
		if (catalog == null) {
			return R.fail("参数`catalogId`错误：" + dto.getCatalogId());
		}
		if (!EnableContributeProperty.getValue(catalog.getConfigProps())) {
			return R.fail("参数`catalogId`异常：" + dto.getCatalogId());
		}
		LoginUser loginUser = StpMemberUtil.getLoginUser();
		if (IdUtils.validate(dto.getContentId())) {
			CmsContent cmsContent = this.contentService.getById(dto.getContentId());
			if (!loginUser.getUserId().equals(cmsContent.getContributorId())) {
				return R.fail("内容ID错误");
			}
			if (!ContentStatus.isDraft(cmsContent.getStatus())) {
				return R.fail("只能编辑待审核的初稿文章");
			}
			cmsContent.setTitle(dto.getTitle());
			cmsContent.setSummary(dto.getSummary());
			cmsContent.setLogo(dto.getLogo());
			cmsContent.setTags(dto.getTags().toArray(String[]::new));
			// 重置发布状态
			cmsContent.setStatus(ContentStatus.DRAFT);
			cmsContent.updateBy(loginUser.getUsername());
			if (!dto.getCatalogId().equals(cmsContent.getCatalogId())) {
				CmsCatalog fromCatalog = this.catalogService.getCatalog(cmsContent.getCatalogId());
				CmsCatalog toCatalog = this.catalogService.getCatalog(dto.getCatalogId());
				cmsContent.setCatalogId(toCatalog.getCatalogId());
				cmsContent.setCatalogAncestors(toCatalog.getAncestors());
				cmsContent.setTopCatalog(CatalogUtils.getTopCatalog(toCatalog));
				cmsContent.setSortFlag(SortUtils.getDefaultSortValue());
				// 目标栏目内容数量+1
				toCatalog.setContentCount(toCatalog.getContentCount() + 1);
				this.catalogService.updateById(toCatalog);
				// 源栏目内容数量-1
				fromCatalog.setContentCount(fromCatalog.getContentCount() - 1);
				this.catalogService.updateById(fromCatalog);
			}
			CmsArticleDetail articleDetail = this.articleService.getById(cmsContent.getContentId());
			articleDetail.setContentHtml(dto.getContentHtml());

			ArticleContent content = new ArticleContent();
			content.setContentEntity(cmsContent);
			content.setExtendEntity(articleDetail);
			content.setOperator(loginUser);
			applicationContext.publishEvent(new BeforeContentSaveEvent(this, content));
			content.save();
			applicationContext.publishEvent(new AfterContentSaveEvent(this, content));
		} else {
			CmsContent contentEntity = new CmsContent();
			contentEntity.setContentType(ArticleContentType.ID);
			contentEntity.setContentId(IdUtils.getSnowflakeId());
			contentEntity.setCatalogId(dto.getCatalogId());
			contentEntity.setTitle(dto.getTitle());
			contentEntity.setSummary(dto.getSummary());
			contentEntity.setSiteId(catalog.getSiteId());
			contentEntity.setLinkFlag(YesOrNo.NO);
			contentEntity.setIsLock(YesOrNo.NO);
			if (StringUtils.isNotEmpty(dto.getLogo()) && InternalUrlUtils.isInternalUrl(dto.getLogo())) {
				contentEntity.setLogo(dto.getLogo());
			}
			if (StringUtils.isNotEmpty(dto.getTags())) {
				contentEntity.setTags(dto.getTags().toArray(String[]::new));
			}
			contentEntity.setContributorId(StpMemberUtil.getLoginIdAsLong());

			CmsArticleDetail extendEntity = new CmsArticleDetail();
			extendEntity.setContentId(contentEntity.getContentId());
			extendEntity.setSiteId(contentEntity.getSiteId());
			extendEntity.setContentHtml(dto.getContentHtml());
			extendEntity.setDownloadRemoteImage(YesOrNo.NO);

			ArticleContent content = new ArticleContent();
			content.setContentEntity(contentEntity);
			content.setExtendEntity(extendEntity);
			if (content.hasExtendEntity() && StringUtils.isEmpty(extendEntity.getContentHtml())) {
				throw CommonErrorCode.NOT_EMPTY.exception("contentHtml");
			}
			content.setOperator(StpMemberUtil.getLoginUser());
			applicationContext.publishEvent(new BeforeContentSaveEvent(this, content));
			content.add();
			applicationContext.publishEvent(new AfterContentSaveEvent(this, content));
		}
		return R.ok();
	}

	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
	@PostMapping("/upload_image")
	public R<?> uploadFile(@RequestParam("file") MultipartFile multipartFile,
						   @RequestParam("sid") @LongId Long siteId) throws Exception {
		Assert.notNull(multipartFile, () -> CommonErrorCode.NOT_EMPTY.exception("file"));

		CmsSite site = this.siteService.getSite(siteId);
		if (site == null) {
			return R.fail("Invalid parameter sid: "+ siteId);
		}
		ResourceUploadDTO dto = ResourceUploadDTO.builder().site(site).file(multipartFile).build();
		dto.setOperator(StpMemberUtil.getLoginUser());
		CmsResource resource = this.resourceService.addResource(dto);
		return R.ok(Map.of("url", resource.getPath(), "iurl", resource.getInternalUrl()));
	}

	/**
	 * 会员发表的内容数据
	 */
	@GetMapping("/{memberId}")
	public R<?> getMemberContentList(@PathVariable @LongId Long memberId,
									 @RequestParam(required = false, defaultValue = "16") @Min(1) Integer limit,
									 @RequestParam(required = false, defaultValue = "1") @Min(0) Long offset) {
		LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
				.eq(CmsContent::getStatus, ContentStatus.PUBLISHED)
				.eq(CmsContent::getContributorId, memberId)
				.lt(offset > 0, CmsContent::getPublishDate, LocalDateTime.ofInstant(Instant.ofEpochMilli(offset), ZoneId.systemDefault()))
				.orderByDesc(CmsContent::getPublishDate);
		Page<CmsContent> page = contentService.page(new Page<>(1, limit, false), q);
		return this.bindDataTable(page);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}

