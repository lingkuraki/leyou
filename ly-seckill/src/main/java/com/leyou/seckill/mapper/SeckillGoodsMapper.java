package com.leyou.seckill.mapper;

import com.leyou.seckill.pojo.SeckillGoods;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface SeckillGoodsMapper extends Mapper<SeckillGoods> {
    @Select("SELECT cid3 FROM tb_spu WHERE id = #{spuId}")
    Long queryCid3BySpuId(@Param("spuId") Long spuId);

    @Select("SELECT * FROM tb_seckill_goods WHERE sku_id = #{skuId}")
    SeckillGoods queryBySkuId(@Param("skuId") Long skuId);

}
