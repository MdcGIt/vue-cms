package com.ruoyi.comment.controller;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.comment.domain.Comment;
import com.ruoyi.comment.domain.CommentLike;
import com.ruoyi.comment.domain.dto.AuditCommentDTO;
import com.ruoyi.comment.permission.CommentPriv;
import com.ruoyi.comment.service.ICommentLikeService;
import com.ruoyi.comment.service.ICommentService;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.validator.LongId;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/comment")
public class CommentController extends BaseRestController {

	private final ICommentService commentService;

	private final ICommentLikeService commentLikeService;

	@Priv(type = AdminUserType.TYPE, value = CommentPriv.View)
	@GetMapping
	public R<?> getCommentList(@RequestParam(required = false) String sourceType,
			@RequestParam(required = false) Long sourceId, @RequestParam(required = false) Long uid,
			@RequestParam(required = false) Integer auditStatus) {
		PageRequest pr = this.getPageRequest();

		Page<Comment> page = this.commentService.lambdaQuery()
				.eq(StringUtils.isNotEmpty(sourceType), Comment::getSourceType, sourceType)
				.eq(IdUtils.validate(sourceId), Comment::getSourceId, sourceId).eq(Comment::getParentId, 0)
				.eq(IdUtils.validate(uid), Comment::getUid, uid)
				.eq(Objects.nonNull(auditStatus), Comment::getAuditStatus, auditStatus)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));

		return this.bindDataTable(page);
	}

	@Priv(type = AdminUserType.TYPE, value = CommentPriv.View)
	@GetMapping("/reply/{commentId}")
	public R<?> getCommentReplyList(@PathVariable @LongId Long commentId) {
		PageRequest pr = this.getPageRequest();
		Page<Comment> page = this.commentService.lambdaQuery()
				.eq(IdUtils.validate(commentId), Comment::getParentId, commentId)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@Priv(type = AdminUserType.TYPE, value = CommentPriv.View)
	@GetMapping("/like/{commentId}")
	public R<?> getCommentLikeList(@PathVariable @LongId Long commentId, @RequestParam(required = false) Long uid) {
		PageRequest pr = this.getPageRequest();
		Page<CommentLike> page = this.commentLikeService.lambdaQuery()
				.eq(IdUtils.validate(commentId), CommentLike::getCommentId, commentId)
				.eq(IdUtils.validate(uid), CommentLike::getUid, uid).orderByDesc(CommentLike::getLogId)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@Priv(type = AdminUserType.TYPE, value = CommentPriv.Audit)
	@PutMapping("/audit")
	public R<?> auditComment(@RequestBody AuditCommentDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.commentService.auditComment(dto);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = CommentPriv.Delete)
	@DeleteMapping
	public R<?> deleteComment(@RequestBody @NotEmpty List<Long> commentIds) {
		this.commentService.deleteComments(commentIds);
		return R.ok();
	}
}
