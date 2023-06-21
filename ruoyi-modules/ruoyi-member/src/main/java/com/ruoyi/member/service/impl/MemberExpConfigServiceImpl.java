package com.ruoyi.member.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ruoyi.common.utils.IdUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.member.domain.MemberExpConfig;
import com.ruoyi.member.domain.MemberLevel;
import com.ruoyi.member.domain.MemberLevelExpLog;
import com.ruoyi.member.exception.MemberErrorCode;
import com.ruoyi.member.level.IExpOperation;
import com.ruoyi.member.level.LevelManager;
import com.ruoyi.member.mapper.MemberExpConfigMapper;
import com.ruoyi.member.service.IMemberExpConfigService;
import com.ruoyi.member.service.IMemberLevelConfigService;
import com.ruoyi.member.service.IMemberLevelExpLogService;
import com.ruoyi.member.service.IMemberLevelService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MemberExpConfigServiceImpl extends ServiceImpl<MemberExpConfigMapper, MemberExpConfig>
		implements IMemberExpConfigService {

	private static final String CACHE_PREFIX = "member_exp_op:";

	private final RedisCache redisCache;

	private final Map<String, IExpOperation> expOperations;

	private final IMemberLevelService memberLevelService;

	private final IMemberLevelConfigService memberLevelConfigService;

	private final IMemberLevelExpLogService expLogService;

	private final AsyncTaskManager taskManager;
	
	private final RedissonClient redissonClient;

	@Override
	public IExpOperation getExpOperation(String opType) {
		IExpOperation eo = this.expOperations.get(IExpOperation.BEAN_PREFIX + opType);
		Assert.notNull(eo, () -> MemberErrorCode.UNSUPPORTED_EXP_OP.exception(opType));
		return eo;
	}

	@Override
	public Map<String, IExpOperation> getExpOperations() {
		return this.expOperations;
	}

	@Override
	public MemberExpConfig getMemberExpOperation(String opType, String levelType) {
		return this.redisCache.getCacheObject(CACHE_PREFIX + opType + ":" + levelType, () -> {
			return this.lambdaQuery().eq(MemberExpConfig::getOpType, opType)
					.eq(MemberExpConfig::getLevelType, levelType).one();
		});
	}

	@Override
	public void addExpOperation(MemberExpConfig expOp) {
		IExpOperation expOperation = this.getExpOperation(expOp.getOpType());

		Long count = this.lambdaQuery().eq(MemberExpConfig::getOpType, expOperation.getId())
				.eq(MemberExpConfig::getLevelType, expOp.getLevelType()).count();
		Assert.isTrue(count == 0,
				() -> MemberErrorCode.EXP_CONFIG_EXIST.exception(expOp.getOpType() + "=" + expOp.getLevelType()));

		expOp.setConfigId(IdUtils.getSnowflakeId());
		expOp.createBy(expOp.getCreateBy());
		this.save(expOp);
	}

	@Override
	public void updateExpOperation(MemberExpConfig expOp) {
		MemberExpConfig db = this.getById(expOp.getConfigId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("configId", expOp.getConfigId()));

		db.setExp(expOp.getExp());
		db.setDayLimit(expOp.getDayLimit());
		db.setTotalLimit(expOp.getTotalLimit());
		db.updateBy(expOp.getUpdateBy());
		this.updateById(db);
	}

	@Override
	public void deleteExpOperations(List<Long> expOperationIds) {
		this.removeByIds(expOperationIds);
	}

	@Override
	public void triggerExpOperation(String expOpType, Long memberId) {
		// 获取用户锁，避免并发导致经验值错误
		RLock lock = this.redissonClient.getLock("TriggerExpOp_" + memberId);
		lock.lock();
		try {
			List<MemberLevelExpLog> logs = new ArrayList<>();
			List<MemberExpConfig> list = this.lambdaQuery().eq(MemberExpConfig::getOpType, expOpType).list();
			for (MemberExpConfig memberExpOperation : list) {
				String levelType = memberExpOperation.getLevelType();
				MemberLevel memberLevel = this.memberLevelService.getMemberLevel(memberId, levelType);
				// 校验上限
				if (!this.checkLimit(memberExpOperation, memberId)) {
					continue;
				}
				// 执行通用经验值变更处理逻辑
				LevelManager levelManager = memberLevelConfigService.getLevelManager(memberExpOperation.getLevelType());
				levelManager.addExp(memberLevel, memberExpOperation.getExp());
				this.memberLevelService.updateById(memberLevel);
				// 记录经验值变更日志
				MemberLevelExpLog expLog = new MemberLevelExpLog();
				expLog.setLogTime(LocalDateTime.now());
				expLog.setLevelType(levelType);
				expLog.setMemberId(memberId);
				expLog.setOpType(expOpType);
				expLog.setChangeExp(memberExpOperation.getExp());
				expLog.setLevel(memberLevel.getLevel());
				expLog.setExp(memberLevel.getExp());
			}
			if (logs.size() > 0) {
				taskManager.execute(() -> this.expLogService.saveBatch(logs));
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 校验日/总次数上限
	 * 
	 * TODO: 数据量可以独立存储操作次数记录进行缓存优化
	 */
	private boolean checkLimit(MemberExpConfig memberExpOperation, Long memberId) {
		if (memberExpOperation.getDayLimit() > 0) {
			Long dayCount = this.expLogService.lambdaQuery().eq(MemberLevelExpLog::getMemberId, memberId)
					.eq(MemberLevelExpLog::getOpType, memberExpOperation.getOpType())
					.eq(MemberLevelExpLog::getLevelType, memberExpOperation.getLevelType())
					.ge(MemberLevelExpLog::getLogTime, DateUtils.getTodayStart()).count();
			if (dayCount >= memberExpOperation.getDayLimit()) {
				return false;
			}
		}
		if (memberExpOperation.getTotalLimit() > 0) {
			Long total = this.expLogService.lambdaQuery().eq(MemberLevelExpLog::getMemberId, memberId)
					.eq(MemberLevelExpLog::getOpType, memberExpOperation.getOpType())
					.eq(MemberLevelExpLog::getLevelType, memberExpOperation.getLevelType()).count();
			if (total >= memberExpOperation.getTotalLimit()) {
				return false;
			}
		}
		return true;
	}
}
