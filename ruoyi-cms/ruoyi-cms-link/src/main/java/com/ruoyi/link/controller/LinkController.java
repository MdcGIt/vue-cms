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
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.link.domain.CmsLink;
import com.ruoyi.link.domain.dto.LinkDTO;
import com.ruoyi.link.priv.FriendLinkPriv;
import com.ruoyi.link.service.ILinkService;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 友情链接前端控制器
 * </p>
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@RestController
@RequestMapping("/cms/link")
@RequiredArgsConstructor
public class LinkController extends BaseRestController {

	private final ISiteService siteService;

	private final ILinkService linkService;

	@Priv(type = AdminUserType.TYPE, value = FriendLinkPriv.VIEW)
	@GetMapping
	public R<?> getPageList(@RequestParam("groupId") @Min(1) Long groupId,
			@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		Page<CmsLink> page = this.linkService.lambdaQuery().eq(CmsLink::getGroupId, groupId)
				.like(StringUtils.isNotEmpty(query), CmsLink::getName, query).orderByDesc(CmsLink::getLinkId)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		if (page.getRecords().size() > 0) {
			page.getRecords().forEach(link -> {
				if (StringUtils.isNotEmpty(link.getLogo())) {
					link.setSrc(InternalUrlUtils.getActualPreviewUrl(link.getLogo()));
				}
			});
		}
		return this.bindDataTable(page);
	}

	@Log(title = "新增友链", businessType = BusinessType.INSERT)
	@Priv(type = AdminUserType.TYPE, value = FriendLinkPriv.ADD)
	@PostMapping
	public R<?> add(@RequestBody LinkDTO dto) {
		CmsLink link = new CmsLink();
		BeanUtils.copyProperties(dto, link, "siteId", "sortFlag");

		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		link.setSiteId(site.getSiteId());
		link.setLinkId(IdUtils.getSnowflakeId());
		link.setSortFlag(SortUtils.getDefaultSortValue());
		link.createBy(StpAdminUtil.getLoginUser().getUsername());
		this.linkService.save(link);
		return R.ok();
	}

	@Log(title = "编辑友链", businessType = BusinessType.UPDATE)
	@Priv(type = AdminUserType.TYPE, value = { FriendLinkPriv.ADD, FriendLinkPriv.EDIT } )
	@PutMapping
	public R<String> edit(@RequestBody LinkDTO dto) {
		CmsLink link = new CmsLink();
		BeanUtils.copyProperties(dto, link, "siteId", "groupId", "sortFlag");
		link.updateBy(StpAdminUtil.getLoginUser().getUsername());
		this.linkService.updateById(link);
		return R.ok();
	}

	@Log(title = "删除友链", businessType = BusinessType.DELETE)
	@Priv(type = AdminUserType.TYPE, value = FriendLinkPriv.DELETE)
	@DeleteMapping
	public R<String> remove(@RequestBody @NotEmpty List<LinkDTO> dtoList) {
		List<Long> linkIds = dtoList.stream().map(LinkDTO::getLinkId).toList();
		this.linkService.removeByIds(linkIds);
		return R.ok();
	}
}
