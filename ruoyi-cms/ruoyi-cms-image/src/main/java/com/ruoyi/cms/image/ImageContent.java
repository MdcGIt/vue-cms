package com.ruoyi.cms.image;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.cms.image.domain.CmsImage;
import com.ruoyi.cms.image.service.IImageService;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileExUtils;
import com.ruoyi.contentcore.core.AbstractContent;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.enums.ContentCopyType;

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

		if (this.getContentEntity().isLinkContent()
				|| ContentCopyType.isMapping(this.getContentEntity().getCopyType())) {
			return this.getContentEntity().getContentId();
		}
		List<CmsImage> images = this.getExtendEntity();

		if (StringUtils.isNotEmpty(images)) {
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
		if (this.getContentEntity().isLinkContent()
				|| ContentCopyType.isMapping(this.getContentEntity().getCopyType())) {
			this.getImageService().remove(new LambdaQueryWrapper<CmsImage>().eq(CmsImage::getContentId,
					this.getContentEntity().getContentId()));
			return this.getContentEntity().getContentId();
		}
		// 图片数处理
		List<CmsImage> imageList = this.getExtendEntity();
		// 先删除数据
		List<Long> deleteImageIds = imageList.stream().filter(img -> IdUtils.validate(img.getImageId()))
				.map(CmsImage::getImageId).toList();
		if (deleteImageIds.size() > 0) {
			this.getImageService()
					.remove(new LambdaQueryWrapper<CmsImage>()
							.eq(CmsImage::getContentId, this.getContentEntity().getContentId())
							.notIn(CmsImage::getImageId, deleteImageIds));
		}
		for (int i = 0; i < images.size(); i++) {
			CmsImage image = images.get(i);
			if (IdUtils.validate(image.getImageId())) {
				image.setSortFlag(i);
				image.updateBy(this.getOperator().getUsername());
				this.getImageService().updateById(image);
			} else {
				image.setImageId(IdUtils.getSnowflakeId());
				image.setContentId(this.getContentEntity().getContentId());
				image.setImageType(FileExUtils.getExtension(image.getPath()));
				image.setSortFlag(i);
				image.createBy(this.getOperator().getUsername());
				this.getImageService().save(image);
			}
		}
		return this.getContentEntity().getContentId();
	}

	@Override
	public void delete() {
		super.delete();
		this.getImageService().remove(
				new LambdaQueryWrapper<CmsImage>().eq(CmsImage::getContentId, this.getContentEntity().getContentId()));
	}

	@Override
	public void copyTo(CmsCatalog toCatalog, Integer copyType) {
		super.copyTo(toCatalog, copyType);

		Long newContentId = (Long) this.getParams().get("NewContentId");
		List<CmsImage> albumImages = this.getImageService().getAlbumImages(this.getContentEntity().getContentId());
		for (CmsImage image : albumImages) {
			image.createBy(this.getOperator().getUsername());
			image.setImageId(IdUtils.getSnowflakeId());
			image.setContentId(newContentId);
			this.getImageService().save(image);
		}
	}

	private IImageService getImageService() {
		if (this.imageService == null) {
			this.imageService = SpringUtils.getBean(IImageService.class);
		}
		return this.imageService;
	}
}