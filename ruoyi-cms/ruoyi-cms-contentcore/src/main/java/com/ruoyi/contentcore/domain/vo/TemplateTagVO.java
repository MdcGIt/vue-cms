package com.ruoyi.contentcore.domain.vo;

import java.util.List;

import com.ruoyi.common.staticize.tag.TagAttr;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TemplateTagVO {
	
	private String name;
	
	private String tagName;
	
	private String description;
	
	private List<TagAttr> tagAttrs;
}
