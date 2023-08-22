package com.ruoyi.common.staticize.tag;

import com.ruoyi.common.staticize.enums.TagAttrDataType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 标签属性描述类
 */
@Getter
@Setter
public class TagAttr {
	
	/**
	 * 标签属性名：page = 是否分页获取数据
	 */
	public final static String AttrName_Page = "page";

	/**
	 * 标签属性名：size = 每页数据量
	 * 
	 * <p>
	 * page=true时作为全局分页参数
	 * page=false时作为标签数据列表上限值
	 * </p>
	 */
	public final static String AttrName_PageSize = "size";
	
	/**
	 * 标签属性名：condition = 扩展条件
	 */
	public final static String AttrName_Condition = "condition";

	/**
	 * 属性名称
	 */
	private String name;

	/**
	 * 是否必填
	 */
	private boolean mandatory;

	/**
	 * 用法说明
	 */
	private String usage;

	/**
	 * 属性数据类型
	 */
	private TagAttrDataType dataType;

	/**
	 * 属性可选项
	 */
	private List<TagAttrOption> options;

	/**
	 * 属性默认值
	 */
	private String defaultValue;

	/**
	 * 布尔值可选项
	 */
	public static List<TagAttrOption> BOOL_OPTIONS = List.of(
			new TagAttrOption("true", "是"),
			new TagAttrOption("false", "否")
	);

	public TagAttr(String name, boolean mandatory, TagAttrDataType dataType, String usage) {
		this.name = name;
		this.mandatory = mandatory;
		this.dataType = dataType;
		this.usage = usage;
		if (dataType == TagAttrDataType.BOOLEAN) {
			this.setOptions(BOOL_OPTIONS);
		}
	}

	public TagAttr(String name, boolean mandatory, TagAttrDataType dataType, String usage, List<TagAttrOption> options) {
		this.name = name;
		this.mandatory = mandatory;
		this.dataType = dataType;
		this.usage = usage;
		this.options = options;
	}

	public TagAttr(String name, boolean mandatory, TagAttrDataType dataType, String usage, String defaultValue) {
		this.name = name;
		this.mandatory = mandatory;
		this.dataType = dataType;
		this.usage = usage;
		this.defaultValue = defaultValue;
		if (dataType == TagAttrDataType.BOOLEAN) {
			this.setOptions(BOOL_OPTIONS);
		}
	}

	public TagAttr(String name, boolean mandatory, TagAttrDataType dataType, String usage, List<TagAttrOption> options, String defaultValue) {
		this.name = name;
		this.mandatory = mandatory;
		this.dataType = dataType;
		this.usage = usage;
		this.options = options;
		this.defaultValue = defaultValue;
	}
}
