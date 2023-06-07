package com.ruoyi.contentcore.template.func;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.ruoyi.common.staticize.FreeMarkerUtils;
import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.staticize.func.AbstractFunc;
import com.ruoyi.common.storage.local.LocalFileStorageType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.InternalURL;
import com.ruoyi.contentcore.domain.CmsResource;
import com.ruoyi.contentcore.service.IResourceService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.contentcore.util.SiteUtils;

import freemarker.core.Environment;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;

/**
 * Freemarker模板自定义函数：生成图片缩略图
 */
@Component
@RequiredArgsConstructor
public class ImageSizeFunction extends AbstractFunc {

	private static final String FUNC_NAME = "imageSize";
	
	private static final String DESC = "{FREEMARKER.FUNC.DESC." + FUNC_NAME + "}";

	private final ISiteService siteService;

	private final IResourceService resourceService;

	@Override
	public String getFuncName() {
		return FUNC_NAME;
	}

	@Override
	public String getDesc() {
		return DESC;
	}

	@Override
	public Object exec0(Object... args) throws TemplateModelException {
		if (args.length != 3) {
			return StringUtils.EMPTY;
		}
		String iurl = ((SimpleScalar) args[0]).getAsString();
		int width = ((SimpleNumber) args[1]).getAsNumber().intValue();
		int height = ((SimpleNumber) args[2]).getAsNumber().intValue();
		if (width <= 0 || width >= 6144 || height <= 0 || height >= 6144) {
			throw new TemplateModelException("Function[imageSize]: make sure the width/height is between 0 - 6144.");
		}
		if (!InternalUrlUtils.isInternalUrl(iurl)) {
			return iurl; // 非内部链接直接返回
		}
		TemplateContext context = FreeMarkerUtils.getTemplateContext(Environment.getCurrentEnvironment());
		InternalURL internalUrl = InternalUrlUtils.parseInternalUrl(iurl);
		String actualUrl = InternalUrlUtils.getActualUrl(internalUrl, context.getPublishPipeCode(),
				context.isPreview());
		try {
			CmsResource resource = this.resourceService.getById(internalUrl.getId());
			// 仅支持本地图片
			if (Objects.nonNull(resource) && LocalFileStorageType.TYPE.equals(resource.getStorageType())) {
				String siteResourceRoot = SiteUtils.getSiteResourceRoot(siteService.getSite(resource.getSiteId()));
				String destPath = StringUtils.substringBeforeLast(resource.getPath(), ".") + "_" + width + "x" + height
						+ "." + StringUtils.substringAfterLast(resource.getPath(), ".");
				Thumbnails.of(siteResourceRoot + resource.getPath()).size(width, height)
						.toFile(siteResourceRoot + destPath);
				return StringUtils.substringBeforeLast(actualUrl, ".") + "_" + width + "x" + height + "."
						+ StringUtils.substringAfterLast(actualUrl, ".");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return actualUrl;
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(
				new FuncArg("图片资源内部路径", FuncArgType.String, true, "仅支持处理内部资源图片(iurl://)"),
				new FuncArg("宽度", FuncArgType.Int, true, null), new FuncArg("高度", FuncArgType.Int, true, null));
	}
}
