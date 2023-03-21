package com.ruoyi.member.level;

/**
 * 等级经验变更操作项
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
public interface IExpOperation {
	
	public String BEAN_PREFIX = "ExpOperation_";

	/**
	 * 唯一标识ID
	 */
	public String getId();
	
	/**
	 * 操作项名称
	 */
	public String getName();
}
