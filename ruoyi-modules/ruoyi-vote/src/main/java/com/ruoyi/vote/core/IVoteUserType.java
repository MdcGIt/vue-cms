package com.ruoyi.vote.core;

/**
 * 投票用户类型
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
public interface IVoteUserType {

	public static final String BEAN_PREFIX = "VoteUserType_";
	
	/**
	 * 投票用户类型ID，唯一标识
	 */
	public String getId();

	/**
	 * 投票用户类型名称
	 */
	public String getName();
}
