package com.ruoyi.media.domain.dto;

import java.util.List;

import org.springframework.beans.BeanUtils;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.dto.ContentDTO;
import com.ruoyi.contentcore.fixed.dict.ContentAttribute;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.media.domain.CmsAudio;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AudioAlbumDTO extends ContentDTO {

	private List<CmsAudio> audioList;

	public static AudioAlbumDTO newInstance(CmsContent content, List<CmsAudio> audioList) {
		AudioAlbumDTO dto = new AudioAlbumDTO();
		BeanUtils.copyProperties(content, dto);
		dto.setAttributes(ContentAttribute.convertStr(content.getAttributes()));
		if (StringUtils.isNotEmpty(dto.getLogo())) {
			dto.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(dto.getLogo()));
		}
    	dto.setAudioList(audioList);
		return dto;
	}
}
