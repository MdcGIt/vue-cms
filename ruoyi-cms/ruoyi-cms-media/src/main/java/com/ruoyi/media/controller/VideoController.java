package com.ruoyi.media.controller;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.contentcore.core.InternalURL;
import com.ruoyi.contentcore.domain.CmsResource;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.media.domain.dto.VideoScreenshotDTO;
import com.ruoyi.media.service.IVideoService;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.schild.jave.EncoderException;

import java.io.IOException;
import java.util.Objects;

/**
 * <p>
 * 视频内容前端控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequestMapping("/cms/video")
@RequiredArgsConstructor
public class VideoController extends BaseRestController {

	private final ISiteService siteService;

	private final IVideoService videoService;

	@Priv(type = AdminUserType.TYPE)
	@PostMapping("/screenshot")
	public R<?> screenshot(@RequestBody  @Validated VideoScreenshotDTO dto, HttpServletRequest request)
			throws EncoderException, IOException {
		CmsSite site = this.siteService.getCurrentSite(request);
		InternalURL internalURL = InternalUrlUtils.parseInternalUrl(dto.getPath());
		if (Objects.nonNull(internalURL)) {
			dto.setPath(internalURL.getPath());
		}
		CmsResource cmsResource = this.videoService.videoScreenshot(site, dto.getPath(),
				dto.getTimestamp(), StpAdminUtil.getLoginUser());
		return R.ok(cmsResource);
	}
}
