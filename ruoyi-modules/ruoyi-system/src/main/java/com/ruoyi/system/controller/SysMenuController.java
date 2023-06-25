package com.ruoyi.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.domain.TreeNode;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.i18n.I18nUtils;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysMenu;
import com.ruoyi.system.enums.MenuType;
import com.ruoyi.system.permission.SysMenuPriv;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.service.ISysMenuService;
import com.ruoyi.system.service.ISysPermissionService;
import com.ruoyi.system.validator.LongId;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单信息
 * 
 * @author ruoyi
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/menu")
public class SysMenuController extends BaseRestController {

	private final ISysMenuService menuService;

	private final ISysPermissionService permissionService;

	/**
	 * 获取菜单列表
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysMenuList)
	@GetMapping("/list")
	public R<?> list(SysMenu menu) {
		LambdaQueryWrapper<SysMenu> q = new LambdaQueryWrapper<SysMenu>()
				.like(StringUtils.isNotEmpty(menu.getMenuName()), SysMenu::getMenuName, menu.getMenuName())
				.eq(StringUtils.isNotEmpty(menu.getStatus()), SysMenu::getStatus, menu.getStatus())
				.orderByAsc(SysMenu::getOrderNum);
		List<SysMenu> menus = this.menuService.list(q);
		return this.bindDataTable(menus);
	}

	/**
	 * 根据菜单编号获取详细信息
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysMenuList)
	@GetMapping(value = "/{menuId}")
	public R<?> getInfo(@PathVariable Long menuId) {
		SysMenu menu = menuService.getById(menuId);
		Assert.notNull(menu, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(menuId));
		return R.ok(menu);
	}

	/**
	 * 获取菜单下拉框树结构数据
	 */
	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/treeselect")
	public R<?> treeselect(SysMenu menu) {
		List<SysMenu> menus = this.menuService.lambdaQuery()
				.like(StringUtils.isNotEmpty(menu.getMenuName()), SysMenu::getMenuName, menu.getMenuName())
				.eq(StringUtils.isNotEmpty(menu.getStatus()), SysMenu::getStatus, menu.getStatus())
				.orderByAsc(SysMenu::getOrderNum).list();
		// 国际化翻译
		I18nUtils.replaceI18nFields(menus, LocaleContextHolder.getLocale());
		List<TreeNode<Long>> buildMenuTreeSelect = menuService.buildMenuTreeSelect(menus);
		return R.ok(buildMenuTreeSelect);
	}

	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/userTreeselect")
	public R<?> userTreeselect() {
		List<SysMenu> menus = this.menuService.lambdaQuery().ne(SysMenu::getMenuType, MenuType.Button.value())
				.orderByAsc(SysMenu::getOrderNum).list();

		List<String> menuPerms = StpAdminUtil.getLoginUser().getPermissions();
		if (!menuPerms.contains(ISysPermissionService.ALL_PERMISSION)) {
			menus = menus.stream().filter(m -> {
				return StringUtils.isEmpty(m.getPerms()) || menuPerms.contains(m.getPerms());
			}).toList();
		}
		// 国际化翻译
		I18nUtils.replaceI18nFields(menus, LocaleContextHolder.getLocale());
		List<TreeNode<Long>> buildMenuTreeSelect = menuService.buildMenuTreeSelect(menus);
		return R.ok(buildMenuTreeSelect);
	}

	/**
	 * 新增菜单
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysMenuAdd)
	@Log(title = "菜单管理", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> add(@Validated @RequestBody SysMenu menu) {
		menu.setCreateBy(StpAdminUtil.getLoginUser().getUsername());
		menuService.insertMenu(menu);
		return R.ok();
	}

	/**
	 * 修改菜单
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysMenuEdit)
	@Log(title = "菜单管理", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> edit(@Validated @RequestBody SysMenu menu) {
		menu.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
		menuService.updateMenu(menu);
		return R.ok();
	}

	/**
	 * 删除菜单
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysMenuRemove)
	@Log(title = "菜单管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{menuId}")
	public R<?> remove(@PathVariable("menuId") @LongId Long menuId) {
		menuService.deleteMenuById(menuId);
		return R.ok();
	}
}