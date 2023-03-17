package com.ruoyi.contentcore.template.exception;

import com.ruoyi.common.utils.StringUtils;

import freemarker.core.Environment;
import freemarker.template.TemplateException;

public class CatalogNotFoundException extends TemplateException {

	private static final long serialVersionUID = 1L;

	public CatalogNotFoundException(String tag, long catalogId, Environment env) {
		super(StringUtils.messageFormat("<@{0}>[id: {1}]", tag, catalogId), env);
	}
}
