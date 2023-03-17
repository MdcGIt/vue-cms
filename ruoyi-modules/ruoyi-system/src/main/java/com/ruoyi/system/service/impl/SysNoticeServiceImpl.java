package com.ruoyi.system.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.domain.dto.SysNoticeDTO;
import com.ruoyi.system.mapper.SysNoticeMapper;
import com.ruoyi.system.service.ISysNoticeService;

/**
 * 公告 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements ISysNoticeService {

	/**
	 * 新增公告
	 * 
	 * @param notice
	 *            公告信息
	 * @return 结果
	 */
	@Override
	public void insertNotice(SysNoticeDTO dto) {
		SysNotice notice = new SysNotice();
		notice.setNoticeTitle(dto.getNoticeTitle());
		notice.setNoticeType(dto.getNoticeType());
		notice.setNoticeContent(dto.getNoticeContent());
		notice.setStatus(dto.getStatus());
		notice.setRemark(dto.getRemark());
		notice.createBy(dto.getOperator().getUsername());
		this.save(notice);
	}

	/**
	 * 修改公告
	 * 
	 * @param notice
	 *            公告信息
	 * @return 结果
	 */
	@Override
	public void updateNotice(SysNoticeDTO dto) {
		SysNotice db = this.getById(dto.getNoticeId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("noticeId", dto.getNoticeId()));

		db.setNoticeTitle(dto.getNoticeTitle());
		db.setNoticeType(dto.getNoticeType());
		db.setNoticeContent(dto.getNoticeContent());
		db.setStatus(dto.getStatus());
		db.setRemark(dto.getRemark());
		db.updateBy(dto.getOperator().getUsername());
		this.updateById(db);
	}

	/**
	 * 批量删除公告信息
	 * 
	 * @param noticeIds
	 *            需要删除的公告ID
	 * @return 结果
	 */
	@Override
	public void deleteNoticeByIds(List<Long> noticeIds) {
		this.removeBatchByIds(noticeIds);
	}
}
