package com.ruoyi.system.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.i18n.I18nField;
import com.ruoyi.common.db.domain.BaseEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 字典类型表 sys_dict_type
 * 
 * @author ruoyi
 */
@Getter
@Setter
@TableName(SysDictType.TABLE_NAME)
public class SysDictType extends BaseEntity {
	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "sys_dict_type";

	/** 字典主键 */
	@ExcelProperty("字典主键")
	@TableId(value = "dict_id", type = IdType.AUTO)
	private Long dictId;

	/** 字典名称 */
	@ExcelProperty("字典名称")
	@I18nField("{DICT.#{dictType}}")
	private String dictName;

	/** 字典类型 */
	@ExcelProperty("字典类型")
	private String dictType;

	/**
	 * 是否系统固定字典数据
	 */
	@TableField(exist = false)
	private String fixed;

	@NotBlank(message = "字典名称不能为空")
	@Size(min = 0, max = 100, message = "字典类型名称长度不能超过100个字符")
	public String getDictName() {
		return dictName;
	}

	@NotBlank(message = "字典类型不能为空")
	@Size(min = 0, max = 100, message = "字典类型类型长度不能超过100个字符")
	@Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]*$", message = "字典类型必须以字母开头，且只能为（大小写字母，数字，下滑线）")
	public String getDictType() {
		return dictType;
	}
}
