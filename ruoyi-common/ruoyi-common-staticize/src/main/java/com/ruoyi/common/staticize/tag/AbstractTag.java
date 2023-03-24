package com.ruoyi.common.staticize.tag;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.ruoyi.common.staticize.FreeMarkerUtils;
import com.ruoyi.common.staticize.enums.TagAttrDataType;
import com.ruoyi.common.utils.StringUtils;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * 自定义标签会作为共享变量添加到Configuration中。非线程安全！！！
 */
public abstract class AbstractTag implements ITag, TemplateDirectiveModel {
	
	/**
	 * 执行标签逻辑，返回标签变量
	 * 
	 * @param env
	 * @param attrs
	 * @return
	 * @throws TemplateException
	 * @throws IOException
	 */
	public abstract Map<String, TemplateModel> execute0(Environment env, Map<String, String> attrs)
			throws TemplateException, IOException;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void execute(Environment env, Map attrs, TemplateModel[] loopVars, TemplateDirectiveBody body)
			throws TemplateException, IOException {
		// 校验标签属性
		Map<String, String> tagAttrs = this.validTagAttributes(env, attrs);

		// 执行标签逻辑
		this.onTagStart(env, tagAttrs, body);
		Map<String, TemplateModel> tagVariables = this.execute0(env, tagAttrs);
		this.onTagEnd(env, tagAttrs, body, tagVariables);
	}

	/**
	 * execute0之前执行
	 * 
	 * @param env
	 * @param tagAttrs
	 * @param body
	 * @throws TemplateException
	 */
	public void onTagStart(Environment env, Map<String, String> tagAttrs, TemplateDirectiveBody body)
			throws TemplateException {
	}

	/**
	 * execute0之后执行
	 * 
	 * @param env
	 * @param tagAttrs
	 * @param body
	 * @param tagVariables
	 * @throws TemplateException
	 * @throws IOException
	 */
	public void onTagEnd(Environment env, Map<String, String> tagAttrs, TemplateDirectiveBody body,
			Map<String, TemplateModel> tagVariables) throws TemplateException, IOException {
		if (body != null) {
			if (tagVariables != null) {
				// 保存冲突变量，render后恢复
				Map<String, TemplateModel> conflictVariables = FreeMarkerUtils.setVariables(env, tagVariables);
				body.render(env.getOut());
				tagVariables.keySet().forEach(k -> env.setVariable(k, null));
				conflictVariables.entrySet().forEach(e -> env.setVariable(e.getKey(), e.getValue()));
			} else {
				body.render(env.getOut());
			}
		}
	}

	/**
	 * 校验标签属性并返回处理过的标签属性
	 * 
	 * @param env
	 * @param attrs
	 * @throws TemplateException
	 */
	Map<String, String> validTagAttributes(Environment env, Map<String, TemplateModel> attrs) throws TemplateException {
		CaseInsensitiveMap<String, String> map = null;
		List<TagAttr> tagAttrs = this.getTagAttrs();
		if (tagAttrs != null) {
			map = new CaseInsensitiveMap<>();
			for (TagAttr tagAttr : tagAttrs) {
				String attrValue = FreeMarkerUtils.getStringFrom(attrs, tagAttr.getName());
				if (tagAttr.isMandatory() && StringUtils.isEmpty(attrValue)) {
					throw new TemplateException(StringUtils.messageFormat("The tag <@{0}> missing required attribute: {1}", this.getTagName(), tagAttr.getName()),
							env);
				}
				if (tagAttr.getDataType() == TagAttrDataType.INTEGER && StringUtils.isNotEmpty(attrValue)
						&& !NumberUtils.isDigits(attrValue)) {
					throw new TemplateException(
							StringUtils.messageFormat("The tag <@{0}> attribute '{1}' must be digit, but is: {2}", this.getTagName(), tagAttr.getName(), attrValue),
							env);
				}
				if (tagAttr.getOptions() != null && StringUtils.isNotEmpty(attrValue)) {
					String[] optionValues = tagAttr.getOptions().keySet()
							.toArray(new String[tagAttr.getOptions().size()]);
					if (!ArrayUtils.contains(optionValues, attrValue)) {
						throw new TemplateException(StringUtils.messageFormat("The tag <@{0}> attribute '{1}' is invalid, options: {2}", this.getTagName(),
								tagAttr.getName(), Arrays.toString(optionValues)), env);
					}
				}
				if (StringUtils.isEmpty(attrValue) && StringUtils.isNotEmpty(tagAttr.getDefaultValue())) {
					attrValue = tagAttr.getDefaultValue();
				}
				map.put(tagAttr.getName(), attrValue);
			}
		}
		return map;
	}

	/**
	 * 构造模板变量
	 *
	 * @param env
	 * @param o
	 * @return
	 * @throws TemplateModelException
	 */
	public TemplateModel wrap(Environment env, Object o) throws TemplateModelException {
		return env.getObjectWrapper().wrap(o);
	}
}
