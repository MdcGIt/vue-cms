package com.ruoyi.cms.image.mapper;

import com.ruoyi.common.db.DBConstants;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.cms.image.domain.CmsImage;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 图集图片Mapper 接口
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface CmsImageMapper extends BaseMapper<CmsImage> {

	/**
	 * 删除逻辑删除标识deleted=1的数据
	 *
	 * @param contentId
	 * @return
	 */
	@Delete("DELETE FROM cms_image WHERE deleted = " + DBConstants.DELETED_YES + " AND content_id = #{contentId}")
	Long deleteLogicDeletedByContentId(Long contentId);

	/**
	 * 删除站点内容，不受逻辑删除影响
	 *
	 * @param siteId
	 * @param limit
	 * @return
	 */
	@Delete("DELETE FROM cms_video WHERE site_id = #{siteId} limit ${limit}")
	Long deleteBySiteIdIgnoreLogicDel(@Param("siteId") Long siteId, @Param("limit") Integer limit);

	/**
	 * 查询站点内容，不受逻辑删除影响
	 *
	 * @param siteId
	 * @return
	 */
	@Select("SELECT count(*) FROM cms_video WHERE site_id = #{siteId}")
	Long selectCountBySiteIdIgnoreLogicDel(@Param("siteId") Long siteId);

	/**
	 * 设置 cms_image 的 deleted 逻辑删除标识值为0
	 *
	 * @param contentId
	 * @return
	 */
	@Update("UPDATE cms_image SET deleted = " + DBConstants.DELETED_NO + " WHERE content_id = #{contentId}")
    void recoverByContentId(Long contentId);
}
