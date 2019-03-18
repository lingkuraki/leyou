package com.leyou.order.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "tb_address")
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;// 用户id
    private String phone;// 移动电话
    private String state;// 省份
    private String city;// 城市
    private String name;// 收件人姓名
    private String district;// 区/县
    private String address;// 具体地址
    private String zipCode;// 邮政编码
    private Boolean isDefault;// 是否是默认地址
    private String email;// 邮箱地址
}
