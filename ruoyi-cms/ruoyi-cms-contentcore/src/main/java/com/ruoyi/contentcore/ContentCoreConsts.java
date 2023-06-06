package com.ruoyi.contentcore;

public interface ContentCoreConsts {

	/**
	 * 资源预览地址前缀
	 */
	public String RESOURCE_PREVIEW_PREFIX = "preview/";

	/**
	 * 缓存命名空间
	 */
	public String CACHE_NAME = "cms";

	/**
	 * 栏目树最大层级，受限制于CmsCatalog.ancestors字段长度
	 */
	public int CATALOG_MAX_TREE_LEVEL = 16;

	/**
	 * 当前站点Header键名
	 */
	public String Header_CurrentSite = "CurrentSite";

	/**
	 * 模板目录，固定在站点发布通道目录下的template目录中
	 */
	public String TemplateDirectory = "template/";

	/**
	 * 扩展配置属性字段模板变量前缀
	 */
	public String ConfigPropFieldPrefix = "extend_";
}
