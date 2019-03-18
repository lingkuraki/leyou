package com.leyou.order.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.order.pojo.Stock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface StockMapper extends BaseMapper<Stock, Long> {
    @Select("SELECT stock FROM tb_stock WHERE sku_id = #{skuId}")
    Integer selectStockBySkuId(@Param("skuId") Long skuId);

    @Update("UPDATE tb_stock SET stock = stock - #{num} WHERE sku_id = #{skuId} AND stock >= #{num}")
    Integer decreaseStock(@Param("num") Integer num, @Param("skuId") Long skuId);
}
