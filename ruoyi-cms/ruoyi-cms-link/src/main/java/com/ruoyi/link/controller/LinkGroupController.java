package com.ruoyi.link.controller;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.SortUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.link.domain.CmsLinkGroup;
import com.ruoyi.link.domain.dto.LinkGroupDTO;
import com.ruoyi.link.permission.FriendLinkPriv;
import com.ruoyi.link.service.ILinkGroupService;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 友情链接分组前端控制器
 * </p>
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/cms/link_group")
public class LinkGroupController extends BaseRestController {

	private final ISiteService siteService;

	private final ILinkGroupService linkGroupService;

	@Priv(type = AdminUserType.TYPE, value = FriendLinkPriv.View)
	@GetMapping
	public R<?> getPageList(@RequestParam(value = "query", required = false) String query) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<CmsLinkGroup> q = new LambdaQueryWrapper<CmsLinkGroup>()
				.eq(CmsLinkGroup::getSiteId, site.getSiteId())
				.like(StringUtils.isNotEmpty(query), CmsLinkGroup::getName, query);
		Page<CmsLinkGroup> page = this.linkGroupService.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
		return this.bindDataTable(page);
	}

	@Log(title = "新增友链分组", businessType = BusinessType.INSERT)
	@Priv(type = AdminUserType.TYPE, value = FriendLinkPriv.Add)
	@PostMapping
	public R<?> add(@RequestBody LinkGroupDTO dto) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		if (this.linkGroupService.lambdaQuery().eq(CmsLinkGroup::getSiteId, site.getSiteId())
				.eq(CmsLinkGroup::getCode, dto.getCode()).count() > 0) {
			return R.fail("友链分组编码重复");
		}
		CmsLinkGroup linkGroup = new CmsLinkGroup();
		BeanUtils.copyProperties(dto, linkGroup, "linkGroupId", "siteId", "sortFlag");
		linkGroup.setSiteId(site.getSiteId());
		linkGroup.setLinkGroupId(IdUtils.getSnowflakeId());
		linkGroup.setSortFlag(SortUtils.getDefaultSortValue());
		linkGroup.createBy(StpAdminUtil.getLoginUser().getUsername());
		this.linkGroupService.save(linkGroup);
		return R.ok();
	}

	@Log(title = "编辑友链分组", businessType = BusinessType.UPDATE)
	@Priv(type = AdminUserType.TYPE, value = { FriendLinkPriv.Add, FriendLinkPriv.Edit })
	@PutMapping
	public R<String> edit(@RequestBody LinkGroupDTO dto) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		if (this.linkGroupService.lambdaQuery().eq(CmsLinkGroup::getSiteId, site.getSiteId())
				.eq(CmsLinkGroup::getCode, dto.getCode()).ne(CmsLinkGroup::getLinkGroupId, dto.getLinkGroupId())
				.count() > 0) {
			return R.fail("友链分组编码重复");
		}
		CmsLinkGroup linkGroup = new CmsLinkGroup();
		BeanUtils.copyProperties(dto, linkGroup, "siteId", "sortFlag");
		linkGroup.updateBy(StpAdminUtil.getLoginUser().getUsername());
		this.linkGroupService.updateById(linkGroup);
		return R.ok();
	}

	@Log(title = "删除友链分组", businessType = BusinessType.DELETE)
	@Priv(type = AdminUserType.TYPE, value = FriendLinkPriv.Delete)
	@DeleteMapping
	public R<String> remove(@RequestBody @NotEmpty List<LinkGroupDTO> dtoList) {
		List<Long> linkGroupIds = dtoList.stream().map(LinkGroupDTO::getLinkGroupId).toList();
		this.linkGroupService.deleteLinkGroup(linkGroupIds);
		return R.ok();
	}
}
