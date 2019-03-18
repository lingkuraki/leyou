package com.leyou.order.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.order.pojo.OrderStatus;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface OrderStatusMapper extends BaseMapper<OrderStatus, Long> {

    @Update("UPDATE tb_order_status SET status = 3 WHERE order_id = #{orderId}")
    Integer updateStatusByOrderId(@Param("orderId") Long orderId);
}
