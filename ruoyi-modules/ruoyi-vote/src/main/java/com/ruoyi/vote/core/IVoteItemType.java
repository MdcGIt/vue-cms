package com.ruoyi.vote.core;

/**
 * 投票选项类型
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
public interface IVoteItemType {

	public static final String BEAN_PREFIX = "VoteItemType_";
	
	/**
	 * 投票选项类型ID，唯一标识
	 */
	public String getId();

	/**
	 * 投票选项类型名称
	 */
	public String getName();
}
