package com.ruoyi.contentcore.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.vo.RecycleContentVO;
import com.ruoyi.contentcore.service.impl.SiteStatServiceImpl.SiteStatData;

public interface CmsContentMapper extends BaseMapper<CmsContent> {
	
	/**
	 * 按内容类型分组统计内容数量
	 * 
	 * @param siteId
	 * @return
	 */
	@Select("""
			<script>
			SELECT content_type dataKey, COUNT(*) dataValue FROM cms_content
			<if test=' siteId != null and siteId != 0 '> WHERE site_id = #{siteId}  </if>
			GROUP BY content_type
			</script>
			""")
	List<SiteStatData> countContentGroupByType(@Param("siteId") Long siteId);

	@Select("""
			<script>
			SELECT * FROM cms_content_backup WHERE site_id = #{siteId} 
			<if test=' catalogId != null and catalogId != 0 '> AND catalog_id = #{catalogId} </if>
			<if test=' status != null and status != "" '> AND status = #{status} </if>
			<if test=' contentType != null and contentType != "" '> AND content_type = #{contentType} </if>
			<if test=' title != null and title != "" '> AND title LIKE #{title} </if>
			ORDER BY backup_id DESC
			</script>
			""")
	public Page<RecycleContentVO> selectRecycleContentList(IPage<RecycleContentVO> page, @Param("siteId") Long siteId,
			@Param("catalogId") Long catalogId, @Param("contentType") String contentType, @Param("status") String status,
			@Param("title") String title);
	
	/**
	 * 获取内容备份表数据
	 * 
	 * @param backupIds
	 * @return
	 */
	@Select("""
			<script>
			SELECT * FROM cms_content_backup WHERE backup_id in (
			<foreach item="backupId" collection="backupIds" separator=",">
			#{backupId}
			</foreach>
			)
			</script>
			""")
	public List<RecycleContentVO> selectRecycleContentByBackupIds(@Param("backupIds") List<Long> backupIds);
	
	/**
	 * 获取指定备份时间之前的内容备份表数据总数
	 * 
	 * @param backupTime
	 * @return
	 */
	@Select("SELECT count(*) FROM cms_content_backup WHERE backup_time < #{backupTime}")
	public Long selectRecycleContentCountBefore(@Param("backupTime") LocalDateTime backupTime);
	
	/**
	 * 获取指定备份时间之前的内容备份表数据
	 * 
	 * @param page
	 * @param backupTime
	 * @return
	 */
	@Select("SELECT * FROM cms_content_backup WHERE backup_time < #{backupTime}")
	public List<RecycleContentVO> selectRecycleContentBefore(IPage<RecycleContentVO> page, @Param("backupTime") LocalDateTime backupTime);
	
	/**
	 * 删除站点下备份内容数据
	 * 
	 * @param siteId
	 * @return
	 */
	@Delete("DELETE FROM cms_content_backup WHERE site_id = #{siteId} limit ${limit}")
	public Long deleteRecycleContentsBySiteId(@Param("siteId") Long siteId, @Param("limit") Integer limit);
	
	/**
	 * 查询站点备份内容
	 * 
	 * @param siteId
	 * @return
	 */
	@Select("SELECT count(*) FROM cms_content_backup WHERE site_id = #{siteId}")
	Long selectBackupCountBySiteId(@Param("siteId") Long siteId);
}
