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
import com.ruoyi.media.domain.CmsAudio;
import com.ruoyi.media.domain.dto.AudioAlbumDTO;
import com.ruoyi.media.domain.vo.AudioAlbumVO;
import com.ruoyi.media.mapper.CmsAudioMapper;
import com.ruoyi.media.service.IAudioService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component(IContentType.BEAN_NAME_PREFIX + AudioContentType.ID)
@RequiredArgsConstructor
public class AudioContentType implements IContentType {

	public final static String ID = "audio";

	private final CmsContentMapper contentMapper;

	private final CmsAudioMapper audioMapper;
	
	private final IAudioService audioService;

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
		return 3;
	}

	@Override
	public String getComponent() {
		return "cms/audioAlbum/editor";
	}

	@Override
	public IContent<?> loadContent(CmsContent xContent) {
		AudioContent imageContent = new AudioContent();
		imageContent.setContentEntity(xContent);
		return imageContent;
	}

	@Override
	public IContent<?> readRequest(HttpServletRequest request) throws IOException {
		AudioAlbumDTO dto = JacksonUtils.from(request.getInputStream(), AudioAlbumDTO.class);

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

		List<CmsAudio> audioList = dto.getAudioList();

		AudioContent content = new AudioContent();
		content.setContentEntity(contentEntity);
		content.setExtendEntity(audioList);
		content.setParams(dto.getParams());
		return content;
	}

	@Override
	public ContentVO initEditor(Long catalogId, Long contentId) {
		CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
		Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", catalogId));
		List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(catalog.getSiteId());
		AudioAlbumVO vo;
		if (IdUtils.validate(contentId)) {
			CmsContent contentEntity = this.contentMapper.selectById(contentId);
			Assert.notNull(contentEntity, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));

			List<CmsAudio> list = new LambdaQueryChainWrapper<>(this.audioMapper).eq(CmsAudio::getContentId, contentId)
					.orderByAsc(CmsAudio::getSortFlag).list();
			list.forEach(audio -> {
				audio.setSrc(InternalUrlUtils.getActualPreviewUrl(audio.getPath()));
				audio.setFileSizeName(FileUtils.byteCountToDisplaySize(audio.getFileSize()));
			});
			vo = AudioAlbumVO.newInstance(contentEntity, list);
			if (StringUtils.isNotEmpty(vo.getLogo())) {
				vo.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(vo.getLogo()));
			}
		} else {
			vo = new AudioAlbumVO();
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

	@Override
	public void recover(Long contentId) {
		this.audioMapper.selectBackupIdsByContentId(contentId).forEach(backupId -> {
			this.audioService.recover(backupId, CmsAudio.class);
		});
	}
	
	@Override
	public void deleteBackups(Long contentId) {
		List<Long> backupIds = this.audioMapper.selectBackupIdsByContentId(contentId);
		this.audioService.deleteBackups(backupIds, CmsAudio.class);
	}
}
