package com.leyou.item.mapper;

import com.leyou.item.pojo.Spu;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface SpuMapper extends Mapper<Spu> {

    @Update("UPDATE tb_spu SET saleable = #{saleable} WHERE id = #{id}")
    int updateSaleableById(@Param("id") Long id, @Param("saleable") Boolean saleable);
}
