package com.ruoyi.contentcore.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.mybatisplus.domain.BaseEntity;
import com.ruoyi.system.fixed.dict.EnableOrDisable;

import lombok.Getter;
import lombok.Setter;

/**
 * 发布通道表对象 [cms_publishpipe]
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Getter
@Setter
@TableName(CmsPublishPipe.TABLE_NAME)
public class CmsPublishPipe extends BaseEntity {

    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "cms_publishpipe";

	public static final String SitePropery_Url = "url";

	/**
	 * 发布通道ID
	 */
    @TableId(value = "publishpipe_id", type = IdType.INPUT)
    private Long publishpipeId;

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 名称
     */
    private String name;

    /**
     * 编码
     */
    private String code;

    /**
     * 发布通道状态
     */
    private String state;

    /**
     * 排序
     */
    private Long sort;
    
    public boolean isEnable() {
    	return EnableOrDisable.isEnable(this.state);
    }
}
