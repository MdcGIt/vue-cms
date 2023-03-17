package com.ruoyi.cms.image.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.cms.image.domain.CmsImage;
import com.ruoyi.cms.image.mapper.CmsImageMapper;
import com.ruoyi.cms.image.service.IImageService;

@Service
public class ImageServiceImpl extends ServiceImpl<CmsImageMapper, CmsImage> implements IImageService {

	public List<CmsImage> getAlbumImages(Long contentId) {
		LambdaQueryWrapper<CmsImage> q = new LambdaQueryWrapper<>();
		q.eq(CmsImage::getContentId, contentId);
		return this.list(q);
	}
}
