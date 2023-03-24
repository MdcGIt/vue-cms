package com.ruoyi.media.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.media.domain.CmsAudio;
import com.ruoyi.media.mapper.CmsAudioMapper;
import com.ruoyi.media.service.IAudioService;
import com.ruoyi.media.util.MediaUtils;

import lombok.RequiredArgsConstructor;
import ws.schild.jave.info.MultimediaInfo;

@RequiredArgsConstructor
@Service
public class AudioServiceImpl extends ServiceImpl<CmsAudioMapper, CmsAudio> implements IAudioService {
	
	@Override
	public List<CmsAudio> getAlbumAudioList(Long contentId) {
		return this.lambdaQuery().eq(CmsAudio::getContentId, contentId).list();
	}
	
	/**
	 * 处理音频信息
	 * 
	 * @param audio
	 */
	@Override
	public void progressAudioInfo(CmsAudio audio) {
		audio.setDuration(-1L);
		audio.setChannels(-1);
		audio.setBitRate(-1);
		audio.setSamplingRate(-1);
		String url = InternalUrlUtils.getActualPreviewUrl(audio.getPath());
		MultimediaInfo multimediaInfo = MediaUtils.getMultimediaInfo(url);
		if (Objects.nonNull(multimediaInfo)) {
			audio.setFormat(multimediaInfo.getFormat());
			audio.setDuration(multimediaInfo.getDuration());
			audio.setDecoder(multimediaInfo.getAudio().getDecoder());
			audio.setChannels(multimediaInfo.getAudio().getChannels());
			audio.setBitRate(multimediaInfo.getAudio().getBitRate());
			audio.setSamplingRate(multimediaInfo.getAudio().getSamplingRate());
		}
	}
}
