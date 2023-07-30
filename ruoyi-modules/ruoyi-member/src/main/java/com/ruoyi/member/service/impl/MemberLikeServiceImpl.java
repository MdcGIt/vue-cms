package com.ruoyi.member.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.member.domain.MemberFavorites;
import com.ruoyi.member.domain.MemberLike;
import com.ruoyi.member.exception.MemberErrorCode;
import com.ruoyi.member.listener.event.AfterMemberCancelLikeEvent;
import com.ruoyi.member.listener.event.AfterMemberLikeEvent;
import com.ruoyi.member.listener.event.BeforeMemberLikeEvent;
import com.ruoyi.member.mapper.MemberLikeMapper;
import com.ruoyi.member.service.IMemberLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 会员点赞服务类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class MemberLikeServiceImpl extends ServiceImpl<MemberLikeMapper, MemberLike>
        implements IMemberLikeService, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void like(Long memberId, String dataType, Long dataId) {
        Long count = this.lambdaQuery()
                .eq(MemberLike::getMemberId, memberId)
                .eq(MemberLike::getDataType, dataType)
                .eq(MemberLike::getDataId, dataId)
                .count();
        Assert.isTrue(count == 0, MemberErrorCode.LIKED::exception);

        this.applicationContext.publishEvent(new BeforeMemberLikeEvent(this, dataType, dataId));

        MemberLike like = new MemberLike();
        like.setLogId(IdUtils.getSnowflakeId());
        like.setMemberId(memberId);
        like.setDataType(dataType);
        like.setDataId(dataId);
        like.setCreateTime(LocalDateTime.now());
        this.save(like);

        this.applicationContext.publishEvent(new AfterMemberLikeEvent(this, like));
    }

    @Override
    public void cancelLike(Long memberId, String dataType, Long dataId) {
        Optional<MemberLike> opt = this.lambdaQuery()
                .eq(MemberLike::getMemberId, memberId)
                .eq(MemberLike::getDataType, dataType)
                .eq(MemberLike::getDataId, dataId)
                .oneOpt();
        Assert.isTrue(opt.isPresent(), MemberErrorCode.NOT_LIKED::exception);

        MemberLike memberLike = opt.get();
        this.removeById(memberLike);

        this.applicationContext.publishEvent(new AfterMemberCancelLikeEvent(this, memberLike));
    }

    @Override
    public List<MemberLike> getMemberLikes(Long memberId, String dataType, Integer limit, Long offset) {
        List<MemberLike> list = this.lambdaQuery()
                .eq(MemberLike::getDataType, dataType)
                .eq(MemberLike::getMemberId, memberId)
                .lt(IdUtils.validate(offset), MemberLike::getLogId, offset)
                .orderByDesc(MemberLike::getLogId)
                .last("limit " + limit)
                .list();
        return list;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
