package com.ruoyi.comment.controller.front;

import java.util.List;

import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.member.security.MemberUserType;
import com.ruoyi.system.annotation.IgnoreDemoMode;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.comment.domain.Comment;
import com.ruoyi.comment.domain.dto.SubmitCommentDTO;
import com.ruoyi.comment.service.ICommentApiService;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.member.security.StpMemberUtil;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comment")
public class CommentApiController extends BaseRestController {

	private final ICommentApiService commentApiService;

	/**
	 * 评论列表，按时间（ID）倒序
	 * 
	 * @param type
	 * @param dataId
	 * @return
	 */
	@GetMapping("/{type}/{dataId}")
	public R<?> getCommentList(@PathVariable("type") String type, @PathVariable("dataId") Long dataId,
			@RequestParam(value = "limit", required = false) Integer limit,
			@RequestParam(value = "offset", defaultValue = "0") Long offset) {
		List<Comment> list = this.commentApiService.getCommentList(type, dataId, limit, offset);
		return R.ok(list);
	}

	/**
	 * 获取评论回复列表，按时间（ID）倒序
	 * 
	 * @param commentId
	 * @param limit
	 * @param offset
	 * @return
	 */
	@GetMapping("/reply/{commentId}")
	public R<?> getCommentReplyList(@PathVariable @Min(1) Long commentId,
			@RequestParam(value = "limit", required = false) Integer limit,
			@RequestParam(value = "offset", defaultValue = "0") Long offset) {
		List<Comment> list = this.commentApiService.getCommentReplyList(commentId, limit, offset);
		return R.ok(list);
	}

	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
	@PostMapping
	public R<?> submitComment(@RequestBody SubmitCommentDTO dto) {
		dto.setOperator(StpMemberUtil.getLoginUser());
		this.commentApiService.submitComment(dto);
		return R.ok();
	}

	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
	@PutMapping("/like/{commentId}")
	public R<?> likeComment(@PathVariable @Min(1) Long commentId) {
		this.commentApiService.likeComment(commentId, StpMemberUtil.getLoginIdAsLong());
		return R.ok();
	}

	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
	@DeleteMapping("/{commentId}")
	public R<?> deleteMyComment(@PathVariable @Min(1) Long commentId) {
		this.commentApiService.deleteUserComment(StpMemberUtil.getLoginIdAsLong(), commentId);
		return R.ok();
	}
}
