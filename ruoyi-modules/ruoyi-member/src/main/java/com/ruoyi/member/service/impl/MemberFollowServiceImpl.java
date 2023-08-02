package com.ruoyi.member.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.member.core.impl.FollowMemberStatData;
import com.ruoyi.member.core.impl.FollowerMemberStatData;
import com.ruoyi.member.domain.MemberFollow;
import com.ruoyi.member.exception.MemberErrorCode;
import com.ruoyi.member.mapper.MemberFollowMapper;
import com.ruoyi.member.service.IMemberFollowService;
import com.ruoyi.member.service.IMemberStatDataService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberFollowServiceImpl extends ServiceImpl<MemberFollowMapper, MemberFollow> implements IMemberFollowService {

    private final IMemberStatDataService memberStatDataService;

    private final RedissonClient redissonClient;

    private final AsyncTaskManager asyncTaskManager;

    @Override
    public void follow(long memberId, Long targetId) {
        Assert.isFalse(memberId == targetId, MemberErrorCode.CAN_NOT_FOLLOW_SELF::exception);
        RLock lock = redissonClient.getLock("MemberFollow-" + memberId);
        lock.lock();
        try {
            Long count = this.lambdaQuery().eq(MemberFollow::getMemberId, memberId)
                    .eq(MemberFollow::getFollowMemberId, targetId).count();
            Assert.isTrue(count == 0, MemberErrorCode.FOLLOWED::exception);

            MemberFollow mf = new MemberFollow();
            mf.setLogId(IdUtils.getSnowflakeId());
            mf.setMemberId(memberId);
            mf.setFollowMemberId(targetId);
            this.save(mf);
            this.updateMemberFollow(memberId, targetId, false);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void cancelFollow(long memberId, Long targetId) {
        RLock lock = redissonClient.getLock("MemberFollow-" + memberId);
        lock.lock();
        try {
            Optional<MemberFollow> opt = this.lambdaQuery().eq(MemberFollow::getMemberId, memberId)
                    .eq(MemberFollow::getFollowMemberId, targetId).oneOpt();
            if (opt.isPresent()) {
                this.removeById(opt.get());
                this.updateMemberFollow(memberId, targetId ,true);
            }
        } finally {
            lock.unlock();
        }
    }

    private void updateMemberFollow(final long memberId, final Long targetId, boolean cancel) {
        this.asyncTaskManager.execute(() -> {
            memberStatDataService.changeMemberStatData(memberId, FollowMemberStatData.TYPE, cancel ? -1 :1);
            memberStatDataService.changeMemberStatData(targetId, FollowerMemberStatData.TYPE, cancel ? -1 : 1);
        });
    }
}