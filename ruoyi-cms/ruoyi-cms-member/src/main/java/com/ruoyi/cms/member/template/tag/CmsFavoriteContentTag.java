package com.ruoyi.cms.member.template.tag;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.cms.member.CmsMemberConstants;
import com.ruoyi.common.staticize.FreeMarkerUtils;
import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.staticize.enums.TagAttrDataType;
import com.ruoyi.common.staticize.tag.AbstractListTag;
import com.ruoyi.common.staticize.tag.TagAttr;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.dto.ContentDTO;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.member.domain.MemberFavorites;
import com.ruoyi.member.service.IMemberFavoritesService;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CmsFavoriteContentTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_favorite_content";
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

	private final IContentService contentService;

	private final IMemberFavoritesService memberFavoritesService;
	
	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr("uid", false, TagAttrDataType.INTEGER, "用户ID"));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex) throws TemplateException {
		long uid = MapUtils.getLongValue(attrs, "uid");

		Page<MemberFavorites> pageResult = this.memberFavoritesService.lambdaQuery().eq(MemberFavorites::getMemberId, uid)
				.eq(MemberFavorites::getDataType, CmsMemberConstants.MEMBER_FAVORITES_DATA_TYPE)
				.orderByDesc(MemberFavorites::getLogId)
				.page(new Page<>(pageIndex, size, page));
		if (pageResult.getRecords().isEmpty()) {
			return TagPageData.of(List.of(), pageResult.getTotal());
		}

		List<Long> contentIds = pageResult.getRecords().stream().map(MemberFavorites::getDataId).toList();
		List<CmsContent> contents = this.contentService.listByIds(contentIds);

		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
		List<ContentDTO> list = contents.stream().map(c -> {
			ContentDTO dto = ContentDTO.newInstance(c);
			dto.setLink(this.contentService.getContentLink(c, 1, context.getPublishPipeCode(), context.isPreview()));
			dto.setLogoSrc(InternalUrlUtils.getActualUrl(c.getLogo(), context.getPublishPipeCode(), context.isPreview()));
			return dto;
		}).toList();
		return TagPageData.of(list, pageResult.getTotal());
	}

	@Override
	public String getTagName() {
		return TAG_NAME;
	}

	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public String getDescription() {
		return DESC;
	}
}
