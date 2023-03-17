package com.ruoyi.contentcore.core.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import javax.imageio.ImageIO;

import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IResourceType;
import com.ruoyi.contentcore.domain.CmsResource;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.enums.WatermarkerPosition;
import com.ruoyi.contentcore.properties.ImageWatermarkArgsProperty;
import com.ruoyi.contentcore.properties.ImageWatermarkArgsProperty.ImageWatermarkArgs;
import com.ruoyi.contentcore.properties.ImageWatermarkProperty;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.SiteUtils;

import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;

@RequiredArgsConstructor
@Component(IResourceType.BEAN_NAME_PREFIX + ResourceType_Image.ID)
public class ResourceType_Image implements IResourceType {

	public final static String ID = "image";

	public final static String[] SuffixArray = { "jpg", "jpeg", "gif", "png", "ico", "webp" };

	private final ISiteService siteService;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String[] getUsableSuffix() {
		return SuffixArray;
	}
	
	public static boolean isImage(String path) {
		String ext = FileNameUtils.getExtension(path);
		return Objects.nonNull(path) && ArrayUtils.contains(SuffixArray, ext);
	}

	@Override
	public byte[] process(CmsResource resource, byte[] bytes) throws IOException {
		CmsSite site = siteService.getSite(resource.getSiteId());
		// 提取图片宽高属性
		try (ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
			BufferedImage bi = ImageIO.read(is);
			resource.setWidth(bi.getWidth());
			resource.setHeight(bi.getHeight());
			// 添加水印
			if (ImageWatermarkProperty.getValue(site.getConfigProps())
					&& !"webp".equalsIgnoreCase(resource.getSuffix())) {
				// TODO webp水印支持
				ImageWatermarkArgs args = ImageWatermarkArgsProperty.getImageWatermarkArgs(site.getConfigProps());
				if (StringUtils.isNotEmpty(args.getImage())) {
					// 水印图片占比大小调整
					String siteResourceRoot = SiteUtils.getSiteResourceRoot(site);
					File file = new File(siteResourceRoot + args.getImage());
					if (file.exists()) {
						float waterremakImageWidth = bi.getWidth() * args.getRatio() * 0.01f;
						BufferedImage biWatermarkImage = ImageIO.read(file);
						biWatermarkImage = Thumbnails.of(biWatermarkImage)
								.scale(waterremakImageWidth / biWatermarkImage.getWidth()).asBufferedImage();
						try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
							// 添加水印
							Thumbnails.of(bi)
									.watermark(WatermarkerPosition.valueOf(args.getPosition()).position(),
											biWatermarkImage, args.getOpacity())
									.scale(1f).outputFormat(resource.getSuffix()).toOutputStream(os);
							bytes = os.toByteArray();
						}
					}
				}
			}
		}
		resource.setFileSize((long) bytes.length);
		return bytes;
	}
}
