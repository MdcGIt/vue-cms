package com.ruoyi.contentcore.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.vo.RecycleContentVO;

public interface CmsContentMapper extends BaseMapper<CmsContent> {

	@Select("""
			<script>
			SELECT * FROM cms_content_backup WHERE site_id = #{siteId} 
			<if test=' catalogId != null and catalogId != 0 '> and catalog_id = #{catalogId} </if>
			<if test=' status != null and status != "" '> and status = #{status} </if>
			<if test=' contentType != null and contentType != "" '> and content_type = #{contentType} </if>
			<if test=' title != null and title != "" '> and title like #{title} </if>
			order by backup_id desc
			</script>
			""")
	public Page<RecycleContentVO> selectRecycleContentList(IPage<RecycleContentVO> page, @Param("siteId") Long siteId,
			@Param("catalogId") Long catalogId, @Param("contentType") String contentType, @Param("status") String status,
			@Param("title") String title);

	@Select("SELECT * FROM cms_content_backup ${ew.customSqlSegment}")
	public Page<RecycleContentVO> selectRecycleContentPage(IPage<RecycleContentVO> page,
			@Param(Constants.WRAPPER) Wrapper<RecycleContentVO> wrapper);

	@Select("""
			<script>
			SELECT * FROM cms_content_backup WHERE backup_id in (
			<foreach item="backupId" collection="backupIds" separator=",">
			#{backupId}
			</foreach>
			)
			</script>
			""")
	public List<CmsContent> selectRecycleContentListByBackupIds(@Param("backupIds") List<Long> backupIds);

	@Delete("DELETE FROM cms_content_backup WHERE content_id = #{contentId}")
	Long deleteBackupByContentId(@Param("contentId") Long contentId);
}
