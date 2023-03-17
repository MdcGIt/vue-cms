package com.ruoyi.system.domain.vo;

import java.util.List;

import com.ruoyi.system.domain.SysPost;
import com.ruoyi.system.domain.SysRole;
import com.ruoyi.system.domain.SysUser;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserInfoVO {

	private SysUser user;
	
	private List<SysRole> roles;
	
	private List<SysPost> posts;
}
