package com.ruoyi.xmodel.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.xmodel.domain.XModelData;

/**
 * <p>
 * 扩展模型数据Mapper 接口
 * </p>
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
public interface XModelDataMapper extends BaseMapper<XModelData> {
	
	@Select("select * from ${tableName} where pk_value=#{pkValue} limit 1")
	public Map<String, Object> getCustomModelData(@Param("tableName") String tableName, 
			@Param("pkValue") String pkValue);
	
	@Update("update ${tableName} set ${updateFields} where pk_value = #{pkValue}")
	public boolean udpateCustomModelData(@Param("tableName") String tableName, 
			@Param("updateFields") String updateFields,
			@Param("pkValue") String pkValue);

	@Insert("insert into ${tableName} (${insertFields}) values (${insertValues})")
	public void insertCustomModelData(@Param("tableName") String tableName, 
			@Param("insertFields") String insertFields,
			@Param("insertValues") String insertValues);

}
