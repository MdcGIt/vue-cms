package com.ruoyi.media.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.mybatisplus.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 音频数据表对象 [cms_audio]
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Getter
@Setter
@TableName(CmsAudio.TABLE_NAME)
public class CmsAudio extends BaseEntity {

    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "cms_audio";

    @TableId(value = "audio_id", type = IdType.INPUT)
    private Long audioId;

    /**
     * 所属内容ID
     */
    private Long contentId;
    
    /**
     * 所属站点ID
     */
    private long siteId;

    /**
     * 音频标题
     */
    private String title;
    
    /**
     * 作者
     */
    private String author;
    
    /**
     * 简介
     */
    private String description;

    /**
     * 音频类型
     */
    private String type;

    /**
     * 音频相对站点资源目录路径
     */
    private String path;

    /**
     * 预览路径
     */
    @TableField(exist = false)
    private String src;

    /**
     * 音频文件大小
     */
    private Long fileSize;
    
    @TableField(exist = false)
    private String fileSizeName;
    
    /**
     * 音频格式
     */
    private String format;

    /**
     * 音频时长，单位：毫秒
     */
    private Long duration;
    
    /**
     * 编码方式
     */
    private String decoder;
    
    /**
     * 声道数
     */
    private Integer channels;
    
    /**
     * 比特率
     */
    private Integer bitRate;
    
    /**
     * 采样率
     */
    private Integer samplingRate;

    /**
     * 排序字段
     */
    private Integer sortFlag;
}
