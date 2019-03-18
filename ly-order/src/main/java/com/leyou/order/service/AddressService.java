package com.leyou.order.service;

import com.leyou.auth.pojo.UserInfo;

import com.leyou.order.interceptor.LoginInterceptor;
import com.leyou.order.mapper.AddressMapper;
import com.leyou.order.pojo.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressMapper addressMapper;

    // 新增地址
    public void addAddress(Address address) {
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        address.setUserId(userInfo.getId());
        this.addressMapper.insert(address);
    }

    public List<Address> queryAllAddress() {
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        if (userInfo != null) {
            Example example = new Example(Address.class);
            Criteria criteria = example.createCriteria();
            criteria.andEqualTo("userId", userInfo.getId());
            return this.addressMapper.selectByExample(example);
            //return this.addressMapper.queryByUserId(userInfo.getId());
        }
        return null;
    }

    public Address queryAddress(Long id) {
        return this.addressMapper.selectByPrimaryKey(id);
    }

    public void updateAddress(Address address) {
        this.addressMapper.updateByPrimaryKeySelective(address);
    }

    public void deleteAddress(Long id) {
        this.addressMapper.deleteByPrimaryKey(id);
    }
}
