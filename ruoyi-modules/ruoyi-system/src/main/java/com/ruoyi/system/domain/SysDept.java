package com.ruoyi.system.domain;

import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.db.domain.BaseEntity;
import com.ruoyi.system.fixed.dict.EnableOrDisable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 部门表 sys_dept
 * 
 * @author ruoyi
 */
@Getter
@Setter
@TableName(SysDept.TABLE_NAME)
public class SysDept extends BaseEntity {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "sys_dept";

	/** 部门ID */
	@TableId(value = "dept_id", type = IdType.INPUT)
	private Long deptId;

	/** 父部门ID */
	private Long parentId;

	/** 祖级列表 */
	private String ancestors;

	/** 部门名称 */
	private String deptName;

	/** 显示顺序 */
	private Integer orderNum;

	/** 负责人 */
	private String leader;

	/** 联系电话 */
	private String phone;

	/** 邮箱 */
	private String email;

	/** 部门状态:EnableOrDisable */
	private String status;

	/** 父部门名称 */
	@TableField(exist = false)
	private String parentName;

	/** 子部门 */
	@TableField(exist = false)
	private List<SysDept> children = new ArrayList<SysDept>();

	@NotBlank(message = "部门名称不能为空")
	@Size(min = 0, max = 30, message = "部门名称长度不能超过30个字符")
	public String getDeptName() {
		return deptName;
	}

	@NotNull(message = "显示顺序不能为空")
	public Integer getOrderNum() {
		return orderNum;
	}

	@Size(min = 0, max = 11, message = "联系电话长度不能超过11个字符")
	public String getPhone() {
		return phone;
	}

	@Email(message = "邮箱格式不正确")
	@Size(min = 0, max = 50, message = "邮箱长度不能超过50个字符")
	public String getEmail() {
		return email;
	}
	
	public boolean isEnable() {
		return EnableOrDisable.isEnable(this.status);
	}
}
