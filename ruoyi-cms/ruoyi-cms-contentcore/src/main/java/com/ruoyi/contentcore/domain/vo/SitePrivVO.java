package com.ruoyi.contentcore.domain.vo;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SitePrivVO {

	private Long siteId;

	private String name;

	private Map<String, Boolean> perms = new HashMap<>();
}
