package com.ruoyi.member.controller.front;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.member.domain.MemberLike;
import com.ruoyi.member.domain.dto.LikeDTO;
import com.ruoyi.member.security.MemberUserType;
import com.ruoyi.member.security.StpMemberUtil;
import com.ruoyi.member.service.IMemberLikeService;
import com.ruoyi.system.annotation.IgnoreDemoMode;
import com.ruoyi.system.validator.LongId;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 会员收藏数据API接口
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = MemberUserType.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member/like")
public class MemberLikeApiController extends BaseRestController {

	private final IMemberLikeService memberLikeService;

	/**
	 * 点赞内容
	 */
	@GetMapping("/check")
	public R<?> checkLike(@RequestParam @NotEmpty String dataType, @RequestParam @LongId Long dataId) {
		long memberId = StpMemberUtil.getLoginIdAsLong();
		Long count = this.memberLikeService.lambdaQuery()
				.eq(MemberLike::getMemberId, memberId)
				.eq(MemberLike::getDataType, dataType)
				.eq(MemberLike::getDataId, dataId)
				.count();
		return R.ok(count > 0);
	}

	/**
	 * 点赞内容
	 */
	@IgnoreDemoMode
	@PostMapping
	public R<?> likeContent(@RequestBody @Validated LikeDTO dto) {
		long memberId = StpMemberUtil.getLoginIdAsLong();
		this.memberLikeService.like(memberId, dto.getDataType(), dto.getDataId());
		return R.ok();
	}

	/**
	 * 取消收藏
	 */
	@IgnoreDemoMode
	@DeleteMapping
	public R<?> cancelFavorite(@RequestBody @Validated LikeDTO dto) {
		long memberId = StpMemberUtil.getLoginIdAsLong();
		this.memberLikeService.cancelLike(memberId, dto.getDataType(), dto.getDataId());
		return R.ok();
	}
}
