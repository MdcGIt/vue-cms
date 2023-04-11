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

	/**
	 * 查询备份表ID
	 * 
	 * @param contentId
	 * @return
	 */
	@Select("SELECT backup_id FROM cms_video_backup WHERE content_id = #{contentId}")
	List<Long> selectBackupIdsByContentId(@Param("contentId") Long contentId);
	
	@Select("SELECT count(*) FROM cms_video_backup WHERE site_id = #{siteId}")
	Long selectBackupCountBySiteId(@Param("siteId") Long siteId);
	
	@Delete("DELETE FROM cms_video_backup WHERE site_id = #{siteId} limit ${limit}")
	Long deleteBackupBySiteId(@Param("siteId") Long siteId, @Param("limit") Integer limit);
}
