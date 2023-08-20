package com.ruoyi.contentcore.domain.dto;

import com.ruoyi.common.security.domain.BaseDTO;
import com.ruoyi.system.validator.LongId;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SiteExportDTO extends BaseDTO {
	
	/**
	 * 所属站点ID
	 */
    @LongId
	private Long siteId;

    /**
     * 导出目录
     */
    private List<String> directories;
}
