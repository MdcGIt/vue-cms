package com.ruoyi.contentcore.domain.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublishContentDTO {

	/**
	 * 内容ID列表
	 */
	@NotNull
	@NotEmpty(message = "内容IDs参数不能为空")
	private List<Long> contentIds;
}
