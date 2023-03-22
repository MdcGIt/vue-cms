package com.ruoyi.word.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.word.domain.CmsHotWordGroup;

public interface IHotWordGroupService extends IService<CmsHotWordGroup> {

	/**
	 * 获取站点所有热词分组
	 * 
	 * @param siteId
	 * @return
	 */
	List<CmsHotWordGroup> getHotWordGroupsBySiteId(Long siteId);

	/**
	 * 添加热词分组
	 * 
	 * @param group
	 */
	void addHotWordGroup(CmsHotWordGroup group);

	/**
	 * 修改热词分组
	 * 
	 * @param group
	 */
	void updateHotWordGroup(CmsHotWordGroup group);

	/**
	 * 删除热词分组
	 * 
	 * @param groupIds
	 */
	void deleteHotWordGroups(List<Long> groupIds);
}
