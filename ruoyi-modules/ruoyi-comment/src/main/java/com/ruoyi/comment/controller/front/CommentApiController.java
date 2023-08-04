package com.ruoyi.comment.controller.front;

import com.ruoyi.comment.domain.Comment;
import com.ruoyi.comment.domain.dto.SubmitCommentDTO;
import com.ruoyi.comment.domain.vo.CommentVO;
import com.ruoyi.comment.service.ICommentApiService;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.member.security.MemberUserType;
import com.ruoyi.member.security.StpMemberUtil;
import com.ruoyi.system.annotation.IgnoreDemoMode;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
			@RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit,
			@RequestParam(value = "offset", defaultValue = "0") Long offset) {
		List<CommentVO> list = this.commentApiService.getCommentList(type, dataId, limit, offset);
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
	@PostMapping("/submit")
	public R<?> submitComment(@RequestBody SubmitCommentDTO dto) {
		dto.setOperator(StpMemberUtil.getLoginUser());
		dto.setClientIp(ServletUtils.getIpAddr(ServletUtils.getRequest()));
		dto.setUserAgent(ServletUtils.getUserAgent());
		Comment comment = this.commentApiService.submitComment(dto);
		return R.ok(comment);
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
