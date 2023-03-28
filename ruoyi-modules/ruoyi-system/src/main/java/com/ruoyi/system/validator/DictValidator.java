package com.ruoyi.system.validator;

import java.util.Optional;

import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysDictData;
import com.ruoyi.system.service.ISysDictTypeService;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DictValidator implements ConstraintValidator<Dict, String> {

	private String dictType;

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		Optional<SysDictData> findFirst = SpringUtils.getBean(ISysDictTypeService.class).selectDictDatasByType(dictType)
				.stream().filter(data -> StringUtils.equals(data.getDictValue(), value)).findFirst();
		if (findFirst.isPresent()) {
			return true;
		}
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate("Invalid dict value: " + value).addConstraintViolation();
		return false;
	}

	@Override
	public void initialize(Dict dict) {
		this.dictType = dict.value();
	}
}