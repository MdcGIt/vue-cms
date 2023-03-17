package com.ruoyi.common.mybatisplus.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.common.utils.poi.converter.LocalDateTimeConverter;

import lombok.Getter;
import lombok.Setter;

/**
 * Entity基类
 */
@Getter
@Setter
public abstract class BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String Field_createTime = "create_time";

	public static final String Field_updateTime = "update_time";

	/** 创建者 */
	@TableField("create_by")
	private String createBy;

	@ColumnWidth(16)
	@ExcelProperty(converter = LocalDateTimeConverter.class)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@TableField(Field_createTime)
	private LocalDateTime createTime;

	/** 更新者 */
	@TableField("update_by")
	private String updateBy;

	@ColumnWidth(16)
	@ExcelProperty(converter = LocalDateTimeConverter.class)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@TableField(Field_updateTime)
	private LocalDateTime updateTime;

	/** 备注 */
	private String remark;

	@TableField(exist = false)
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@ExcelIgnore
	private Map<String, Object> params;

	public void createBy(String username) {
		this.createBy = username;
		this.createTime = LocalDateTime.now();
		this.updateBy = this.createBy;
		this.updateTime = this.createTime;
	}

	public void updateBy(String username) {
		this.updateBy = username;
		this.updateTime = LocalDateTime.now();
	}

	public Map<String, Object> getParams() {
		if (params == null) {
			params = new HashMap<>();
		}
		return params;
	}
}
