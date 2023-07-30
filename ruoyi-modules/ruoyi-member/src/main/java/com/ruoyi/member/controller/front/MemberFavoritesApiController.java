package com.ruoyi.member.controller.front;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.member.domain.MemberFavorites;
import com.ruoyi.member.domain.dto.FavoriteDTO;
import com.ruoyi.member.security.MemberUserType;
import com.ruoyi.member.security.StpMemberUtil;
import com.ruoyi.member.service.IMemberFavoritesService;
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
@RequestMapping("/api/member/favorites")
public class MemberFavoritesApiController extends BaseRestController {

	private final IMemberFavoritesService memberFavoritesService;

	/**
	 * 是否收藏过指定内容
	 */
	@GetMapping("/check")
	public R<?> isFavorited(@RequestParam @NotEmpty String dataType, @RequestParam @LongId Long dataId) {
		long memberId = StpMemberUtil.getLoginIdAsLong();
		Long count = this.memberFavoritesService.lambdaQuery()
				.eq(MemberFavorites::getMemberId, memberId)
				.eq(MemberFavorites::getDataType, dataType)
				.eq(MemberFavorites::getDataId, dataId)
				.count();
		return R.ok(count > 0);
	}

	/**
	 * 收藏内容
	 */
	@PostMapping
	public R<?> favorite(@RequestBody @Validated FavoriteDTO dto) {
		long memberId = StpMemberUtil.getLoginIdAsLong();
		this.memberFavoritesService.favorite(memberId, dto.getDataType(), dto.getDataId());
		return R.ok();
	}

	/**
	 * 取消收藏
	 */
	@DeleteMapping
	public R<?> cancelFavorite(@RequestBody @Validated FavoriteDTO dto) {
		long memberId = StpMemberUtil.getLoginIdAsLong();
		this.memberFavoritesService.cancelFavorite(memberId, dto.getDataType(), dto.getDataId());
		return R.ok();
	}
}
