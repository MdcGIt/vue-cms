package com.ruoyi.cms.member.controller.front;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.cms.member.domain.CmsMemberFavorites;
import com.ruoyi.cms.member.domain.vo.FavoriteContentVO;
import com.ruoyi.cms.member.service.IMemberFavoritesService;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.member.security.MemberUserType;
import com.ruoyi.member.security.StpMemberUtil;
import com.ruoyi.system.validator.LongId;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会员收藏数据API接口
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cms/favorites")
public class MemberFavoritesApiController extends BaseRestController {

	private final IMemberFavoritesService memberFavoritesService;

	private final IContentService contentService;

	/**
	 * 收藏内容列表
	 */
	@GetMapping("/list")
	public R<?> getMemberFavorites(@RequestParam(value = "pn", required = false, defaultValue = "1") @Min(1) Integer pageNumber,
								   @RequestParam(value = "ps", required = false, defaultValue = "16") @Min(1) Integer pageSize) {
		Page<CmsMemberFavorites> page = this.memberFavoritesService.page(new Page<>(pageNumber, pageSize, true));
		if (page.getTotal() == 0) {
			return this.bindDataTable(List.of());
		}
		List<Long> contentIds = page.getRecords().stream().map(CmsMemberFavorites::getContentId).toList();
		List<FavoriteContentVO> contents = this.contentService.listByIds(contentIds)
				.stream().map(FavoriteContentVO::newInstance).toList();
		return this.bindDataTable(contents, page.getTotal());
	}

	/**
	 * 是否收藏过指定内容
	 */
	@Priv(type = MemberUserType.TYPE)
	@GetMapping("/check/{contentId}")
	public R<?> isFavorited(@PathVariable @LongId Long contentId) {
		long memberId = StpMemberUtil.getLoginIdAsLong();
		Long count = this.memberFavoritesService.lambdaQuery().eq(CmsMemberFavorites::getMemberId, memberId)
				.eq(CmsMemberFavorites::getContentId, contentId).count();
		return R.ok(count);
	}

	/**
	 * 收藏内容
	 */
	@Priv(type = MemberUserType.TYPE)
	@PostMapping("/add/{contentId}")
	public R<?> favorite(@PathVariable @LongId Long contentId) {
		long memberId = StpMemberUtil.getLoginIdAsLong();
		this.memberFavoritesService.favoriteContent(memberId, contentId);
		return R.ok();
	}

	/**
	 * 取消收藏
	 */
	@Priv(type = MemberUserType.TYPE)
	@PostMapping("/cancel/{contentId}")
	public R<?> cancelFavorite(@PathVariable @LongId Long contentId) {
		long memberId = StpMemberUtil.getLoginIdAsLong();
		this.memberFavoritesService.cancelFavoriteContent(memberId, contentId);
		return R.ok();
	}
}
