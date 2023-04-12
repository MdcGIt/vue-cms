package com.ruoyi.contentcore.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.contentcore.core.IPageWidget;
import com.ruoyi.contentcore.core.IPageWidgetType;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsPageWidget;
import com.ruoyi.contentcore.exception.ContentCoreErrorCode;
import com.ruoyi.contentcore.mapper.CmsPageWidgetMapper;
import com.ruoyi.contentcore.perms.PageWidgetPermissionType.PageWidgetPrivItem;
import com.ruoyi.contentcore.service.IPageWidgetService;
import com.ruoyi.contentcore.util.CmsPrivUtils;
import com.ruoyi.system.security.StpAdminUtil;

import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PageWidgetServiceImpl extends ServiceImpl<CmsPageWidgetMapper, CmsPageWidget>
		implements IPageWidgetService {

	private final Map<String, IPageWidgetType> pageWidgetTypes;

	@Override
	public IPageWidgetType getPageWidgetType(String type) {
		IPageWidgetType pwt = this.pageWidgetTypes.get(IPageWidgetType.BEAN_NAME_PREFIX + type);
		Assert.notNull(pwt, () -> ContentCoreErrorCode.UNSUPPORT_PAGE_WIDGET_TYPE.exception());
		return pwt;
	}

	@Override
	public List<IPageWidgetType> getPageWidgetTypes() {
		return this.pageWidgetTypes.values().stream().collect(Collectors.toList());
	}

	@Override
	public boolean checkCodeUnique(Long siteId, String code, Long pageWidgetId) {
		LambdaQueryWrapper<CmsPageWidget> q = new LambdaQueryWrapper<CmsPageWidget>().eq(CmsPageWidget::getCode, code)
				.eq(CmsPageWidget::getSiteId, siteId)
				.ne(pageWidgetId != null && pageWidgetId > 0, CmsPageWidget::getPageWidgetId, pageWidgetId)
				.last("limit 1");
		return this.count(q) == 0;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addPageWidget(IPageWidget pw) {
		boolean checkCodeUnique = checkCodeUnique(pw.getPageWidgetEntity().getSiteId(),
				pw.getPageWidgetEntity().getCode(), null);
		Assert.isTrue(checkCodeUnique,
				() -> CommonErrorCode.DATA_CONFLICT.exception("code: " + pw.getPageWidgetEntity().getCode()));

		pw.add();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void savePageWidget(IPageWidget pw) {
		CmsPageWidget pageWidget = this.getById(pw.getPageWidgetEntity().getPageWidgetId());
		Assert.notNull(pageWidget, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("pagewidgetId",
				pw.getPageWidgetEntity().getPageWidgetId()));
		CmsPrivUtils.checkPageWidgetPermission(pageWidget.getPageWidgetId(), PageWidgetPrivItem.Edit, pw.getOperator());

		boolean checkCodeUnique = checkCodeUnique(pw.getPageWidgetEntity().getSiteId(),
				pw.getPageWidgetEntity().getCode(), pw.getPageWidgetEntity().getPageWidgetId());
		Assert.isTrue(checkCodeUnique,
				() -> CommonErrorCode.DATA_CONFLICT.exception("code: " + pw.getPageWidgetEntity().getCode()));

		pw.save();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deletePageWidgets(List<Long> pageWidgetIds) {
		List<CmsPageWidget> pageWidgets = this.listByIds(pageWidgetIds);
		for (CmsPageWidget pageWidget : pageWidgets) {
			IPageWidgetType pwt = this.getPageWidgetType(pageWidget.getType());
			IPageWidget pw = pwt.loadPageWidget(pageWidget);
			pw.delete();
		}
	}

	@Override
	public void deletePageWidgetsByCatalog(CmsCatalog catalog) {
		List<CmsPageWidget> list = this.lambdaQuery().likeRight(CmsPageWidget::getCatalogAncestors, catalog.getAncestors()).list();
		for (int i = 0; i < list.size(); i++) {
			CmsPageWidget pageWidget = list.get(i);
			AsyncTaskManager.setTaskProgressInfo((i * 100) / list.size(), "正在删除页面部件：" + i + " / " + list.size());
			IPageWidgetType pwt = this.getPageWidgetType(pageWidget.getType());
			IPageWidget pw = pwt.loadPageWidget(pageWidget);
			pw.delete();
		}
	}

	@Override
	public void publishPageWidgets(List<Long> pageWidgetIds) throws TemplateException, IOException {
		List<CmsPageWidget> pageWidgets = this.listByIds(pageWidgetIds);
		for (CmsPageWidget pageWidget : pageWidgets) {
			IPageWidgetType pwt = this.getPageWidgetType(pageWidget.getType());
			IPageWidget pw = pwt.loadPageWidget(pageWidget);
			pw.setOperator(StpAdminUtil.getLoginUser());
			pw.publish();
		}
	}
}
