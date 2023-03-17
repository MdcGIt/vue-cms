package com.ruoyi.cms.image;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.cms.image.domain.CmsImage;
import com.ruoyi.cms.image.domain.dto.ImageAlbumDTO;
import com.ruoyi.cms.image.domain.vo.ImageAlbumVO;
import com.ruoyi.cms.image.mapper.CmsImageMapper;
import com.ruoyi.cms.image.template.ImageTemplateType;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IContent;
import com.ruoyi.contentcore.core.IContentType;
import com.ruoyi.contentcore.core.IPublishPipeProp.PublishPipePropUseType;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.CmsPublishPipe;
import com.ruoyi.contentcore.domain.dto.PublishPipeProp;
import com.ruoyi.contentcore.domain.vo.ContentVO;
import com.ruoyi.contentcore.enums.ContentOpType;
import com.ruoyi.contentcore.fixed.dict.ContentAttribute;
import com.ruoyi.contentcore.mapper.CmsContentMapper;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.IPublishPipeService;
import com.ruoyi.contentcore.util.InternalUrlUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component(IContentType.BEAN_NAME_PREFIX + ImageContentType.ID)
@RequiredArgsConstructor
public class ImageContentType implements IContentType {

	public final static String ID = "image";

	private final CmsContentMapper contentMapper;

	private final CmsImageMapper imageMapper;

	private final ICatalogService catalogService;

	private final IPublishPipeService publishPipeService;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "CONTENTCORE.CONTENTTYPE." + ID;
	}

	@Override
	public int getOrder() {
		return 2;
	}

	@Override
	public String getComponent() {
		return "cms/imageAlbum/editor";
	}

	@Override
	public String getTemplateType() {
		return ImageTemplateType.TypeId;
	}

	@Override
	public IContent<?> loadContent(CmsContent xContent) {
		ImageContent imageContent = new ImageContent();
		imageContent.setContentEntity(xContent);
		return imageContent;
	}

	@Override
	public IContent<?> readRequest(HttpServletRequest request) throws IOException {
		ImageAlbumDTO dto = JacksonUtils.from(request.getInputStream(), ImageAlbumDTO.class);

		CmsContent contentEntity;
		if (dto.getOpType() == ContentOpType.UPDATE) {
			contentEntity = this.contentMapper.selectById(dto.getContentId());
			Assert.notNull(contentEntity,
					() -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", dto.getContentId()));
		} else {
			contentEntity = new CmsContent();
		}
		BeanUtils.copyProperties(dto, contentEntity);
		CmsCatalog catalog = this.catalogService.getCatalog(dto.getCatalogId());
		contentEntity.setSiteId(catalog.getSiteId());
		contentEntity.setAttributes(ContentAttribute.convertInt(dto.getAttributes()));

		List<CmsImage> imageList = dto.getImageList();

		ImageContent content = new ImageContent();
		content.setContentEntity(contentEntity);
		content.setExtendEntity(imageList);
		content.setParams(dto.getParams());
		return content;
	}

	@Override
	public ContentVO initEditor(Long catalogId, Long contentId) {
		CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
		Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", catalogId));
		List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(catalog.getSiteId());
		ImageAlbumVO vo;
		if (IdUtils.validate(contentId)) {
			CmsContent contentEntity = this.contentMapper.selectById(contentId);
			Assert.notNull(contentEntity, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));

			LambdaQueryWrapper<CmsImage> q = new LambdaQueryWrapper<CmsImage>().eq(CmsImage::getContentId, contentId)
					.orderByAsc(CmsImage::getSortFlag);
			List<CmsImage> list = this.imageMapper.selectList(q);
			list.forEach(img -> {
				img.setSrc(InternalUrlUtils.getActualPreviewUrl(img.getPath()));
				img.setFileSizeName(FileUtils.byteCountToDisplaySize(img.getFileSize()));
			});
			vo = ImageAlbumVO.newInstance(contentEntity, list);
			if (StringUtils.isNotEmpty(vo.getLogo())) {
				vo.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(vo.getLogo()));
			}
		} else {
			vo = new ImageAlbumVO();
			vo.setContentId(IdUtils.getSnowflakeId());
			vo.setCatalogId(catalog.getCatalogId());
			vo.setContentType(ID);
			// 发布通道初始数据
			vo.setPublishPipe(publishPipes.stream().map(CmsPublishPipe::getCode).toArray(String[]::new));
		}
		vo.setCatalogName(catalog.getName());
		// 发布通道模板数据
		List<PublishPipeProp> publishPipeProps = this.publishPipeService.getPublishPipeProps(catalog.getSiteId(), PublishPipePropUseType.Content, vo.getPublishPipeProps());
		vo.setPublishPipeTemplates(publishPipeProps);
		return vo;
	}
}
