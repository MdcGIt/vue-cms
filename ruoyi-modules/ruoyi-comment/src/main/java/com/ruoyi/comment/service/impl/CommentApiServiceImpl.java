package com.ruoyi.comment.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.comment.CommentConsts;
import com.ruoyi.comment.domain.Comment;
import com.ruoyi.comment.domain.CommentLike;
import com.ruoyi.comment.domain.dto.SubmitCommentDTO;
import com.ruoyi.comment.exception.CommentErrorCode;
import com.ruoyi.comment.fixed.dict.CommentAuditStatus;
import com.ruoyi.comment.listener.event.AfterCommentSubmitEvent;
import com.ruoyi.comment.mapper.CommentLikeMapper;
import com.ruoyi.comment.mapper.CommentMapper;
import com.ruoyi.comment.service.ICommentApiService;
import com.ruoyi.comment.service.ICommentService;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IP2RegionUtils;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.ServletUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CommentApiServiceImpl implements ICommentApiService, ApplicationContextAware {

	private final CommentMapper commentMapper;

	private final CommentLikeMapper commentLikeMapper;

	private final ICommentService commentService;

	private final AsyncTaskManager asyncTaskManager;

	private final RedissonClient redissonClient;

	private ApplicationContext applicationContext;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void likeComment(Long commentId, long uid) {
		Comment comment = this.commentMapper.selectById(commentId);
		Assert.notNull(comment, CommentErrorCode.API_COMMENT_NOT_FOUND::exception);

		this.incrCommentLikeCount(comment, uid);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void cancelLikeComment(Long commentId, long uid) {
		Comment comment = this.commentMapper.selectById(commentId);
		Assert.notNull(comment, CommentErrorCode.API_COMMENT_NOT_FOUND::exception);

		this.decrCommentLikeCount(comment, uid);
	}

	private void incrCommentLikeCount(Comment comment, long uid) {
		this.changeCommentLikeCount(comment, uid, true);
	}

	private void decrCommentLikeCount(Comment comment, long uid) {
		this.changeCommentLikeCount(comment, uid, false);
	}

	private void changeCommentLikeCount(Comment comment, long uid, boolean increase) {
		RLock lock = redissonClient.getLock("CommentLike-" + comment.getCommentId());
		lock.lock();
		try {
			if (increase) {
				Long count = this.commentLikeMapper.selectCount(new LambdaQueryWrapper<CommentLike>()
						.eq(CommentLike::getCommentId, comment.getCommentId()).eq(CommentLike::getUid, uid));
				if (count > 0) {
					return;
				}
				this.commentMapper.incrCommentLikeCount(comment.getCommentId());
				CommentLike commentLike = new CommentLike();
				commentLike.setCommentId(comment.getCommentId());
				commentLike.setUid(uid);
				commentLike.setLikeTime(LocalDateTime.now());
				this.commentLikeMapper.insert(commentLike);
			} else {
				this.commentLikeMapper.delete(new LambdaQueryWrapper<CommentLike>()
						.eq(CommentLike::getCommentId, comment.getCommentId()).eq(CommentLike::getUid, uid));
				this.commentMapper.decrCommentLikeCount(comment.getCommentId());
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void submitComment(SubmitCommentDTO dto) {
		Comment comment = new Comment();
		comment.setCommentId(IdUtils.getSnowflakeId());
		comment.setSourceType(dto.getSourceType());
		comment.setSourceId(dto.getSourceId());
		comment.setUid(dto.getOperator().getUserId());
		comment.setContent(dto.getContent()); // TODO 敏感词过滤
		comment.setCommentTime(LocalDateTime.now());
		comment.setAuditStatus(CommentAuditStatus.PASSED);
		comment.setDelFlag(0);
		comment.setLikeCount(0);
		comment.setParentId(dto.getCommentId());
		comment.setReplyCount(0);
		comment.setReplyUid(dto.getReplyUid());
		comment.setIp(dto.getClientIp());
		comment.setUserAgent(dto.getUserAgent());
		comment.setLocation(IP2RegionUtils.ip2Region(dto.getClientIp()));
		comment.setClientType(ServletUtils.getDeviceType(dto.getUserAgent()));
		this.commentService.save(comment);
		// 提供扩展点
		this.applicationContext.publishEvent(new AfterCommentSubmitEvent(this, comment));
		// 如果是回复，修改父级评论回复数
		if (comment.getParentId() > 0) {
			asyncTaskManager.execute(() -> {
				incrCommentReplyCount(comment.getParentId());
			});
		}

	}

	@Override
	public List<Comment> getCommentList(String type, Long sourceId, Integer limit, Long offset) {
		List<Comment> list = this.commentService.lambdaQuery().eq(Comment::getSourceType, type)
				.eq(Comment::getSourceId, sourceId).eq(Comment::getParentId, 0)
				.eq(Comment::getAuditStatus, CommentAuditStatus.PASSED).gt(Comment::getCommentId, offset)
				.orderByDesc(Comment::getCommentId).last("limit " + limit).list();
		list.forEach(comment -> {
			if (comment.isDeleted()) {
				comment.setContent(null); // 已删除评论内容不返回
			}
			if (comment.getReplyCount() > 0) {
				List<Comment> replyList = this.getCommentReplyList(comment.getCommentId(), 2, 0L);
				comment.setReplyList(replyList);
			}
		});
		return list;
	}

	@Override
	public List<Comment> getCommentReplyList(Long commentId, Integer limit, Long offset) {
		List<Comment> list = this.commentService.lambdaQuery().eq(Comment::getParentId, commentId)
				.eq(Comment::getAuditStatus, CommentAuditStatus.PASSED).gt(Comment::getCommentId, offset)
				.orderByDesc(Comment::getCommentId).last("limit " + limit).list();
		list.forEach(comment -> {
			if (comment.isDeleted()) {
				comment.setContent(null); // 已删除的评论内容不返回
			}
		});
		return list;
	}

	@Override
	public void deleteUserComment(Long userId, Long commentId) {
		Comment comment = this.commentService.getById(commentId);
		Assert.notNull(comment, CommentErrorCode.API_COMMENT_NOT_FOUND::exception);
		Assert.isTrue(comment.getUid() == userId, CommentErrorCode.API_ACCESS_DENY::exception);
		// 直接评论且存在未删除回复的不直接删除，标记为删除状态
		if (comment.getParentId() == 0) {
			if (comment.getReplyCount() > 0) {
				List<Comment> list = this.commentService.lambdaQuery().eq(Comment::getParentId, comment.getCommentId())
						.list();
				long actReplyCount = list.stream().filter(c -> !c.isDeleted()).count();
				if (actReplyCount > 0) {
					comment.setDelFlag(CommentConsts.DELETE_FLAG);
					this.commentService.updateById(comment);
				} else {
					this.commentService.removeById(comment.getCommentId());
					if (list.size() > 0) {
						this.commentService.removeByIds(list);
					}
				}
			} else {
				this.commentService.removeById(comment.getCommentId());
			}
		} else {
			this.commentService.removeById(comment);
			// 修改上级评论回复数
			this.decrCommentReplyCount(comment.getParentId());
		}
	}

	/**
	 * 修改评论回复数+1/-1
	 * 
	 * @param commentId 评论ID
	 * @param increase  是否增加
	 */
	private void changeCommentReplyCount(Long commentId, boolean increase) {
		RLock lock = redissonClient.getLock("Comment-" + commentId);
		lock.lock();
		try {
			if (increase) {
				this.commentMapper.incrCommentReplyCount(commentId);
			} else {
				this.commentMapper.decrCommentReplyCount(commentId);
			}
		} finally {
			lock.unlock();
		}
	}

	private void incrCommentReplyCount(Long commentId) {
		this.changeCommentReplyCount(commentId, true);
	}

	private void decrCommentReplyCount(Long commentId) {
		this.changeCommentReplyCount(commentId, false);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
