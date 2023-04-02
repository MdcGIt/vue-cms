package com.ruoyi.advertisement.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.advertisement.IAdvertisementType;
import com.ruoyi.advertisement.domain.CmsAdvertisement;
import com.ruoyi.advertisement.mapper.CmsAdvertisementMapper;
import com.ruoyi.advertisement.pojo.dto.AdvertisementDTO;
import com.ruoyi.advertisement.service.IAdvertisementService;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.system.fixed.dict.EnableOrDisable;

import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 广告数据服务实现类
 * </p>
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Service
@RequiredArgsConstructor
public class AdvertisementServiceImpl extends ServiceImpl<CmsAdvertisementMapper, CmsAdvertisement>
		implements IAdvertisementService {

	private final Map<String, IAdvertisementType> advertisementTypes;

	@Override
	public IAdvertisementType getAdvertisementType(String typeId) {
		return this.advertisementTypes.get(IAdvertisementType.BEAN_NAME_PREFIX + typeId);
	}

	@Override
	public List<IAdvertisementType> getAdvertisementTypeList() {
		return this.advertisementTypes.values().stream().toList();
	}
	
	@Override
	public CmsAdvertisement addAdvertisement(AdvertisementDTO dto) {
		CmsAdvertisement advertisement = new CmsAdvertisement();
		BeanUtils.copyProperties(dto, advertisement);
		advertisement.setAdvertisementId(IdUtils.getSnowflakeId());
		advertisement.setState(EnableOrDisable.ENABLE);
		advertisement.createBy(dto.getOperator().getUsername());
		this.save(advertisement);
		return advertisement;
	}

	@Override
	public CmsAdvertisement saveAdvertisement(AdvertisementDTO dto) {
		CmsAdvertisement advertisement = this.getById(dto.getAdvertisementId());
		Assert.notNull(advertisement,
				() -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("advertisementId", dto.getAdvertisementId()));

		BeanUtils.copyProperties(dto, advertisement, "adSpaceId", "createTime", "createUser");
		advertisement.updateBy(dto.getOperator().getUsername());
		this.updateById(advertisement);
		return advertisement;
	}

	@Override
	@Transactional
	public void deleteAdvertisement(List<Long> advertisementIds) {
		this.removeByIds(advertisementIds);
	}

	@Override
	public void enableAdvertisement(List<Long> advertisementIds, String operator) {
		List<CmsAdvertisement> list = this.listByIds(advertisementIds);
		for (CmsAdvertisement ad : list) {
			if (!ad.isEnable()) {
				ad.setState(EnableOrDisable.ENABLE);
				ad.updateBy(operator);
			}
		}
		this.updateBatchById(list);
	}

	@Override
	public void disableAdvertisement(List<Long> advertisementIds, String operator) {
		List<CmsAdvertisement> list = this.listByIds(advertisementIds);
		for (CmsAdvertisement ad : list) {
			if (ad.isEnable()) {
				ad.setState(EnableOrDisable.DISABLE);
				ad.updateBy(operator);
			}
		}
		this.updateBatchById(list);
		// todo 重新发布
	}
}
