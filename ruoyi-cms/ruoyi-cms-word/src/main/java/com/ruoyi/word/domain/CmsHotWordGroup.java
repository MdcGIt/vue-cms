package com.ruoyi.word.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.mybatisplus.domain.BaseEntity;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * 热词分组表对应 [cms_hot_word_group]
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Getter
@Setter
@TableName(CmsHotWordGroup.TABLE_NAME)
public class CmsHotWordGroup extends BaseEntity {

    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "cms_hot_word_group";

    @TableId(value = "group_id", type = IdType.INPUT)
    private Long groupId;

    /**
     * 所属站点ID
     */
    private Long siteId;

    /**
     * 名称
     */
    @NotEmpty
    private String name;

    /**
     * 编码，唯一标识
     */
    @NotEmpty
	@Pattern(regexp = "^[A-Za-z0-9_]+$", message = "热词分组编码只能使用大小写字母、数字和下划线")
    private String code;
    
    /**
     * 排序标识
     */
    private Long sortFlag;
}
