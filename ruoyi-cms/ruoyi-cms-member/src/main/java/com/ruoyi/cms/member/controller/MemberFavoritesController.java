package com.ruoyi.cms.member.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.cms.member.domain.CmsMemberFavorites;
import com.ruoyi.cms.member.service.IMemberFavoritesService;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.system.security.AdminUserType;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会员收藏数据
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/cms/member/favorites")
public class MemberFavoritesController extends BaseRestController {

	private final ISiteService siteService;

	private final IMemberFavoritesService memberFavoritesService;

	@GetMapping
	public R<?> getMemberFavorites() {
		CmsSite currentSite = this.siteService.getCurrentSite(ServletUtils.getRequest());

		PageRequest pr = this.getPageRequest();
		Page<CmsMemberFavorites> page = this.memberFavoritesService.lambdaQuery()
				.eq(CmsMemberFavorites::getSiteId, currentSite.getSiteId())
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@DeleteMapping
	public R<?> deleteMemberFavorites(@RequestBody @NotEmpty final List<Long> dataIds) {
		this.memberFavoritesService.deleteMemberFavorites(dataIds);
		return R.ok();
	}
}

