package com.ruoyi.media;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
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
import com.ruoyi.media.domain.CmsVideo;
import com.ruoyi.media.domain.dto.VideoAlbumDTO;
import com.ruoyi.media.domain.vo.VideoAlbumVO;
import com.ruoyi.media.mapper.CmsVideoMapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component(IContentType.BEAN_NAME_PREFIX + VideoContentType.ID)
@RequiredArgsConstructor
public class VideoContentType implements IContentType {

	public final static String ID = "video";

	private final CmsContentMapper contentMapper;

	private final CmsVideoMapper videoMapper;

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
		return 4;
	}

	@Override
	public String getComponent() {
		return "cms/videoAlbum/editor";
	}

	@Override
	public IContent<?> loadContent(CmsContent xContent) {
		AudioContent imageContent = new AudioContent();
		imageContent.setContentEntity(xContent);
		return imageContent;
	}

	@Override
	public IContent<?> readRequest(HttpServletRequest request) throws IOException {
		VideoAlbumDTO dto = JacksonUtils.from(request.getInputStream(), VideoAlbumDTO.class);

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

		List<CmsVideo> videoList = dto.getVideoList();

		VideoContent content = new VideoContent();
		content.setContentEntity(contentEntity);
		content.setExtendEntity(videoList);
		content.setParams(dto.getParams());
		return content;
	}

	@Override
	public ContentVO initEditor(Long catalogId, Long contentId) {
		CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
		Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", catalogId));
		List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(catalog.getSiteId());
		VideoAlbumVO vo;
		if (IdUtils.validate(contentId)) {
			CmsContent contentEntity = this.contentMapper.selectById(contentId);
			Assert.notNull(contentEntity, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));

			List<CmsVideo> list = new LambdaQueryChainWrapper<>(this.videoMapper).eq(CmsVideo::getContentId, contentId)
					.orderByAsc(CmsVideo::getSortFlag).list();
			list.forEach(video -> {
				video.setSrc(InternalUrlUtils.getActualPreviewUrl(video.getPath()));
				video.setFileSizeName(FileUtils.byteCountToDisplaySize(video.getFileSize()));
			});
			vo = VideoAlbumVO.newInstance(contentEntity, list);
			if (StringUtils.isNotEmpty(vo.getLogo())) {
				vo.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(vo.getLogo()));
			}
		} else {
			vo = new VideoAlbumVO();
			vo.setContentId(IdUtils.getSnowflakeId());
			vo.setCatalogId(catalog.getCatalogId());
			vo.setContentType(ID);
			// 发布通道初始数据
			vo.setPublishPipe(publishPipes.stream().map(CmsPublishPipe::getCode).toArray(String[]::new));
		}
		vo.setCatalogName(catalog.getName());
		// 发布通道模板数据
		List<PublishPipeProp> publishPipeProps = this.publishPipeService.getPublishPipeProps(catalog.getSiteId(),
				PublishPipePropUseType.Content, vo.getPublishPipeProps());
		vo.setPublishPipeTemplates(publishPipeProps);
		return vo;
	}
}
