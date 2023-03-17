package com.ruoyi.word.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.word.domain.CmsHotWordGroup;

public interface IHotWordGroupService extends IService<CmsHotWordGroup> {

	void deleteHotWordGroups(List<Long> groupIds);

	List<CmsHotWordGroup> getHotWordGroupsBySiteId(Long siteId);
}
