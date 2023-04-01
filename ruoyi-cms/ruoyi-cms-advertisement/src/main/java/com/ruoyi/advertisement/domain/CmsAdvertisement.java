package com.ruoyi.advertisement.domain;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.mybatisplus.domain.BaseEntity;
import com.ruoyi.system.fixed.dict.EnableOrDisable;

import lombok.Getter;
import lombok.Setter;

/**
 * 广告详情表对象 [cms_advertisement]
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Getter
@Setter
@TableName(CmsAdvertisement.TABLE_NAME)
public class CmsAdvertisement extends BaseEntity {

	private static final long serialVersionUID = 1L;
    
    public static final String TABLE_NAME = "cms_advertisement";

	@TableId(value = "advertisement_id", type = IdType.INPUT)
    private Long advertisementId;
	
	/**
	 * 所属广告版位ID（等同页面部件ID）
	 */
	private Long adSpaceId;
	
	/**
	 * 类型
	 */
    private String type;
    
    /**
     * 类型名称
     */
    @TableField(exist = false)
    private String typeName;

    /**
     * 名称
     */
    private String name;

    /**
     * 权重
     */
    private Integer weight;

    /**
     * 关键词
     */
    private String keywords;

    /**
     * 状态
     * 
     * @see EnableOrDisable
     */
    private String state;
    
    /**
     * 上线时间
     */
    private LocalDateTime onlineDate;

    /**
     * 下线时间
     */
    private LocalDateTime offlineDate;
    
    /**
     * 跳转链接
     */
    private String redirectUrl;
    
    /**
     * 素材链接
     */
    private String resourcePath;
    
    public boolean isEnable() {
    	return EnableOrDisable.isEnable(this.state);
    }
}
