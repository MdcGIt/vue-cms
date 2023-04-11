package com.ruoyi.cms.image.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.cms.image.domain.CmsImage;
import com.ruoyi.cms.image.mapper.CmsImageMapper;
import com.ruoyi.cms.image.service.IImageService;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.listener.event.BeforeSiteDeleteEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ImageListener {

	private final IImageService imageService;
	
	private final CmsImageMapper imageMapper;

	@EventListener
	public void beforeSiteDelete(BeforeSiteDeleteEvent event) {
		CmsSite site = event.getSite();
		int pageSize = 500;
		// 删除图集内容数据
		try {
			long total = this.imageService
					.count(new LambdaQueryWrapper<CmsImage>().eq(CmsImage::getSiteId, site.getSiteId()));
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int) (i * pageSize * 100 / total),
						"正在删除图片内容数据：" + (i * pageSize) + "/" + total);
				this.imageService.remove(new LambdaQueryWrapper<CmsImage>().eq(CmsImage::getSiteId, site.getSiteId())
						.last("limit " + pageSize));
			}
		} catch (Exception e) {
			e.printStackTrace();
			AsyncTaskManager.addErrMessage("删除图片内容错误：" + e.getMessage());
		}
		// 删除图集备份内容数据
		try {
			long total = this.imageMapper.selectBackupCountBySiteId(site.getSiteId());
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除图片内容备份数据：" + (i * pageSize) + "/" + total);
				this.imageMapper.deleteBackupBySiteId(site.getSiteId(), pageSize);
			}
		} catch (Exception e) {
			e.printStackTrace();
			AsyncTaskManager.addErrMessage("删除图片内容备份错误：" + e.getMessage());
		}
	}
}
