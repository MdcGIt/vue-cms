package com.ruoyi.contentcore.core.impl;

import org.springframework.stereotype.Component;

import com.ruoyi.contentcore.core.IResourceType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component(IResourceType.BEAN_NAME_PREFIX + ResourceType_File.ID)
public class ResourceType_File implements IResourceType {
	
	public final static String ID = "file";
	
	private final static String[] SuffixArray = { ".zip", "" };
	
	@Override
	public String getId() {
		return ID;
	}
	
	@Override
	public String[] getUsableSuffix() {
		return SuffixArray;
	}
}
