package com.ruoyi.system.config.converter;

import java.util.Objects;
import java.util.Optional;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.ruoyi.common.i18n.I18nUtils;
import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.annotation.ExcelDictField;
import com.ruoyi.system.domain.SysDictData;
import com.ruoyi.system.service.ISysDictTypeService;

public class DictConverter implements Converter<Object> {

	private ISysDictTypeService dictService = SpringUtils.getBean(ISysDictTypeService.class);

	@Override
	public Class<?> supportJavaTypeKey() {
		return Object.class;
	}

	@Override
	public CellDataTypeEnum supportExcelTypeKey() {
		return null;
	}

	@Override
	public WriteCellData<String> convertToExcelData(Object value, ExcelContentProperty contentProperty,
			GlobalConfiguration cfg) throws Exception {
		if (Objects.nonNull(value)) {
			ExcelDictField excelDictField = contentProperty.getField().getAnnotation(ExcelDictField.class);
			if (Objects.nonNull(excelDictField) && StringUtils.isNotEmpty(excelDictField.value())) {
				Optional<SysDictData> opt = dictService.optDictData(excelDictField.value(), value.toString());
				if (opt.isPresent()) {
					return new WriteCellData<>(I18nUtils.get(opt.get().getDictLabel(), cfg.getLocale()));
				}
			}
		}
		return new WriteCellData<>(Objects.isNull(value) ? StringUtils.EMPTY : value.toString());
	}

	@Override
	public Object convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
			GlobalConfiguration cfg) throws Exception {
		String value = cellData.getStringValue();
		ExcelDictField excelDictField = contentProperty.getField().getAnnotation(ExcelDictField.class);
		if (Objects.nonNull(excelDictField) && StringUtils.isNotEmpty(excelDictField.value())) {
			Optional<SysDictData> opt = dictService.selectDictDatasByType(excelDictField.value()).stream().filter(d -> {
				return StringUtils.equals(cellData.getStringValue(), I18nUtils.get(d.getDictLabel(), cfg.getLocale()));
			}).findFirst();
			if (opt.isPresent()) {
				return opt.get().getDictValue();
			}
		}
		return value;
	}
}