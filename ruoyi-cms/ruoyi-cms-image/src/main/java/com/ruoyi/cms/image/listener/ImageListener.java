package com.ruoyi.cms.image.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.cms.image.domain.CmsImage;
import com.ruoyi.cms.image.mapper.CmsImageMapper;
import com.ruoyi.cms.image.service.IImageService;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.listener.event.BeforeSiteDeleteEvent;
import com.ruoyi.contentcore.listener.event.SiteExportEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class ImageListener {

	private final CmsImageMapper imageMapper;

	private final IImageService imageService;

	@EventListener
	public void beforeSiteDelete(BeforeSiteDeleteEvent event) {
		CmsSite site = event.getSite();
		int pageSize = 500;
		// 删除图集内容数据
		try {
			long total = this.imageMapper.selectCountBySiteIdIgnoreLogicDel(site.getSiteId());
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int) (i * pageSize * 100 / total),
						"正在删除图片内容数据：" + (i * pageSize) + "/" + total);
				this.imageMapper.deleteBySiteIdIgnoreLogicDel(site.getSiteId(), pageSize);
			}
		} catch (Exception e) {
			e.printStackTrace();
			AsyncTaskManager.addErrMessage("删除图片内容错误：" + e.getMessage());
		}
	}

	@EventListener
	public void onSiteExport(SiteExportEvent event) throws IOException {
		// cms_image
		long total = imageService.count(new LambdaQueryWrapper<CmsImage>()
				.eq(CmsImage::getSiteId, event.getSite().getSiteId()));
		int pageNumber = 1;
		long offset = 0;
		long limit = 500;
		while (total > 0) {
			LambdaQueryWrapper<CmsImage> q = new LambdaQueryWrapper<CmsImage>()
					.eq(CmsImage::getSiteId, event.getSite().getSiteId())
					.gt(CmsImage::getImageId, offset)
					.orderByAsc(CmsImage::getImageId);
			Page<CmsImage> page = imageService.page(new Page<>(1, limit, false), q);
			String json = JacksonUtils.to(page.getRecords());
			event.getZipBuilder().add(json.getBytes(StandardCharsets.UTF_8))
					.path("db/" + CmsImage.TABLE_NAME + "/page" + pageNumber + ".json")
					.save();
			offset = page.getRecords().get(page.getRecords().size() - 1).getImageId();
			total -= page.getRecords().size();
		}
	}
}
