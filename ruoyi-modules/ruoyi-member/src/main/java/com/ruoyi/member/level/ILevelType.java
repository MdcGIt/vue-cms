package com.ruoyi.member.level;

import com.ruoyi.member.domain.MemberLevel;

/**
 * 会员等级类型
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ILevelType {
	
	public String BEAN_PREFIX = "LevelType_";

	/**
	 * 等级类型唯一标识ID
	 * 
	 * @return
	 */
	public String getId();
	
	/**
	 * 等级类型名称
	 */
	public String getName();
	
	/**
	 * 升级处理逻辑，每提升一级都需要调用一次，一次升N级需要调用N次
	 */
	default public void onLevelUp(MemberLevel level) {
		
	}
}
