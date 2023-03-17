package com.ruoyi.system.domain;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.mybatisplus.domain.BaseEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 岗位表 sys_post
 * 
 * @author ruoyi
 */
@Getter
@Setter
@TableName(SysPost.TABLE_NAME)
public class SysPost extends BaseEntity {
	
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "sys_post";

	/** 岗位序号 */
	@ExcelProperty("岗位序号")
	@TableId(value = "post_id", type = IdType.AUTO)
	private Long postId;

	/** 岗位编码 */
	@ExcelProperty("岗位编码")
	private String postCode;

	/** 岗位名称 */
	@ExcelProperty("岗位名称")
	private String postName;

	/** 岗位排序 */
	@ExcelProperty("岗位排序")
	private Integer postSort;

	/** 状态（0正常 1停用） */
	@ExcelProperty("状态")
	private String status;

	/** 用户是否存在此岗位标识 默认不存在 */
	@ExcelIgnore
	@TableField(exist = false)
	private boolean flag = false;

	@NotBlank(message = "岗位编码不能为空")
	@Size(min = 0, max = 64, message = "岗位编码长度不能超过64个字符")
	public String getPostCode() {
		return postCode;
	}

	@NotBlank(message = "岗位名称不能为空")
	@Size(min = 0, max = 50, message = "岗位名称长度不能超过50个字符")
	public String getPostName() {
		return postName;
	}

	@NotNull(message = "显示顺序不能为空")
	public Integer getPostSort() {
		return postSort;
	}
}
