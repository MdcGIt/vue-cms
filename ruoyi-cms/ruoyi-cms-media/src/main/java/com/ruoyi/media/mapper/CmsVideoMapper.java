package com.ruoyi.media.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.media.domain.CmsVideo;

/**
 * <p>
 * 视频集集音频数据Mapper 接口
 * </p>
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
public interface CmsVideoMapper extends BaseMapper<CmsVideo> {

	@Select("SELECT * FROM cms_video_backup WHERE content_id = #{contentId}")
	List<CmsVideo> selectBackupByContentId(@Param("contentId") Long contentId);

	@Delete("DELETE FROM cms_video_backup WHERE content_id = #{contentId}")
	Long deleteBackupByContentId(@Param("contentId") Long contentId);
}
