package com.ruoyi.system.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.domain.dto.SysNoticeDTO;

/**
 * 公告 服务层
 * 
 * @author ruoyi
 */
public interface ISysNoticeService extends IService<SysNotice> {
	
	/**
	 * 新增公告
	 * 
	 * @param notice
	 *            公告信息
	 * @return 结果
	 */
	public void insertNotice(SysNoticeDTO dto);

	/**
	 * 修改公告
	 * 
	 * @param notice
	 *            公告信息
	 * @return 结果
	 */
	public void updateNotice(SysNoticeDTO dto);

	/**
	 * 批量删除公告信息
	 * 
	 * @param noticeIds
	 *            需要删除的公告ID
	 * @return 结果
	 */
	public void deleteNoticeByIds(List<Long> noticeIds);
}
