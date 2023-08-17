package com.ruoyi.media.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.listener.event.BeforeSiteDeleteEvent;
import com.ruoyi.contentcore.listener.event.SiteExportEvent;
import com.ruoyi.media.domain.CmsAudio;
import com.ruoyi.media.domain.CmsVideo;
import com.ruoyi.media.mapper.CmsAudioMapper;
import com.ruoyi.media.mapper.CmsVideoMapper;
import com.ruoyi.media.service.IAudioService;
import com.ruoyi.media.service.IVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class MediaListener {

	private final CmsAudioMapper audioMapper;

	private final IAudioService audioService;

	private final CmsVideoMapper videoMapper;

	private final IVideoService videoService;

	@EventListener
	public void beforeSiteDelete(BeforeSiteDeleteEvent event) {
		CmsSite site = event.getSite();
		int pageSize = 500;
		// 删除音频内容数据
		try {
			long total = this.audioMapper.selectCountBySiteIdIgnoreLogicDel(site.getSiteId());
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int) (i * pageSize * 100 / total),
						"正在删除音频内容数据：" + (i * pageSize) + "/" + total);
				this.audioMapper.deleteBySiteIdIgnoreLogicDel(site.getSiteId(), pageSize);
			}
		} catch (Exception e) {
			e.printStackTrace();
			AsyncTaskManager.addErrMessage("删除音频内容错误：" + e.getMessage());
		}
		// 删除视频内容数据
		try {
			long total = this.videoMapper.selectCountBySiteIdIgnoreLogicDel(site.getSiteId());
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int) (i * pageSize * 100 / total),
						"正在删除视频内容数据：" + (i * pageSize) + "/" + total);
				this.videoMapper.deleteBySiteIdIgnoreLogicDel(site.getSiteId(), pageSize);
			}
		} catch (Exception e) {
			e.printStackTrace();
			AsyncTaskManager.addErrMessage("删除视频内容错误：" + e.getMessage());
		}
	}

	@EventListener
	public void onSiteExport(SiteExportEvent event) throws IOException {
		// cms_audio
		{
			long total = audioService.count(new LambdaQueryWrapper<CmsAudio>()
					.eq(CmsAudio::getSiteId, event.getSite().getSiteId()));
			int pageNumber = 1;
			long offset = 0;
			long limit = 500;
			while (total > 0) {
				LambdaQueryWrapper<CmsAudio> q = new LambdaQueryWrapper<CmsAudio>()
						.eq(CmsAudio::getSiteId, event.getSite().getSiteId())
						.gt(CmsAudio::getAudioId, offset)
						.orderByAsc(CmsAudio::getAudioId);
				Page<CmsAudio> page = audioService.page(new Page<>(1, limit, false), q);
				String json = JacksonUtils.to(page.getRecords());
				event.getZipBuilder().add(json.getBytes(StandardCharsets.UTF_8))
						.path("db/" + CmsAudio.TABLE_NAME + "/page" + pageNumber + ".json")
						.save();
				offset = page.getRecords().get(page.getRecords().size() - 1).getAudioId();
				total -= page.getRecords().size();
			}
		}
		// cms_video
		{
			long total = videoService.count(new LambdaQueryWrapper<CmsVideo>()
					.eq(CmsVideo::getSiteId, event.getSite().getSiteId()));
			int pageNumber = 1;
			long offset = 0;
			long limit = 500;
			while (total > 0) {
				LambdaQueryWrapper<CmsVideo> q = new LambdaQueryWrapper<CmsVideo>()
						.eq(CmsVideo::getSiteId, event.getSite().getSiteId())
						.gt(CmsVideo::getVideoId, offset)
						.orderByAsc(CmsVideo::getVideoId);
				Page<CmsVideo> page = videoService.page(new Page<>(1, limit, false), q);
				String json = JacksonUtils.to(page.getRecords());
				event.getZipBuilder().add(json.getBytes(StandardCharsets.UTF_8))
						.path("db/" + CmsVideo.TABLE_NAME + "/page" + pageNumber + ".json")
						.save();
				offset = page.getRecords().get(page.getRecords().size() - 1).getVideoId();
				total -= page.getRecords().size();
			}
		}
	}
}
