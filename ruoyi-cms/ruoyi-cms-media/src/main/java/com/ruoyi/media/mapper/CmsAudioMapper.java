package com.ruoyi.media.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.media.domain.CmsAudio;

/**
 * <p>
 * 音频集集音频数据Mapper 接口
 * </p>
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
public interface CmsAudioMapper extends BaseMapper<CmsAudio> {

	@Select("SELECT * FROM cms_audio_backup WHERE content_id = #{contentId}")
	List<CmsAudio> selectBackupByContentId(@Param("contentId") Long contentId);

	@Delete("DELETE FROM cms_audio_backup WHERE content_id = #{contentId}")
	Long deleteBackupByContentId(@Param("contentId") Long contentId);
}
