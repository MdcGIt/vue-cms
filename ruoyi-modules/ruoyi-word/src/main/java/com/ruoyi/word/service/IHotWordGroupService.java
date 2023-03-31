package com.ruoyi.word.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.word.domain.HotWordGroup;

public interface IHotWordGroupService extends IService<HotWordGroup> {

	/**
	 * 添加热词分组
	 * 
	 * @param group
	 */
	void addHotWordGroup(HotWordGroup group);

	/**
	 * 修改热词分组
	 * 
	 * @param group
	 */
	void updateHotWordGroup(HotWordGroup group);

	/**
	 * 删除热词分组
	 * 
	 * @param groupIds
	 */
	void deleteHotWordGroups(List<Long> groupIds);
}
