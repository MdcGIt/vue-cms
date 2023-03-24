package com.ruoyi.contentcore.core.impl;

import java.util.Objects;

import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import com.ruoyi.contentcore.core.IResourceType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component(IResourceType.BEAN_NAME_PREFIX + ResourceType_Video.ID)
public class ResourceType_Video implements IResourceType {

	public final static String ID = "video";

	public final static String[] SuffixArray = { "mp4", "mpg", "mpeg", "rmvb", "rm", "avi", "wmv", "mov", "flv" };

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String[] getUsableSuffix() {
		return SuffixArray;
	}

	public static boolean isVideo(String path) {
		String ext = FileNameUtils.getExtension(path);
		return Objects.nonNull(path) && ArrayUtils.contains(SuffixArray, ext);
	}
}
