package com.ruoyi.advertisement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.advertisement.domain.CmsAdvertisement;
import com.ruoyi.advertisement.service.IAdvertisementService;
import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.contentcore.core.AbstractPageWidget;

/**
 * 广告位
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class AdSpacePageWidget extends AbstractPageWidget {

	private final IAdvertisementService advertisementService = SpringUtils.getBean(IAdvertisementService.class);

	@Override
	public void delete() {
		super.delete();
		// 删除广告版位相关的广告
		this.advertisementService.remove(new LambdaQueryWrapper<CmsAdvertisement>()
				.eq(CmsAdvertisement::getAdSpaceId, this.getPageWidgetEntity().getPageWidgetId()));
		// TODO 删除广告统计数据
	}
}
