package com.ruoyi.cms.image.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
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

	@Select("SELECT * FROM cms_image_backup WHERE content_id = #{contentId}")
	List<CmsImage> selectBackupByContentId(@Param("contentId") Long contentId);

	@Delete("DELETE FROM cms_image_backup WHERE content_id = #{contentId}")
	Long deleteBackupByContentId(@Param("contentId") Long contentId);
}
