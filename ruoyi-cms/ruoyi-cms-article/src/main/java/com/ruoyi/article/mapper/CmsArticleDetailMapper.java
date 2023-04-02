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
 * @email liweiyimwz@126.com
 */
public interface CmsArticleDetailMapper extends BaseMapper<CmsArticleDetail> {

	@Select("SELECT * FROM cms_article_detail_backup WHERE content_id = #{contentId}")
	CmsArticleDetail selectBackupByContentId(@Param("contentId") Long contentId);
	
	@Delete("DELETE FROM cms_article_detail_backup WHERE content_id = #{contentId}")
	Long deleteBackupByContentId(@Param("contentId") Long contentId);
}
