package com.ruoyi.media.domain.vo;

import java.util.List;

import org.springframework.beans.BeanUtils;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.vo.ContentVO;
import com.ruoyi.contentcore.fixed.dict.ContentAttribute;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.media.domain.CmsAudio;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AudioAlbumVO extends ContentVO {

	private List<CmsAudio> audioList;

	public static AudioAlbumVO newInstance(CmsContent content, List<CmsAudio> audioList) {
		AudioAlbumVO dto = new AudioAlbumVO();
		BeanUtils.copyProperties(content, dto);
		dto.setAttributes(ContentAttribute.convertStr(content.getAttributes()));
		if (StringUtils.isNotEmpty(dto.getLogo())) {
			dto.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(dto.getLogo()));
		}
    	dto.setAudioList(audioList);
		return dto;
	}
}
