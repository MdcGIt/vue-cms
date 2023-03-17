package com.ruoyi.contentcore.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.contentcore.domain.CmsSiteProperty;
import com.ruoyi.contentcore.mapper.CmsSitePropertyMapper;
import com.ruoyi.contentcore.service.ISitePropertyService;

@Service
public class SitePropertyServiceImpl extends ServiceImpl<CmsSitePropertyMapper, CmsSiteProperty> implements ISitePropertyService {

	@Override
	public R<String> addSiteProperty(CmsSiteProperty siteProperty) {
		if (!checkUnique(siteProperty.getPropCode(), siteProperty.getSiteId(), null)) {
			return R.fail("属性代码不能重复");
		}
		siteProperty.setPropertyId(IdUtils.getSnowflakeId());
		this.save(siteProperty);
		return R.ok();
	}

	@Override
	public R<String> saveSiteProperty(CmsSiteProperty siteProperty) {
		CmsSiteProperty prop = this.getById(siteProperty.getPropertyId());
		if (prop == null) {
			return R.fail("数据ID错误");
		}
		if (!checkUnique(siteProperty.getPropCode(), siteProperty.getSiteId(), siteProperty.getPropertyId())) {
			return R.fail("属性代码不能重复");
		}
		prop.setPropName(siteProperty.getPropName());
		prop.setPropCode(siteProperty.getPropCode());
		prop.setPropValue(siteProperty.getPropValue());
		prop.setRemark(siteProperty.getRemark());
		prop.updateBy(siteProperty.getUpdateBy());
		this.updateById(prop);
		return R.ok();
	}

	@Override
	public R<String> deleteSiteProperties(List<Long> propertyIds) {
		if (!this.removeByIds(propertyIds)) {
			return R.fail("数据库操作异常");
		}
		return R.ok();
	}

    private boolean checkUnique(String propCode, Long siteId, Long propertyId) {
        LambdaQueryWrapper<CmsSiteProperty> q = new LambdaQueryWrapper<CmsSiteProperty>()
        		.eq(CmsSiteProperty::getSiteId, siteId)
                .eq(CmsSiteProperty::getPropCode, propCode)
                .ne(propertyId != null && propertyId > 0, CmsSiteProperty::getPropertyId, propertyId);
        return this.count(q) == 0;
    }
}
