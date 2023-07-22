package com.ruoyi.cms.member.controller.front;

import com.ruoyi.cms.member.service.IMemberLikeService;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.member.security.MemberUserType;
import com.ruoyi.member.security.StpMemberUtil;
import com.ruoyi.system.validator.LongId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 会员收藏数据API接口
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cms/like")
public class MemberLikeApiController extends BaseRestController {

	private final IMemberLikeService memberLikeService;

	/**
	 * 点赞内容
	 */
	@Priv(type = MemberUserType.TYPE)
	@PostMapping("/{contentId}")
	public R<?> likeContent(@PathVariable @LongId Long contentId) {
		long memberId = StpMemberUtil.getLoginIdAsLong();
		this.memberLikeService.likeContent(memberId, contentId);
		return R.ok();
	}

	/**
	 * 取消收藏
	 */
	@Priv(type = MemberUserType.TYPE)
	@PostMapping("/cancel/{contentId}")
	public R<?> cancelFavorite(@PathVariable @LongId Long contentId) {
		long memberId = StpMemberUtil.getLoginIdAsLong();
		this.memberLikeService.cancelLikeContent(memberId, contentId);
		return R.ok();
	}
}
