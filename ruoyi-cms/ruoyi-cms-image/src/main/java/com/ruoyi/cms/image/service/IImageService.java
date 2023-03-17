package com.ruoyi.cms.image.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.cms.image.domain.CmsImage;

public interface IImageService extends IService<CmsImage> {

	public List<CmsImage> getAlbumImages(Long contentId);
}
