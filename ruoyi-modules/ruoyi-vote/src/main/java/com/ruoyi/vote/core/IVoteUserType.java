package com.ruoyi.vote.core;

/**
 * 问卷调查用户类型
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
public interface IVoteUserType {

	public static final String BEAN_PREFIX = "VoteUserType_";
	
	/**
	 * 问卷调查用户类型ID，唯一标识
	 */
	String getId();

	/**
	 * 问卷调查用户类型名称
	 */
	String getName();

	/**
	 * 获取用户唯一标识
	 */
	String getUserId();
}
