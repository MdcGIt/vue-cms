package com.ruoyi.contentcore.properties;

import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IProperty;
import com.ruoyi.contentcore.enums.WatermarkerPosition;
import com.ruoyi.contentcore.util.ConfigPropertyUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * 图片水印参数配置
 */
@RequiredArgsConstructor
@Component(IProperty.BEAN_NAME_PREFIX + ImageWatermarkArgsProperty.ID)
public class ImageWatermarkArgsProperty implements IProperty {

	public final static String ID = "ImageWatermarkArgs";
	
	static UseType[] UseTypes = new UseType[] { UseType.Site };
	
	@Override
	public UseType[] getUseTypes() {
		return UseTypes;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "图片水印参数";
	}
	
	@Override
	public String defaultValue() {
		return JacksonUtils.to(new ImageWatermarkArgs());
	}

	@Override
	public Class<?> valueClass() {
		return ImageWatermarkArgs.class;
	}
	
	public static ImageWatermarkArgs getImageWatermarkArgs(Map<String, Object> configProps) {
		String stringValue = ConfigPropertyUtils.getStringValue(ID, configProps);
		ImageWatermarkArgs args = JacksonUtils.from(stringValue, ImageWatermarkArgs.class);
		if (Objects.isNull(args)) {
			args = new ImageWatermarkArgs();
		}
		return args;
	}
	
	@Getter
	@Setter
	public static class ImageWatermarkArgs {
		
		private String image = StringUtils.EMPTY;
		
		private String position = WatermarkerPosition.TOP_RIGHT.name();
		
		private float opacity = 1f;

		private float ratio = 30f;
	}
}
