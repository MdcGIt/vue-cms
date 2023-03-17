package com.ruoyi.common.validation;

import java.util.Objects;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

/**
 * bean对象属性验证
 * 
 * @author ruoyi
 */
public class BeanValidators {
	
	public static void validateWithException(Validator validator, Object object, Class<?>... groups)
			throws ConstraintViolationException {
		if (Objects.nonNull(validator)) {
			Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object, groups);
			if (!constraintViolations.isEmpty()) {
				throw new ConstraintViolationException(constraintViolations);
			}
		}
	}
}
