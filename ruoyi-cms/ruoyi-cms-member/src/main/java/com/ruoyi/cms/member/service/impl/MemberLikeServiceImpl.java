package com.ruoyi.cms.member.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.cms.member.domain.CmsMemberLike;
import com.ruoyi.cms.member.exception.CmsMemberErrorCode;
import com.ruoyi.cms.member.mapper.CmsMemberLikeMapper;
import com.ruoyi.cms.member.service.IMemberLikeService;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.mapper.CmsContentMapper;
import com.ruoyi.contentcore.service.impl.ContentDynamicDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 会员内容点赞服务类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class MemberLikeServiceImpl extends ServiceImpl<CmsMemberLikeMapper, CmsMemberLike>
        implements IMemberLikeService {

    private final ContentDynamicDataService contentDynamicDataService;

    private final CmsContentMapper contentMapper;

    @Override
    public void likeContent(Long memberId, Long contentId) {
        Long count = this.lambdaQuery()
                .eq(CmsMemberLike::getMemberId, memberId)
                .eq(CmsMemberLike::getContentId, contentId)
                .count();
        Assert.isTrue(count == 0, CmsMemberErrorCode.LIKED::exception);

        CmsContent content = this.contentMapper.selectById(contentId);
        Assert.notNull(content, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));

        CmsMemberLike like = new CmsMemberLike();
        like.setLogId(IdUtils.getSnowflakeId());
        like.setSiteId(content.getSiteId());
        like.setMemberId(memberId);
        like.setContentId(contentId);
        like.setCreateTime(LocalDateTime.now());
        this.save(like);
        // 内容收藏数+1
        this.contentDynamicDataService.increaseLikeCount(contentId);
    }

    @Override
    public void cancelLikeContent(Long memberId, Long contentId) {
        Optional<CmsMemberLike> opt = this.lambdaQuery()
                .eq(CmsMemberLike::getMemberId, memberId)
                .eq(CmsMemberLike::getContentId, contentId)
                .oneOpt();
        if (opt.isEmpty()) {
            return;
        }
        this.removeById(opt.get());
        // 内容收藏数-1
        this.contentDynamicDataService.decreaseLikeCount(contentId);
    }
}
