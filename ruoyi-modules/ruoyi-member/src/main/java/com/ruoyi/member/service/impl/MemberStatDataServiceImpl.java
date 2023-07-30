package com.ruoyi.member.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.member.core.IMemberStatData;
import com.ruoyi.member.domain.Member;
import com.ruoyi.member.domain.MemberStatData;
import com.ruoyi.member.domain.vo.MemberCache;
import com.ruoyi.member.mapper.MemberStatDataMapper;
import com.ruoyi.member.service.IMemberService;
import com.ruoyi.member.service.IMemberStatDataService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MemberStatDataServiceImpl extends ServiceImpl<MemberStatDataMapper, MemberStatData> implements IMemberStatDataService {

    private static final String CACHE_PREFIX = "cc:member:";

    private final IMemberService memberService;

    private final RedissonClient redissonClient;

    private final RedisCache redisCache;

    private final Map<String, IMemberStatData> memberStatDataTypes;

    @Override
    public MemberCache getMemberCache(Long memberId) {
        return this.redisCache.getCacheObject(CACHE_PREFIX + memberId, () -> {
            Member member = memberService.getById(memberId);
            if (member == null) {
                return null;
            }
            MemberCache memberCache = new MemberCache();
            memberCache.setMemberId(memberId);
            if (StringUtils.isNotEmpty(member.getNickName())) {
                memberCache.setDisplayName(member.getNickName());
            } else {
                memberCache.setDisplayName(member.getUserName());
            }
            memberCache.setCover(member.getCover());
            memberCache.setAvatar(member.getAvatar());
            memberCache.setSlogan(member.getSlogan());
            MemberStatData data = getById(memberId);
            memberStatDataTypes.values().forEach(t -> {
                memberCache.getStat().put(t.getType(), Objects.nonNull(data) ? data.getValue(t.getField()) : 0);
            });
            return memberCache;
        });
    }

    @Override
    public void removeMemberCache(Long memberId) {
        this.redisCache.deleteObject(CACHE_PREFIX + memberId);
    }

    @Async
    @Override
    public void changeMemberStatData(Long memberId, String statDataType, Integer delta) {
        IMemberStatData memberStatData = this.getMemberStatData(statDataType);
        if (memberStatData == null) {
            return;
        }
        Member member = this.memberService.getById(memberId);
        if (member == null) {
            return;
        }
        RLock lock = redissonClient.getLock("MemberStatData_" + memberId);
        lock.lock();
        try {
            MemberStatData data = this.getById(memberId);
            if (data == null) {
                data = new MemberStatData();
                data.setMemberId(memberId);
            }

            Integer value = data.getValue(memberStatData.getField());
            if (value == null) {
                value = 0;
            }
            value += delta;
            data.setValue(memberStatData.getField(), value);
            this.saveOrUpdate(data);
            this.redisCache.deleteObject(CACHE_PREFIX + memberId);
        } finally {
            lock.unlock();
        }
    }

    private IMemberStatData getMemberStatData(String type) {
        return this.memberStatDataTypes.get(IMemberStatData.BEAN_PREFIX + type);
    }
}