package com.ruoyi.cms.search.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ESContentVO {
	
	private Long contentId;
	
	private String contentType;
	
	private Long siteId;
	
	private Long catalogId;
	
	private String catalogName;
	
	private String catalogAncestors;
	
	private String logo;

	private String title;

	private String summary;
	
	private String link;
	
	private String status;
	
	private String author;
	
	private String editor;
	
	private String keywords;
	
	private String tags;
	
	private String fullText;

	private Long publishDate;

	private LocalDateTime publishDateInstance;

	private Long createTime;

	private LocalDateTime createTimeInstance;
	
	private Double hitScore;
}
