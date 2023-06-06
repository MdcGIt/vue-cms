package com.ruoyi.article.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.article.domain.CmsArticleDetail;

/**
 * <p>
 * 文章Mapper 接口
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface CmsArticleDetailMapper extends BaseMapper<CmsArticleDetail> {

	@Select("SELECT backup_id FROM cms_article_detail_backup WHERE content_id = #{contentId}")
	Long selectBackupIdByContentId(@Param("contentId") Long contentId);
	
	@Select("SELECT count(*) FROM cms_article_detail_backup WHERE site_id = #{siteId}")
	Long selectBackupCountBySiteId(@Param("siteId") Long siteId);
	
	@Delete("DELETE FROM cms_article_detail_backup WHERE site_id = #{siteId} limit ${limit}")
	Long deleteBackupBySiteId(@Param("siteId") Long siteId, @Param("limit") Integer limit);
}
