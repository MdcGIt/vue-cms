package com.ruoyi.media.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.listener.event.BeforeSiteDeleteEvent;
import com.ruoyi.media.domain.CmsAudio;
import com.ruoyi.media.domain.CmsVideo;
import com.ruoyi.media.mapper.CmsAudioMapper;
import com.ruoyi.media.mapper.CmsVideoMapper;
import com.ruoyi.media.service.IAudioService;
import com.ruoyi.media.service.IVideoService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MediaListener {

	private final IAudioService audioService;
	
	private final CmsAudioMapper audioMapper;

	private final IVideoService videoService;
	
	private final CmsVideoMapper videoMapper;

	@EventListener
	public void beforeSiteDelete(BeforeSiteDeleteEvent event) {
		CmsSite site = event.getSite();
		int pageSize = 500;
		// 删除音频内容数据
		try {
			long total = this.audioService
					.count(new LambdaQueryWrapper<CmsAudio>().eq(CmsAudio::getSiteId, site.getSiteId()));
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int) (i * pageSize * 100 / total),
						"正在删除音频内容数据：" + (i * pageSize) + "/" + total);
				this.audioService.remove(new LambdaQueryWrapper<CmsAudio>().eq(CmsAudio::getSiteId, site.getSiteId())
						.last("limit " + pageSize));
			}
		} catch (Exception e) {
			e.printStackTrace();
			AsyncTaskManager.addErrMessage("删除音频内容错误：" + e.getMessage());
		}
		// 删除音频内容备份数据
		try {
			long total = this.audioMapper.selectBackupCountBySiteId(site.getSiteId());
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除音频内容备份数据：" + (i * pageSize) + "/" + total);
				this.audioMapper.deleteBackupBySiteId(site.getSiteId(), pageSize);
			}
		} catch (Exception e) {
			e.printStackTrace();
			AsyncTaskManager.addErrMessage("删除音频内容备份错误：" + e.getMessage());
		}
		// 删除视频内容数据
		try {
			long total = this.videoService
					.count(new LambdaQueryWrapper<CmsVideo>().eq(CmsVideo::getSiteId, site.getSiteId()));
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int) (i * pageSize * 100 / total),
						"正在删除视频内容数据：" + (i * pageSize) + "/" + total);
				this.videoService.remove(new LambdaQueryWrapper<CmsVideo>().eq(CmsVideo::getSiteId, site.getSiteId())
						.last("limit " + pageSize));
			}
		} catch (Exception e) {
			e.printStackTrace();
			AsyncTaskManager.addErrMessage("删除视频内容错误：" + e.getMessage());
		}
		// 删除视频内容备份数据
		try {
			long total = this.videoMapper.selectBackupCountBySiteId(site.getSiteId());
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除视频内容备份数据：" + (i * pageSize) + "/" + total);
				this.videoMapper.deleteBackupBySiteId(site.getSiteId(), pageSize);
			}
		} catch (Exception e) {
			e.printStackTrace();
			AsyncTaskManager.addErrMessage("删除视频内容备份错误：" + e.getMessage());
		}
	}
}
