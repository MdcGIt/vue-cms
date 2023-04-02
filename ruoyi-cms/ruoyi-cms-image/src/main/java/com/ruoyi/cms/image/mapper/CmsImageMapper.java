package com.ruoyi.cms.image.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.cms.image.domain.CmsImage;

/**
 * <p>
 * 图集图片Mapper 接口
 * </p>
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
public interface CmsImageMapper extends BaseMapper<CmsImage> {

	/**
	 * 查询备份表ID
	 * 
	 * @param contentId
	 * @return
	 */
	@Select("SELECT backup_id FROM cms_image_backup WHERE content_id = #{contentId}")
	List<Long> selectBackupIdsByContentId(@Param("contentId") Long contentId);
}
