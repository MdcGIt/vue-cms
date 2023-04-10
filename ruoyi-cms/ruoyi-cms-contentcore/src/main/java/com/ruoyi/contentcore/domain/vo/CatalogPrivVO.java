package com.ruoyi.contentcore.domain.vo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatalogPrivVO {

	private Long catalogId;
	
	private Long parentId;

	private String name;
	
	private List<CatalogPrivVO> children;
	
	private Map<String, Boolean> perms = new HashMap<>();
}
