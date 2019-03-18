package com.leyou.order.service;

import com.leyou.order.mapper.AreaMapper;
import com.leyou.order.mapper.CityMapper;
import com.leyou.order.mapper.ProvinceMapper;
import com.leyou.order.pojo.Area;
import com.leyou.order.pojo.City;
import com.leyou.order.pojo.Province;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

import java.util.List;

@Service
public class ProvinceService {

    @Autowired
    private ProvinceMapper provinceMapper;

    @Autowired
    private CityMapper cityMapper;

    @Autowired
    private AreaMapper areaMapper;

    public List<Province> queryAllProvince() {
        return this.provinceMapper.selectAll();
    }

    public List<City> queryAllCityFromProvinceId(String provinceId) {
        Example example = new Example(City.class);
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("provinceId", provinceId);
        return this.cityMapper.selectByExample(example);
    }

    public List<Area> queryAllAreaFromCityId(String cityId) {
        Example example = new Example(Area.class);
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("cityId", cityId);
        return this.areaMapper.selectByExample(example);
    }
}
