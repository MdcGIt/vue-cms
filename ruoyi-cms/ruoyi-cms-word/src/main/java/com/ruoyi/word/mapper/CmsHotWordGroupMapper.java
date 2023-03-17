package com.ruoyi.word.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.word.domain.CmsHotWordGroup;

/**
 * <p>
 * 热词分组Mapper 接口
 * </p>
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
public interface CmsHotWordGroupMapper extends BaseMapper<CmsHotWordGroup> {

	@Select("select group_id from cms_hot_word_group where code in(#{codes})")
	public List<Long> getHotWordIdsByCode(List<String> codes);
}
