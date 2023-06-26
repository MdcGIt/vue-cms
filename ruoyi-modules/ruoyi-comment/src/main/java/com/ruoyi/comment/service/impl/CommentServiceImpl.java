package com.ruoyi.comment.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.comment.CommentConsts;
import com.ruoyi.comment.domain.Comment;
import com.ruoyi.comment.domain.dto.AuditCommentDTO;
import com.ruoyi.comment.fixed.dict.CommentAuditStatus;
import com.ruoyi.comment.mapper.CommentMapper;
import com.ruoyi.comment.service.ICommentService;
import com.ruoyi.system.fixed.dict.YesOrNo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void auditComment(AuditCommentDTO dto) {
		int auditStatus = YesOrNo.isYes(dto.getAuditFlag()) ? CommentAuditStatus.PASSED : CommentAuditStatus.NOT_PASSED;
		this.lambdaUpdate().set(Comment::getAuditStatus, auditStatus).in(Comment::getCommentId, dto.getCommentIds())
				.update();
	}

	/**
	 * 后台删除评论仅做删除标识，不影响上级评论回复数，前端依然可见评论/回复项，但是内容不可见
	 */
	@Override
	public void deleteComments(List<Long> commentIds) {
		this.deleteComments(commentIds);
	}
}