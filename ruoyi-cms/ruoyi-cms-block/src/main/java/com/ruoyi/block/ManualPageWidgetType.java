package com.ruoyi.block;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.ruoyi.block.domain.vo.ManualPageWidgetVO;
import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IPageWidget;
import com.ruoyi.contentcore.core.IPageWidgetType;
import com.ruoyi.contentcore.domain.CmsPageWidget;
import com.ruoyi.contentcore.domain.vo.PageWidgetVO;
import com.ruoyi.contentcore.util.InternalUrlUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * 手动控制区块页面部件<br/>
 * 此区块内容支持多行多列自定义控制
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@RequiredArgsConstructor
@Component(IPageWidgetType.BEAN_NAME_PREFIX + ManualPageWidgetType.ID)
public class ManualPageWidgetType implements IPageWidgetType {

	public final static String ID = "manual";
	
	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "CONTENTCORE.PAGEWIDGET." + ID;
	}

	@Override
	public String getIcon() {
		return "el-icon-list";
	}
	
	@Override
	public String getRoute() {
		return "/cms/block/manual/editor";
	}

	@Override
	public IPageWidget loadPageWidget(CmsPageWidget cmsPageWdiget) {
		ManualPageWidget pw = new ManualPageWidget();
		pw.setPageWidgetEntity(cmsPageWdiget);
		return pw;
	}
	
	@Override
	public IPageWidget newInstance() {
		return new ManualPageWidget();
	}
	
	@Override
	public PageWidgetVO getPageWidgetVO(CmsPageWidget pageWidget) {
		ManualPageWidgetVO vo = new ManualPageWidgetVO();
		BeanUtils.copyProperties(pageWidget, vo);
		vo.setContent(this.parseContent(pageWidget, null, true));
		return vo;
	}
	
	@Override
	public List<RowData> parseContent(CmsPageWidget pageWidget, String publishPipeCode, boolean isPreview) {
		List<RowData> list = new ArrayList<>();
		if (StringUtils.isNotEmpty(pageWidget.getContent())) {
			list = JacksonUtils.fromList(pageWidget.getContent(), RowData.class);
			list.forEach(row -> {
				row.items.forEach(item -> {
					item.logoSrc = InternalUrlUtils.getActualUrl(item.getLogo(), publishPipeCode, isPreview);
					if (StringUtils.isNotEmpty(publishPipeCode)) {
						item.url = InternalUrlUtils.getActualUrl(item.getUrl(), publishPipeCode, isPreview);
					}
				});
			});
		}
		return list;
	}
	

	@Getter
	@Setter
	public static class RowData {
		
		private List<ItemData> items;
	}

	@Getter
	@Setter
	public static class ItemData {
		
		private String title;
		
		private String titleStyle;
		
		private String summary;
		
		private String url;
		
		private String logo;
		
		private String logoSrc;
		
		private LocalDateTime publishDate;
	}
}
