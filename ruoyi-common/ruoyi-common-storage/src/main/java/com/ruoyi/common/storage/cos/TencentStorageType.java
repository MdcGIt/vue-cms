package com.ruoyi.common.storage.cos;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.CreateBucketRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.ruoyi.common.i18n.I18nUtils;
import com.ruoyi.common.storage.IFileStorageType;
import com.ruoyi.common.storage.OSSClient;
import com.ruoyi.common.storage.StorageCopyArgs;
import com.ruoyi.common.storage.StorageCreateBucketArgs;
import com.ruoyi.common.storage.StorageExistArgs;
import com.ruoyi.common.storage.StorageMoveArgs;
import com.ruoyi.common.storage.StorageReadArgs;
import com.ruoyi.common.storage.StorageRemoveArgs;
import com.ruoyi.common.storage.StorageWriteArgs;

@Component(IFileStorageType.BEAN_NAME_PREIFX + TencentStorageType.TYPE)
public class TencentStorageType implements IFileStorageType {

	public final static String TYPE = "TencentCOS";

	private Map<String, OSSClient<COSClient>> clients = new HashMap<>();

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public String getName() {
		return I18nUtils.get("STORAGE.TYPE." + TYPE);
	}
	
	@Override
	public boolean testConnection(String endpoint, String accessKey, String accessSecret) {
		OSSClient<COSClient> client = this.getClient(endpoint, accessKey, accessSecret);
		try {
			client.getClient().listBuckets();
			return true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}

	@Override
	public void reloadClient(String endpoint, String accessKey, String accessSecret) {
		OSSClient<COSClient> client = this.getClient(endpoint, accessKey, accessSecret);
		if (client != null) {
			client.getClient().shutdown();
			this.clients.remove(endpoint);
		}
		this.getClient(endpoint, accessKey, accessSecret);
	}
	
	/**
	 * 创建存储桶
	 * 
	 * @param bucketName
	 */
	@Override
	public void createBucket(StorageCreateBucketArgs args) {
		OSSClient<COSClient> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		if (client.getClient().doesBucketExist(args.getBucket())) {
			return;
		}
		CreateBucketRequest request = new CreateBucketRequest(args.getBucket());
		client.getClient().createBucket(request);
	}

	@Override
	public boolean exists(StorageExistArgs args) {
		OSSClient<COSClient> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		return client.getClient().doesObjectExist(args.getBucket(), args.getPath());
	}
	
	@Override
	public InputStream read(StorageReadArgs args) {
		OSSClient<COSClient> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		COSObject object = client.getClient().getObject(args.getBucket(), args.getPath());
		return object.getObjectContent();
	}

	@Override
	public void write(StorageWriteArgs args) {
		OSSClient<COSClient> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		ObjectMetadata objectMetadata = new ObjectMetadata();
		if (args.getLength() > 0) {
			objectMetadata.setContentLength(args.getLength());
		}
		PutObjectRequest putObjectRequest = new PutObjectRequest(args.getBucket(), args.getPath(), args.getInputStream(), objectMetadata);
		client.getClient().putObject(putObjectRequest);
	}

	@Override
	public void remove(StorageRemoveArgs args) {
		OSSClient<COSClient> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		client.getClient().deleteObject(args.getBucket(), args.getPath());
	}

	@Override
	public void copy(StorageCopyArgs args) {
		OSSClient<COSClient> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		client.getClient().copyObject(args.getBucket(), args.getSourcePath(), args.getBucket(), args.getDestPath());
	}

	@Override
	public void move(StorageMoveArgs args) {
		OSSClient<COSClient> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		client.getClient().copyObject(args.getBucket(), args.getSourcePath(), args.getBucket(), args.getDestPath());
		// 复制后删除源
		client.getClient().deleteObject(args.getBucket(), args.getSourcePath());
	}
	
	private OSSClient<COSClient> getClient(String endpoint, String accessKey, String accessSecret) {
		OSSClient<COSClient> client = this.clients.get(endpoint);
		if (client == null) {
			client = new OSSClient<>();
			COSCredentials credentials = new BasicCOSCredentials(accessKey, accessSecret);
			client.setClient(new COSClient(credentials, new ClientConfig(new Region(endpoint))));
			this.clients.put(endpoint, client);
		}
		client.setLastActiveTime(System.currentTimeMillis());
		return client;
	}
}
