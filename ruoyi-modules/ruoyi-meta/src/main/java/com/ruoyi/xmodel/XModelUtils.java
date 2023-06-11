package com.ruoyi.xmodel;

public class XModelUtils {

	/**
	 * 字段选择项类型：文本
	 * <p>格式：</p>
	 */
	public final static String OPTIONS_TYPE_TEXT = "text";

	/**
	 * 字段选择项类型：字典数据
	 */
	public final static String OPTIONS_TYPE_DICT = "dict";

	/**
	 * 系统内置扩展模型数据表
	 */
	public final static String DEFAULT_MODEL_VALUE_TABLE = "x_model_data";

	/**
	 * 自定义扩展模型数据字段键值前缀
	 */
	public static final String DATA_FIELD_PREFIX = "ModelData_";
	
	/**
	 * 自定义扩展模型数据表表名前缀
	 */
	public final static String CUSTOM_TABLE_NAME_PRFIX = "xmd_";

	/**
	 * 自定义扩展模型数据表主键字段名
	 */
	public static final String PRIMARY_FIELD_NAME = "pk_value";
	
	public static boolean isDefaultTable(String tableName) {
		return DEFAULT_MODEL_VALUE_TABLE.equalsIgnoreCase(tableName);
	}
}
