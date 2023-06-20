package com.ruoyi.system.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.db.domain.BaseEntity;
import com.ruoyi.system.fixed.dict.EnableOrDisable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色表 sys_role
 * 
 * @author ruoyi
 */
@Getter
@Setter
@TableName(SysRole.TABLE_NAME)
public class SysRole extends BaseEntity {
	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "sys_role";

	/** 角色ID */
	@ExcelProperty("角色序号")
	@TableId(value = "role_id", type = IdType.AUTO)
	private Long roleId;

	/** 角色名称 */
	@ExcelProperty("角色名称")
	private String roleName;

	/** 角色权限 */
	@ExcelProperty("角色权限")
	private String roleKey;

	/** 角色排序 */
	@ExcelProperty("角色排序")
	private Integer roleSort;

	/** 角色状态（0正常 1停用） */
	@ExcelProperty("角色状态")
	private String status;
	
	public SysRole() {
		
	}

	public SysRole(Long roleId) {
		this.roleId = roleId;
	}

	@NotBlank(message = "角色名称不能为空")
	@Size(min = 0, max = 30, message = "角色名称长度不能超过30个字符")
	public String getRoleName() {
		return roleName;
	}

	@NotBlank(message = "权限字符不能为空")
	@Size(min = 0, max = 100, message = "权限字符长度不能超过100个字符")
	public String getRoleKey() {
		return roleKey;
	}

	@NotNull(message = "显示顺序不能为空")
	public Integer getRoleSort() {
		return roleSort;
	}
	
	public boolean isEnable() {
		return EnableOrDisable.isEnable(this.status);
	}
}
