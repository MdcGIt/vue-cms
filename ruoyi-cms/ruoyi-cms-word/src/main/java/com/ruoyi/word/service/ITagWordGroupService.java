package com.ruoyi.word.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.domain.TreeNode;
import com.ruoyi.word.domain.CmsTagWordGroup;

public interface ITagWordGroupService extends IService<CmsTagWordGroup> {
	
	/**
	 * 添加TAG词分组
	 * 
	 * @param group
	 * @return
	 */
	void addTagWordGroup(CmsTagWordGroup group);

	/**
	 * 编辑TAG词分组
	 * 
	 * @param group
	 * @return
	 */
	void editTagWordGroup(CmsTagWordGroup group);

	/**
	 * 删除TAG词分组
	 * 
	 * @param groupIds
	 * @return
	 */
	void deleteTagWordGroups(List<Long> groupIds);

	/**
	 * 生成分组树数据
	 * 
	 * @param groups
	 * @return
	 */
	List<TreeNode<String>> buildTreeData(List<CmsTagWordGroup> groups);
}
