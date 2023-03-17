package com.ruoyi.system.domain;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.ruoyi.common.mybatisplus.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 权限表对象
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Getter
@Setter
@TableName(value = SysPermission.TABLE_NAME, autoResultMap = true)
public class SysPermission extends BaseEntity {

	private static final long serialVersionUID = 1L;
	
	public final static String TABLE_NAME = "sys_permission";
	
	@TableId(value = "perm_id", type = IdType.AUTO)
	private Long permId;
	
	/**
	 * 授权对象类型：联合主键
	 */
	private String ownerType;
	
	/**
	 * 授权对象唯一标识：联合主键
	 */
	private String owner;
	
	/**
	 * 授权信息
	 */
	@TableField(typeHandler = JacksonTypeHandler.class)
	private Map<String, List<String>> permissions;
}
