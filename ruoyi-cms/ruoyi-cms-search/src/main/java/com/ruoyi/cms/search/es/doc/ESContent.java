package com.ruoyi.cms.search.es.doc;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(indexName = ESContent.INDEX_NAME)
public class ESContent {
	
	public static final String INDEX_NAME = "cms_content";

	@Id
	@Field(type = FieldType.Long)
	private Long contentId;

	@Field(type = FieldType.Keyword)
	private String contentType;

	@Field(type = FieldType.Long)
	private Long siteId;

	@Field(type = FieldType.Long)
	private Long catalogId;

	@Field(type = FieldType.Keyword)
	private String catalogAncestors;

	@Field(type = FieldType.Keyword)
	private String logo;

	@Field(type = FieldType.Text, analyzer = "ik_smart")
	private String title;

	@Field(type = FieldType.Keyword)
	private String link;

	@Field(type = FieldType.Keyword)
	private String status;

	@Field(type = FieldType.Keyword)
	private String author;

	@Field(type = FieldType.Keyword)
	private String editor;

	@Field(type = FieldType.Keyword)
	private String keywords;

	@Field(type = FieldType.Keyword)
	private String tags;
	
	/**
	 * 内容详情，需要分词处理的数据
	 */
	@Field(type = FieldType.Text, analyzer = "ik_smart")
	private String fullText;

	@Field(type = FieldType.Long)
	private Long publishDate;

	@Field(type = FieldType.Long)
	private Long createTime;
}
