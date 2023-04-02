package com.ruoyi.media.service;

import java.util.List;

import com.ruoyi.common.mybatisplus.IBackupService;
import com.ruoyi.media.domain.CmsVideo;

public interface IVideoService extends IBackupService<CmsVideo> {

	/**
	 * 获取视频集视频列表
	 * 
	 * @param contentId
	 * @return
	 */
	public List<CmsVideo> getAlbumVideoList(Long contentId);

	/**
	 * 处理视频信息
	 * 
	 * @param audio
	 */
	void progressVideoInfo(CmsVideo video);
}
