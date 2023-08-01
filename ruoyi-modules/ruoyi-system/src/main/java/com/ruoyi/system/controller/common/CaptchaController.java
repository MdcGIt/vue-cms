package com.ruoyi.system.controller.common;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.code.kaptcha.Producer;
import com.ruoyi.common.captcha.CaptchaType;
import com.ruoyi.common.config.CaptchaConfig;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.GlobalException;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.system.SysConstants;
import com.ruoyi.system.config.properties.SysProperties;
import com.ruoyi.system.domain.vo.ImageCaptchaVO;
import com.ruoyi.system.exception.SysErrorCode;
import com.ruoyi.system.fixed.config.SysCaptchaEnable;

import lombok.RequiredArgsConstructor;

/**
 * 验证码操作处理
 */
@RestController
@RequiredArgsConstructor
public class CaptchaController {

	private final Map<String, Producer> captchaProducers;

	private final RedisCache redisCache;

	private final SysProperties properties;

	/**
	 * 生成验证码
	 */
	@GetMapping("/captchaImage")
	public R<?> getCode(HttpServletResponse response) throws IOException {
		boolean captchaEnabled = SysCaptchaEnable.isEnable();
		if (!captchaEnabled) {
			return R.ok(ImageCaptchaVO.builder().captchaEnabled(false).build());
		}

		// 保存验证码信息
		String uuid = IdUtils.simpleUUID();
		String verifyKey = SysConstants.CAPTCHA_CODE_KEY + uuid;

		String capStr = null, code = null;
		BufferedImage image = null;

		Producer captchaProducer = captchaProducers.get(CaptchaConfig.BEAN_PREFIX + this.properties.getCaptchaType());
		Assert.notNull(captchaProducer, () -> SysErrorCode.CAPTCHA_CONFIG_ERR.exception(this.properties.getCaptchaType()));
		// 生成验证码
		String captchaType = properties.getCaptchaType();
		if (CaptchaType.MATH.equals(captchaType)) {
			String capText = captchaProducer.createText();
			capStr = capText.substring(0, capText.lastIndexOf("@"));
			code = capText.substring(capText.lastIndexOf("@") + 1);
			image = captchaProducer.createImage(capStr);
		} else if (CaptchaType.CHAR.equals(captchaType)) {
			capStr = code = captchaProducer.createText();
			image = captchaProducer.createImage(capStr);
		} else {
			throw new GlobalException("Unkown captcha type: " + captchaType);
		}

		redisCache.setCacheObject(verifyKey, code, SysConstants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
		// 转换流信息写出
		FastByteArrayOutputStream os = new FastByteArrayOutputStream();
		try {
			ImageIO.write(image, "jpg", os);
		} catch (IOException e) {
			return R.fail(e.getMessage());
		}
		ImageCaptchaVO vo = ImageCaptchaVO.builder().captchaEnabled(captchaEnabled).uuid(uuid)
				.img(Base64.getEncoder().encodeToString(os.toByteArray())).build();
		return R.ok(vo);
	}
}
