package com.ruoyi.contentcore.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.ruoyi.contentcore.core.IResourceStat;
import com.ruoyi.contentcore.util.ResourceUtils;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.storage.IFileStorageType;
import com.ruoyi.common.storage.StorageReadArgs;
import com.ruoyi.common.storage.StorageReadArgs.StorageReadArgsBuilder;
import com.ruoyi.common.storage.StorageWriteArgs;
import com.ruoyi.common.storage.StorageWriteArgs.StorageWriteArgsBuilder;
import com.ruoyi.common.storage.exception.StorageErrorCode;
import com.ruoyi.common.storage.local.LocalFileStorageType;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.HttpUtils;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileExUtils;
import com.ruoyi.contentcore.core.IResourceType;
import com.ruoyi.contentcore.core.impl.InternalDataType_Resource;
import com.ruoyi.contentcore.core.impl.ResourceType_Image;
import com.ruoyi.contentcore.domain.CmsResource;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.dto.ResourceUploadDTO;
import com.ruoyi.contentcore.exception.ContentCoreErrorCode;
import com.ruoyi.contentcore.mapper.CmsResourceMapper;
import com.ruoyi.contentcore.properties.FileStorageArgsProperty;
import com.ruoyi.contentcore.properties.FileStorageArgsProperty.FileStorageArgs;
import com.ruoyi.contentcore.properties.FileStorageTypeProperty;
import com.ruoyi.contentcore.service.IResourceService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.ContentCoreUtils;
import com.ruoyi.contentcore.util.SiteUtils;
import com.ruoyi.system.fixed.dict.EnableOrDisable;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl extends ServiceImpl<CmsResourceMapper, CmsResource> implements IResourceService {

	private final Map<String, IFileStorageType> fileStorageTypes;
	
	private final ISiteService siteService;

	@Override
	public CmsResource downloadImageFromUrl(String url, long siteId, String operator) throws Exception {
		if (!ServletUtils.isHttpUrl(url)) {
			throw CommonErrorCode.INVALID_REQUEST_ARG.exception("url");
		}

		String suffix = FileExUtils.getImageSuffix(url);
		IResourceType resourceType = ContentCoreUtils.getResourceType(ResourceType_Image.ID);
		if (!resourceType.check(suffix)) {
			throw ContentCoreErrorCode.UNSUPPORTED_RESOURCE_TYPE.exception(suffix);  // 不支持的图片格式
		}
		CmsSite site = siteService.getSite(siteId);

		long resourceId = IdUtils.getSnowflakeId();
		String fileName = resourceId + StringUtils.DOT + suffix;
		String dir = resourceType.getUploadPath()
				+ LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + StringUtils.SLASH;
		// 下载图片
		byte[] imageBytes = HttpUtils.syncDownload(url);
		Assert.notNull(imageBytes, () -> CommonErrorCode.REQUEST_FAILED.exception(url));
		
		CmsResource resource = new CmsResource();
		resource.setResourceId(resourceId);
		resource.setSiteId(site.getSiteId());
		resource.setResourceType(resourceType.getId());
		resource.setFileName(fileName);
		resource.setName(fileName);
		resource.setSourceUrl(url);
		resource.setPath(dir + fileName);
		resource.setStatus(EnableOrDisable.ENABLE);
		resource.setSuffix(suffix);
		resource.createBy(operator);
		// 处理资源
		this.processResource(resource, resourceType, site, imageBytes);
		return resource;
	}
	
	@Override
	public CmsResource addResource(ResourceUploadDTO dto)
			throws IOException {
		String suffix = FileExUtils.getExtension(Objects.requireNonNull(dto.getFile().getOriginalFilename()));
		IResourceType resourceType = ResourceUtils.getResourceTypeBySuffix(suffix);
		Assert.notNull(resourceType, () -> ContentCoreErrorCode.UNSUPPORTED_RESOURCE_TYPE.exception(suffix));

		CmsResource resource = new CmsResource();
		resource.setResourceId(IdUtils.getSnowflakeId());
		resource.setSiteId(dto.getSite().getSiteId());
		resource.setResourceType(resourceType.getId());
		resource.setFileName(dto.getFile().getOriginalFilename());
		resource.setName(StringUtils.isEmpty(dto.getName()) ? dto.getFile().getOriginalFilename() : dto.getName());
		resource.setSuffix(suffix);

		String siteResourceRoot = SiteUtils.getSiteResourceRoot(dto.getSite());
		String fileName = resource.getResourceId() + StringUtils.DOT + suffix;
		String dir = resourceType.getUploadPath()
				+ LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + StringUtils.SLASH;
		FileExUtils.mkdirs(siteResourceRoot + dir);
		
		resource.setPath(dir + fileName);
		resource.setStatus(EnableOrDisable.ENABLE);
		resource.createBy(dto.getOperator().getUsername());
		resource.setRemark(dto.getRemark());
		// 处理资源
		this.processResource(resource, resourceType, dto.getSite(), dto.getFile().getBytes());
		return resource;
	}
	
	@Override
	public CmsResource addBase64Image(CmsSite site, String operator, String base64Data) throws IOException {
		if (!base64Data.startsWith("data:image/")) {
			return null;
		}
		String suffix = base64Data.substring(11, base64Data.indexOf(";"));

		IResourceType resourceType = ResourceUtils.getResourceTypeBySuffix(suffix);
		Assert.notNull(resourceType, () -> ContentCoreErrorCode.UNSUPPORTED_RESOURCE_TYPE.exception(suffix));

		CmsResource resource = new CmsResource();
		resource.setResourceId(IdUtils.getSnowflakeId());
		resource.setSiteId(site.getSiteId());
		resource.setResourceType(resourceType.getId());
		resource.setFileName("base64_" + resource.getResourceId());
		resource.setName(resource.getFileName());
		resource.setSuffix(suffix);

		String siteResourceRoot = SiteUtils.getSiteResourceRoot(site);
		String fileName = resource.getResourceId() + StringUtils.DOT + suffix;
		String dir = resourceType.getUploadPath()
				+ LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + StringUtils.SLASH;
		FileExUtils.mkdirs(siteResourceRoot + dir);
		
		resource.setPath(dir + fileName);
		resource.setStatus(EnableOrDisable.ENABLE);
		resource.createBy(operator);

		String base64Str = StringUtils.substringAfter(base64Data, ",");
		byte[] imageBytes = Base64.getDecoder().decode(base64Str);
		this.processResource(resource, resourceType, site, imageBytes);
		return resource;
	}

	@Override
	public CmsResource addImageFromFile(CmsSite site, String operator, File imageFile) throws IOException {
		if (Objects.isNull(imageFile) || !imageFile.exists()) {
			return null;
		}
		String suffix = FileExUtils.getExtension(imageFile.getName());
		IResourceType resourceType = ResourceUtils.getResourceTypeBySuffix(suffix);
		Assert.notNull(resourceType, () -> ContentCoreErrorCode.UNSUPPORTED_RESOURCE_TYPE.exception(suffix));

		CmsResource resource = new CmsResource();
		resource.setResourceId(IdUtils.getSnowflakeId());
		resource.setSiteId(site.getSiteId());
		resource.setResourceType(resourceType.getId());
		resource.setFileName(resource.getResourceId().toString() + "." + suffix);
		resource.setName(resource.getFileName());
		resource.setSuffix(suffix);

		String siteResourceRoot = SiteUtils.getSiteResourceRoot(site);
		String fileName = resource.getResourceId() + StringUtils.DOT + suffix;
		String dir = resourceType.getUploadPath()
				+ LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + StringUtils.SLASH;
		FileExUtils.mkdirs(siteResourceRoot + dir);

		resource.setPath(dir + fileName);
		resource.setStatus(EnableOrDisable.ENABLE);
		resource.createBy(operator);

		byte[] bytes = FileUtils.readFileToByteArray(imageFile);
		this.processResource(resource, resourceType, site, bytes);
		return resource;
	}
	
	private void processResource(CmsResource resource, IResourceType resourceType, CmsSite site, byte[] bytes) throws IOException {
		// 处理资源，图片属性读取、水印等
		bytes = resourceType.process(resource, bytes);
		// 写入磁盘/OSS
		String fileStorageType = FileStorageTypeProperty.getValue(site.getConfigProps());
		IFileStorageType fst = this.getFileStorageType(fileStorageType);
		FileStorageArgs fileStorageArgs = FileStorageArgsProperty.getValue(site.getConfigProps());
		// 写入参数设置
		StorageWriteArgsBuilder builder = StorageWriteArgs.builder();
		builder.bucket(fileStorageArgs.getBucket());
		if (LocalFileStorageType.TYPE.equals(fst.getType())) {
			builder.bucket(SiteUtils.getSiteResourceRoot(site));
		} else {
			builder.accessKey(fileStorageArgs.getAccessKey());
			builder.accessSecret(fileStorageArgs.getAccessSecret());
			builder.endpoint(fileStorageArgs.getEndpoint());
		}
		builder.path(resource.getPath());
		builder.inputStream(new ByteArrayInputStream(bytes));
		fst.write(builder.build());
		resource.setStorageType(fst.getType());
		// 内部链接
		resource.setInternalUrl(InternalDataType_Resource.getInternalUrl(resource));
		// 资源记录入库
		this.save(resource);
	}

	@Override
	public void deleteResource(List<Long> resourceIds) {
		List<CmsResource> resources = this.listByIds(resourceIds);
		if (resources.size() > 0) {
			CmsSite site = siteService.getSite(resources.get(0).getSiteId());
			String siteRoot = SiteUtils.getSiteResourceRoot(site);
			resources.forEach(r -> {
				// 删除资源文件
				try {
					FileUtils.delete(new File(siteRoot + r.getPath()));
				} catch (IOException e) {
					e.printStackTrace();
				}
				// 删除数据库记录
				this.removeById(r.getResourceId());
			});
		}
	}

	@Override
	public String getResourceLink(CmsResource resource, boolean isPreview) {
		CmsSite site = this.siteService.getSite(resource.getSiteId());
		String prefix = ResourceUtils.getResourcePrefix(resource.getStorageType(), site, isPreview);
		return prefix + resource.getPath();
	}
	
	@Override
	public void downloadResource(CmsResource resource, HttpServletResponse response) {
		CmsSite site = this.siteService.getSite(resource.getSiteId());
		IFileStorageType storagetType = this.getFileStorageType(resource.getStorageType());
		StorageReadArgsBuilder builder = StorageReadArgs.builder();
		FileStorageArgs fileStorageArgs = FileStorageArgsProperty.getValue(site.getConfigProps());
		if (fileStorageArgs != null) {
			builder.endpoint(fileStorageArgs.getEndpoint())
				.accessKey(fileStorageArgs.getAccessKey())
				.accessSecret(fileStorageArgs.getAccessSecret())
				.bucket(fileStorageArgs.getBucket())
				.path(resource.getPath());
		}
		InputStream is = storagetType.read(builder.build());
		try {
			IOUtils.copy(is, response.getOutputStream());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private IFileStorageType getFileStorageType(String type) {
		IFileStorageType fileStorageType = fileStorageTypes.get(IFileStorageType.BEAN_NAME_PREIFX + type);
		Assert.notNull(fileStorageType, () -> StorageErrorCode.UNSUPPORTED_STORAGE_TYPE.exception(type));
		return fileStorageType;
	}

	private final List<IResourceStat> resourceStats;
	
	/**
	 * TODO 统计资源引用
	 */
	public void statResourceUsage() {
		Map<Long, Integer> resourceIds = new HashMap<>();
		this.resourceStats.forEach(rs -> {
			Map<Long, Integer> quotedResource = rs.findQuotedResource();
		});
		// 站点Logo，栏目Logo，内容Logo
		
		// 页面部件区块Logo
		
		// 页面部件广告图

		// 文章内容
		
		// 图集内容
		
		// 音频内容
		
		// 视频内容
	}
}
