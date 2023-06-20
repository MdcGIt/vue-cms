package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.db.domain.BaseEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 通知公告表 sys_notice
 * 
 * @author ruoyi
 */
@Getter
@Setter
@TableName(SysNotice.TABLE_NAME)
public class SysNotice extends BaseEntity {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "sys_notice";

	/** 公告ID */
	@TableId(value = "notice_id", type = IdType.AUTO)
	private Long noticeId;

	/** 公告标题 */
	private String noticeTitle;

	/** 公告类型（1通知 2公告） */
	private String noticeType;

	/** 公告内容 */
	private String noticeContent;

	/** 公告状态（0正常 1关闭） */
	private String status;

	@NotBlank(message = "公告标题不能为空")
	@Size(min = 0, max = 50, message = "公告标题不能超过50个字符")
	public String getNoticeTitle() {
		return noticeTitle;
	}

}
