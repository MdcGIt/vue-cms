package com.ruoyi.media.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.media.domain.CmsVideo;
import com.ruoyi.media.mapper.CmsVideoMapper;
import com.ruoyi.media.service.IVideoService;
import com.ruoyi.media.util.MediaUtils;

import ws.schild.jave.info.MultimediaInfo;

@Service
public class VideoServiceImpl extends ServiceImpl<CmsVideoMapper, CmsVideo> implements IVideoService {

	@Override
	public List<CmsVideo> getAlbumVideoList(Long contentId) {
		return this.lambdaQuery().eq(CmsVideo::getContentId, contentId).list();
	}
	
	/**
	 * 处理视频信息
	 * 
	 * @param video
	 */
	@Override
	public void progressVideoInfo(CmsVideo video) {
		video.setDuration(-1L);
		video.setWidth(0);
		video.setHeight(0);
		video.setBitRate(-1);
		video.setFrameRate(-1);
		String url = InternalUrlUtils.getActualPreviewUrl(video.getPath());
		MultimediaInfo multimediaInfo = MediaUtils.getMultimediaInfo(url);
		if (Objects.nonNull(multimediaInfo)) {
			video.setFormat(multimediaInfo.getFormat());
			video.setDuration(multimediaInfo.getDuration());
			video.setWidth(multimediaInfo.getVideo().getSize().getWidth());
			video.setHeight(multimediaInfo.getVideo().getSize().getHeight());
			video.setDecoder(multimediaInfo.getVideo().getDecoder());
			video.setBitRate(multimediaInfo.getVideo().getBitRate());
			video.setFrameRate(Float.valueOf(multimediaInfo.getVideo().getFrameRate()).intValue());
		}
	}
}
