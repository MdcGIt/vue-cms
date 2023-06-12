package com.ruoyi.cms.image.service;

import java.util.List;

import com.ruoyi.cms.image.domain.CmsImage;
import com.ruoyi.common.mybatisplus.service.IBackupService;

public interface IImageService extends IBackupService<CmsImage> {

	/**
	 * 获取图集中的图片列表
	 * 
	 * @param contentId
	 * @return
	 */
	public List<CmsImage> getAlbumImages(Long contentId);
}
