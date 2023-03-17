package com.ruoyi.contentcore.util;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsPageWidget;

public class PageWidgetUtils {

	public static String getStaticFileName(CmsPageWidget pw, String staticSuffix) {
		return pw.getCode() + StringUtils.DOT + staticSuffix;
	}
}
