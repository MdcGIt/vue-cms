package com.ruoyi.contentcore.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsSiteProperty;
import com.ruoyi.contentcore.perms.ContentCorePriv;
import com.ruoyi.contentcore.service.ISitePropertyService;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.validator.LongId;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

/**
 * 站点自定义属性管理
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE, value = ContentCorePriv.SiteView)
@RestController
@RequestMapping("/cms/site/prop")
@RequiredArgsConstructor
public class SitePropertyController extends BaseRestController {

	private final ISitePropertyService sitePropertyService;

	/**
	 * 查询站点自定义属性列表
	 * 
	 * @param siteId
	 *            站点ID
	 * @param query
	 *            属性名称/编码
	 * @return
	 */
	@GetMapping("/list")
	public R<?> list(@RequestParam("siteId") String siteId,
			@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<CmsSiteProperty> q = new LambdaQueryWrapper<CmsSiteProperty>()
				.eq(CmsSiteProperty::getSiteId, siteId).and(StringUtils.isNotEmpty(query), wrapper -> wrapper
						.like(CmsSiteProperty::getPropName, query).or().like(CmsSiteProperty::getPropCode, query));
		Page<CmsSiteProperty> page = sitePropertyService.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true),
				q);
		return this.bindDataTable(page);
	}

	/**
	 * 获取站点自定义属性详情
	 * 
	 * @param propertyId
	 *            属性ID
	 * @return
	 */
	@GetMapping(value = "/{siteId}")
	public R<?> getInfo(@PathVariable @LongId Long propertyId) {
		CmsSiteProperty siteProperty = sitePropertyService.getById(propertyId);
		if (siteProperty == null) {
			return R.fail("站点数据未找到：" + propertyId);
		}
		return R.ok(siteProperty);
	}

	/**
	 * 新增站点自定义数据
	 * 
	 * @param siteProperty
	 * @return
	 * @throws IOException
	 */
	@Log(title = "新增站点属性", businessType = BusinessType.INSERT)
	@PostMapping
	public R<String> addSiteProperty(@RequestBody @Validated CmsSiteProperty siteProperty) throws IOException {
		siteProperty.createBy(StpAdminUtil.getLoginUser().getUsername());
		return this.sitePropertyService.addSiteProperty(siteProperty);
	}

	/**
	 * 修改站点自定义属性
	 * 
	 * @param siteProperty
	 * @return
	 * @throws IOException
	 */
	@Log(title = "编辑站点属性", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<String> editSiteProperty(@RequestBody @Validated CmsSiteProperty siteProperty) throws IOException {
		siteProperty.updateBy(StpAdminUtil.getLoginUser().getUsername());
		return this.sitePropertyService.saveSiteProperty(siteProperty);
	}

	/**
	 * 删除站点自定义属性
	 * 
	 * @param propertyIds
	 *            自定义属性IDs
	 * @return
	 * @throws IOException
	 */
	@Log(title = "删除站点属性", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<String> removeSiteProperties(@RequestBody @NotEmpty List<Long> propertyIds) throws IOException {
		return this.sitePropertyService.deleteSiteProperties(propertyIds);
	}
}