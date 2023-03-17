package com.ruoyi.block;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.ruoyi.block.ManualPageWidgetType.RowData;
import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.contentcore.core.AbstractPageWidget;
import com.ruoyi.contentcore.domain.CmsPageWidget;

/**
 * 手动控制区块
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
public class ManualPageWidget extends AbstractPageWidget {
	
	@Override
	public void add() {
		this.dealContentIngoreFields();
		super.add();
	}
	
	@Override
	public void save() {
		this.dealContentIngoreFields();
		super.save();
	}
	
	private void dealContentIngoreFields() {
		CmsPageWidget pageWidgetEntity = this.getPageWidgetEntity();
		List<RowData> rows = JacksonUtils.fromList(pageWidgetEntity.getContent(), RowData.class);
		if (Objects.nonNull(rows)) {
			rows.forEach(row -> {
				row.getItems().forEach(item -> item.setLogoSrc(null));
			});
		} else {
			rows = Collections.emptyList();
		}
		pageWidgetEntity.setContent(JacksonUtils.to(rows));
	}
}
