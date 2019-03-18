package com.leyou.home.mapper;

import com.leyou.home.pojo.OrderStatus;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

import java.util.Date;

public interface OrderStatusMapper extends Mapper<OrderStatus> {

    @Select("SELECT status FROM tb_order_status WHERE order_id = #{orderId}")
    Integer queryStatusByOrderId(@Param("orderId") Long orderId);

    @Update("UPDATE tb_order_status SET status = 4,end_time = #{endTime} WHERE order_id = #{orderId}")
    Integer updateOrderToReceive(@Param("orderId") Long orderId, @Param("endTime") Date date);

    @Update("UPDATE tb_order_status SET status = 5 ,comment_time = #{commentTime} WHERE order_id = #{orderId}")
    Integer updateStatusByOrderId(@Param("commentTime") Date commentTime, @Param("orderId") Long orderId);
}
