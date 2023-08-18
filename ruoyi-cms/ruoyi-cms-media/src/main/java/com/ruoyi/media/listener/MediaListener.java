package com.ruoyi.media.listener;

import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.listener.event.BeforeSiteDeleteEvent;
import com.ruoyi.media.mapper.CmsAudioMapper;
import com.ruoyi.media.mapper.CmsVideoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MediaListener {

	private final CmsAudioMapper audioMapper;

	private final CmsVideoMapper videoMapper;

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
}
