package com.ruoyi.advertisement.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.advertisement.domain.CmsAdHourStat;
import com.ruoyi.advertisement.mapper.CmsAdHourStatMapper;
import com.ruoyi.advertisement.service.IAdHourStatService;

import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 广告统计 服务实现类
 * </p>
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Service
@RequiredArgsConstructor
public class AdHourStatServiceImpl extends ServiceImpl<CmsAdHourStatMapper, CmsAdHourStat>
		implements IAdHourStatService {

}
