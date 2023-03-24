package com.ruoyi.contentcore.core;

import java.io.IOException;

import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.vo.ContentVO;

import jakarta.servlet.http.HttpServletRequest;

public interface IContentType extends Comparable<IContentType> {
	
	/**
	 * Bean名称前缀
	 */
	public static final String BEAN_NAME_PREFIX = "ContentType_";

	/**
	 * 内容类型唯一标识ID
	 */
    public String getId();

    /**
     * 内容类型名称
     */
    public String getName();

    /**
     * 图标
     */
    default public String getIcon() {
        return null;
    }

    /**
     * 内容扩展详情编辑组件
     */
    String getComponent();

    /**
     * 显示顺序
     */
    default public int getOrder() {
        return 0;
    }

    @Override
    default public int compareTo(IContentType o) {
        return this.getOrder() - o.getOrder();
    }

	/**
	 * 加载内容数据，根据cmsContent.contentId拉取内容扩展表数据
	 * 
	 * @param xContent
	 * @return
	 */
	public IContent<?> loadContent(CmsContent xContent);

    /**
     * 从请求读取内容数据
     * 
     * @param request
     * @return
     * @throws IOException
     */
    public IContent<?> readRequest(HttpServletRequest request) throws IOException;
    
    /**
     * 初始化内容编辑页面数据
     * 
     * @param catalogId
     * @param contentId
     * @return
     */
    public ContentVO initEditor(Long catalogId, Long contentId);
}
