package com.leyou.home.mapper;

import com.leyou.home.pojo.Order;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface OrderMapper extends Mapper<Order> {
    @Select("SELECT * FROM tb_order WHERE user_id = #{userId}")
    List<Order> queryOrderByUserId(@Param("userId") Long userId);

    @Update("UPDATE tb_order SET buyer_message = #{message} WHERE order_id = #{orderId}")
    Integer updateMessageByOrderId(@Param("message") String message, @Param("orderId") Long orderId);
}