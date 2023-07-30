package com.ruoyi.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.member.domain.MemberFollow;
import com.ruoyi.member.domain.MemberStatData;
import com.ruoyi.member.domain.vo.MemberCache;
import com.ruoyi.member.service.impl.MemberStatDataServiceImpl;

public interface IMemberStatDataService extends IService<MemberStatData> {

    /**
     * 会员基础数据缓存
     *
     * @param memberId
     * @return
     */
    MemberCache getMemberCache(Long memberId);

    void removeMemberCache(Long memberId);

    /**
     * 更新统计数据
     *
     * @param memberId
     * @param statDataType
     * @param delta
     */
    void changeMemberStatData(Long memberId, String statDataType, Integer delta);
}