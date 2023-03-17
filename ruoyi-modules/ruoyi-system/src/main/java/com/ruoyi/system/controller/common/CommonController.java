package com.ruoyi.system.controller.common;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileExUtils;
import com.ruoyi.system.config.SystemConfig;
import com.ruoyi.system.fixed.config.SysUploadSizeLimit;
import com.ruoyi.system.fixed.config.SysUploadTypeLimit;
import com.ruoyi.system.security.SaAdminCheckLogin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 通用请求处理
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/common")
public class CommonController extends BaseRestController {

	private static final Logger log = LoggerFactory.getLogger(CommonController.class);

	/**
	 * 通用上传请求（单个）
	 */
	@SaAdminCheckLogin
	@PostMapping("/upload")
	public R<?> uploadFile(MultipartFile file) throws Exception {
		try {
			if (Objects.isNull(file) || file.isEmpty()) {
				return R.fail("Upload multipart file is empty.");
			}
			String ext = FileExUtils.getExtension(file.getOriginalFilename());
			// 校验上传文件大小及类型
			SysUploadSizeLimit.check(file.getSize());
			SysUploadTypeLimit.check(ext);
			// 默认按日期目录存储
			String path = DateUtils.datePath() + StringUtils.SLASH + IdUtils.simpleUUID() + StringUtils.DOT + ext;
			// 写入文件
			FileUtils.writeByteArrayToFile(new File(SystemConfig.getUploadDir() + path), file.getBytes());
			// 文件预览路径
			String src = SystemConfig.getResourcePrefix() + path;
			// 返回预览链接地址
			return R.ok(Map.of("fileName", src));
		} catch (Exception e) {
			return R.fail(e.getMessage());
		}
	}

	/**
	 * 本地资源通用下载
	 */
	@SaAdminCheckLogin
	@GetMapping("/download")
	public void resourceDownload(String path, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		try {
			response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
			String downloadName = StringUtils.substringAfterLast(path, "/");
			
			StringBuilder contentDispositionValue = new StringBuilder();
			contentDispositionValue.append("attachment; filename=").append(downloadName).append(";")
					.append("filename*=").append("utf-8''").append(downloadName);

			response.setHeader("Content-disposition", contentDispositionValue.toString());
			response.setHeader("download-filename", downloadName);
			
			Files.copy(Path.of(SystemConfig.getUploadDir() + path), response.getOutputStream());
		} catch (Exception e) {
			log.error("下载文件失败", e);
		}
	}
}
