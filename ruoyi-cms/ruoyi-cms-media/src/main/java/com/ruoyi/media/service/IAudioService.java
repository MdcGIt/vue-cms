package com.ruoyi.media.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.media.domain.CmsAudio;

public interface IAudioService extends IService<CmsAudio> {

	/**
	 * 获取音频集音频列表数据
	 * 
	 * @param contentId
	 * @return
	 */
	public List<CmsAudio> getAlbumAudioList(Long contentId);

	/**
	 * 处理音频信息
	 * 
	 * @param audio
	 */
	void progressAudioInfo(CmsAudio audio);
}