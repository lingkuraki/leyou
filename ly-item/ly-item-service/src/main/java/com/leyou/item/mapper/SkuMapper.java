package com.leyou.item.mapper;

import com.leyou.item.pojo.Sku;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuMapper extends Mapper<Sku> {

    @Delete("DELETE FROM tb_sku WHERE spu_id = #{spuId}")
    int deleteBySpuId(@Param("spuId") Long spuIid);

    @Select("SELECT id FROM tb_sku WHERE spu_id = #{spuId}")
    List<Long> selectSkuIdBySpuId(@Param("spuId") Long spuId);
}
