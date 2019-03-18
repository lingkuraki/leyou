package com.leyou.order.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.order.pojo.Address;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AddressMapper extends BaseMapper<Address, Long> {
    @Select("SELECT * FROM tb_address WHERE user_id = #{userId}")
    List<Address> queryByUserId(@Param("userId") Long userId);
}
