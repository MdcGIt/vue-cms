package com.ruoyi.link.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.link.domain.CmsLinkGroup;

public interface ILinkGroupService extends IService<CmsLinkGroup> {

	boolean deleteLinkGroup(List<Long> linkGroupIds);
}
