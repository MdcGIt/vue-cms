package com.ruoyi.block.domain.vo;

import java.util.List;

import com.ruoyi.block.ManualPageWidgetType.RowData;
import com.ruoyi.contentcore.domain.vo.PageWidgetVO;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 动态区块页面部件展示对象
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter 
@Setter
@Accessors(chain = true)
public class ManualPageWidgetVO extends PageWidgetVO {
	
	/**
	 * 区块内容
	 */
    private List<RowData> content;
}
