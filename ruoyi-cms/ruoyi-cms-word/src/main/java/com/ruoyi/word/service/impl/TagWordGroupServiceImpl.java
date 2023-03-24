package com.ruoyi.word.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.domain.TreeNode;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.SortUtils;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.word.domain.CmsTagWord;
import com.ruoyi.word.domain.CmsTagWordGroup;
import com.ruoyi.word.mapper.CmsTagWordGroupMapper;
import com.ruoyi.word.mapper.CmsTagWordMapper;
import com.ruoyi.word.service.ITagWordGroupService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagWordGroupServiceImpl extends ServiceImpl<CmsTagWordGroupMapper, CmsTagWordGroup>
		implements ITagWordGroupService {

	private final CmsTagWordMapper tagWordMapper;

	@Override
	public void addTagWordGroup(CmsTagWordGroup group) {
		checkUnique(group.getSiteId(), group.getParentId(), null, group.getName(), group.getCode());

		group.setGroupId(IdUtils.getSnowflakeId());
		group.setSortFlag(SortUtils.getDefaultSortValue());
		this.save(group);
	}

	@Override
	public void editTagWordGroup(CmsTagWordGroup group) {
		CmsTagWordGroup dbGroup = this.getById(group.getGroupId());
		Assert.notNull(dbGroup, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("groupId", group.getGroupId()));

		checkUnique(dbGroup.getSiteId(), group.getParentId(), dbGroup.getGroupId(), group.getName(), group.getCode());

		dbGroup.setName(group.getName());
		dbGroup.setLogo(group.getLogo());
		dbGroup.setRemark(group.getRemark());
		dbGroup.updateBy(group.getUpdateBy());
		this.updateById(group);
	}

	private void checkUnique(Long siteId, Long parentId, Long groupId, String name, String code) {
		Long count = this.lambdaQuery().eq(CmsTagWordGroup::getSiteId, siteId)
				.eq(CmsTagWordGroup::getParentId, parentId)
				.ne(groupId != null && groupId > 0, CmsTagWordGroup::getGroupId, groupId)
				.and(wrapper -> wrapper.eq(CmsTagWordGroup::getName, name).or().eq(CmsTagWordGroup::getCode, code))
				.count();
		Assert.isTrue(count == 0, () -> CommonErrorCode.DATA_CONFLICT.exception("name/code"));
	}

	@Override
	@Transactional
	public void deleteTagWordGroups(List<Long> groupIds) {
		for (Long groupId : groupIds) {
			this.removeById(groupId);
			this.tagWordMapper.delete(new LambdaQueryWrapper<CmsTagWord>().eq(CmsTagWord::getGroupId, groupId));
		}
	}

	@Override
	public List<TreeNode<String>> buildTreeData(List<CmsTagWordGroup> groups) {
		List<TreeNode<String>> list = new ArrayList<>();
		if (groups != null && groups.size() > 0) {
			groups.forEach(c -> {
				TreeNode<String> treeNode = new TreeNode<>(String.valueOf(c.getGroupId()),
						String.valueOf(c.getParentId()), c.getName(), c.getParentId() == 0);
				String logoSrc = InternalUrlUtils.getActualPreviewUrl(c.getLogo());
				Map<String, Object> props = Map.of("code", c.getCode(), "logo", c.getLogo(), "logoSrc", logoSrc);
				treeNode.setProps(props);
				list.add(treeNode);
			});
		}
		return TreeNode.build(list);
	}
}