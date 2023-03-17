package com.ruoyi.cms.image;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.cms.image.domain.CmsImage;
import com.ruoyi.cms.image.service.IImageService;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileExUtils;
import com.ruoyi.contentcore.core.AbstractContent;
import com.ruoyi.contentcore.core.InternalURL;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.util.InternalUrlUtils;

public class ImageContent extends AbstractContent<List<CmsImage>> {

	private IImageService imageService;

	@Override
	public String getContentType() {
		return ImageContentType.ID;
	}

	@Override
	public Long add() {
		super.add();
		this.getContentService().save(this.getContentEntity());
		
		List<CmsImage> images = this.getExtendEntity();
		
		if (!this.getContentEntity().isLinkContent() && StringUtils.isNotEmpty(images)) {
			for (int i = 0; i < images.size(); i++) {
				CmsImage cmsImage = images.get(i);
				cmsImage.setImageId(IdUtils.getSnowflakeId());
				cmsImage.setContentId(this.getContentEntity().getContentId());
				cmsImage.setImageType(FileExUtils.getExtension(cmsImage.getPath()));
				cmsImage.setSortFlag(i);
				cmsImage.createBy(this.getOperator().getUsername());
			}
			this.getImageService().saveBatch(images);
		}
		return this.getContentEntity().getContentId();
	}

	@Override
	public Long save() {
		super.save();
		this.getContentService().updateById(this.getContentEntity());
		// 处理图片
		List<CmsImage> images = this.getExtendEntity();
		if (this.getContentEntity().isLinkContent()) {
			this.getImageService().remove(new LambdaQueryWrapper<CmsImage>().eq(CmsImage::getContentId, this.getContentEntity().getContentId()));
		} else if (images != null && images.size() > 0) {
			long[] oldImageIds = images.stream().filter(item -> item.getImageId() != null && item.getImageId() > 0)
					.mapToLong(item -> item.getImageId()).toArray();
			List<CmsImage> list = this.getImageService().list(new LambdaQueryWrapper<CmsImage>()
					.eq(CmsImage::getContentId, this.getContentEntity().getContentId()));
			for (CmsImage dbImg : list) {
				if (!ArrayUtils.contains(oldImageIds, dbImg.getImageId().longValue())) {
					this.getImageService().removeById(dbImg);
				}
			}
			for (int i = 0; i < images.size(); i++) {
				CmsImage cmsImage = images.get(i);
				if (cmsImage.getImageId() == null || cmsImage.getImageId() == 0) {
					cmsImage.setImageId(IdUtils.getSnowflakeId());
					cmsImage.setContentId(this.getContentEntity().getContentId());
					String path = cmsImage.getPath();
					if (InternalUrlUtils.isInternalUrl(path)) {
						InternalURL internalUrl = InternalUrlUtils.parseInternalUrl(cmsImage.getPath());
						path = internalUrl.getPath();
					}
					String fileType = FileExUtils.getExtension(path);
					cmsImage.setImageType(fileType);
					cmsImage.setSortFlag(i);
					cmsImage.createBy(this.getOperator().getUsername());
					this.getImageService().save(cmsImage);
				} else {
					cmsImage.setSortFlag(i);
					cmsImage.updateBy(this.getOperator().getUsername());
					this.getImageService().updateById(cmsImage);
				}
			}
		}
		return this.getContentEntity().getContentId();
	}

	@Override
	public void delete() {
		super.delete();
		IImageService imageService = this.getImageService();
		List<CmsImage> albumImages = imageService.getAlbumImages(this.getContentEntity().getContentId());
		this.getImageService().removeByIds(albumImages.stream().map(CmsImage::getImageId).toList());
	}

	@Override
	public void copyTo(CmsCatalog toCatalog, Integer copyType) {
		super.copyTo(toCatalog, copyType);

		Long newContentId = (Long) this.getParams().get("NewContentId");
		List<CmsImage> albumImages = this.getImageService().getAlbumImages(this.getContentEntity().getContentId());
		for (CmsImage cmsImage : albumImages) {
			cmsImage.createBy(this.getOperator().getUsername());
			cmsImage.setImageId(IdUtils.getSnowflakeId());
			cmsImage.setContentId(newContentId);
			this.getImageService().save(cmsImage);
		}
	}

	private IImageService getImageService() {
		if (this.imageService == null) {
			this.imageService = SpringUtils.getBean(IImageService.class);
		}
		return this.imageService;
	}
}
