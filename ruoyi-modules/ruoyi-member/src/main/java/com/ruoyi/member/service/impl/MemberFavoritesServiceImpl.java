package com.ruoyi.member.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.member.domain.MemberFavorites;
import com.ruoyi.member.exception.MemberErrorCode;
import com.ruoyi.member.listener.event.AfterMemberCancelFavoriteEvent;
import com.ruoyi.member.listener.event.AfterMemberFavoriteEvent;
import com.ruoyi.member.listener.event.BeforeMemberFavoriteEvent;
import com.ruoyi.member.mapper.MemberFavoritesMapper;
import com.ruoyi.member.service.IMemberFavoritesService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 会员内容收藏服务类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class MemberFavoritesServiceImpl extends ServiceImpl<MemberFavoritesMapper, MemberFavorites>
        implements IMemberFavoritesService, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public List<MemberFavorites> getMemberFavorites(Long memberId, String dataType, Integer limit, Long offset) {
        List<MemberFavorites> list = this.lambdaQuery()
                .eq(MemberFavorites::getDataType, dataType)
                .eq(MemberFavorites::getMemberId, memberId)
                .lt(IdUtils.validate(offset), MemberFavorites::getLogId, offset)
                .orderByDesc(MemberFavorites::getLogId)
                .last("limit " + limit)
                .list();
        return list;
    }

    @Override
    public void favorite(Long memberId, String dataType, Long dataId) {
        Long count = this.lambdaQuery()
                .eq(MemberFavorites::getMemberId, memberId)
                .eq(MemberFavorites::getDataType, dataType)
                .eq(MemberFavorites::getDataId, dataId)
                .count();
        Assert.isTrue(count == 0, MemberErrorCode.FAVORITED::exception);

        this.applicationContext.publishEvent(new BeforeMemberFavoriteEvent(this, dataType, dataId));

        MemberFavorites favorites = new MemberFavorites();
        favorites.setLogId(IdUtils.getSnowflakeId());
        favorites.setMemberId(memberId);
        favorites.setDataType(dataType);
        favorites.setDataId(dataId);
        favorites.setCreateTime(LocalDateTime.now());
        this.save(favorites);

        this.applicationContext.publishEvent(new AfterMemberFavoriteEvent(this, favorites));
    }

    @Override
    public void cancelFavorite(Long memberId, String dataType, Long dataId) {
        Optional<MemberFavorites> opt = this.lambdaQuery()
                .eq(MemberFavorites::getMemberId, memberId)
                .eq(MemberFavorites::getDataType, dataType)
                .eq(MemberFavorites::getDataId, dataId)
                .oneOpt();
        Assert.isTrue(opt.isPresent(), MemberErrorCode.NOT_FAVORITED::exception);

        MemberFavorites memberFavorites = opt.get();
        this.removeById(memberFavorites);

        this.applicationContext.publishEvent(new AfterMemberCancelFavoriteEvent(this, memberFavorites));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
