package com.ruoyi.cms.image.domain.dto;

import java.util.List;

import org.springframework.beans.BeanUtils;

import com.ruoyi.cms.image.domain.CmsImage;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.dto.ContentDTO;
import com.ruoyi.contentcore.fixed.dict.ContentAttribute;
import com.ruoyi.contentcore.util.InternalUrlUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageAlbumDTO extends ContentDTO {

	private List<CmsImage> imageList;

	public static ImageAlbumDTO newInstance(CmsContent content, List<CmsImage> images) {
		ImageAlbumDTO dto = new ImageAlbumDTO();
		BeanUtils.copyProperties(content, dto);
		dto.setAttributes(ContentAttribute.convertStr(content.getAttributes()));
		if (StringUtils.isNotEmpty(dto.getLogo())) {
			dto.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(dto.getLogo()));
		}
    	dto.setImageList(images);
		return dto;
	}
}
