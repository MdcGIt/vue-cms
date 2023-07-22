package com.ruoyi.cms.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.cms.member.domain.CmsMemberFavorites;

import java.util.List;

public interface IMemberFavoritesService extends IService<CmsMemberFavorites> {

    void deleteMemberFavorites(List<Long> dataIds);

    void favoriteContent(Long memberId, Long contentId);

    void cancelFavoriteContent(Long memberId, Long contentId);
}
