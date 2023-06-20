package com.ruoyi.media.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.media.domain.CmsVideo;

import java.util.List;

public interface IVideoService extends IService<CmsVideo> {

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
	 * @param video
	 */
	void progressVideoInfo(CmsVideo video);
}
