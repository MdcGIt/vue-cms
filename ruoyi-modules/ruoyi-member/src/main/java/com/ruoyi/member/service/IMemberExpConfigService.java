package com.ruoyi.member.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.member.domain.MemberExpConfig;
import com.ruoyi.member.level.IExpOperation;

public interface IMemberExpConfigService extends IService<MemberExpConfig> {
	
	/**
	 * 获取经验值操作项实例
	 * 
	 * @param opType
	 * @return
	 */
	IExpOperation getExpOperation(String opType);
	
	/**
	 * 获取经验值操作项实例集合
	 * 
	 * @param opType
	 * @return
	 */
	Map<String, IExpOperation> getExpOperations();

	/**
	 * 获取指定等级类型的操作项配置数据
	 * 
	 * @param opType
	 * @param levelType
	 * @return
	 */
	MemberExpConfig getMemberExpOperation(String opType, String levelType);

	/**
	 * 添加操作项配置
	 * 
	 * @param expOp
	 * @param operator
	 */
	void addExpOperation(MemberExpConfig expOp);

	/**
	 * 更细操作项配置
	 * 
	 * @param expOp
	 * @param operator
	 */
	void updateExpOperation(MemberExpConfig expOp);

	/**
	 * 删除操作项配置
	 * 
	 * @param expOperationIds
	 */
	void deleteExpOperations(List<Long> expOperationIds);

	/**
	 * 触发操作项，处理操作项配置等级经验值
	 * 
	 * @param expOpId
	 * @param memberId
	 */
	void triggerExpOperation(String expOpId, Long memberId);
}