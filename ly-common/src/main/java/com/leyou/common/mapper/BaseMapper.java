package com.leyou.common.mapper;

import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

public interface BaseMapper<T, PK> extends Mapper<T>, IdListMapper<T, PK>, InsertListMapper<T> {
}
