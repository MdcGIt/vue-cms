package com.ruoyi.cms.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.cms.member.domain.CmsMemberLike;

public interface IMemberLikeService extends IService<CmsMemberLike> {

    void likeContent(Long memberId, Long contentId);

    void cancelLikeContent(Long memberId, Long contentId);
}
