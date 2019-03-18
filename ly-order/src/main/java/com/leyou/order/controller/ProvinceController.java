package com.leyou.order.controller;

import com.leyou.order.pojo.Area;
import com.leyou.order.pojo.City;
import com.leyou.order.pojo.Province;
import com.leyou.order.service.ProvinceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("province")
public class ProvinceController {

    @Autowired
    private ProvinceService provinceService;

    @GetMapping
    public ResponseEntity<List<Province>> queryAllProvince() {
        List<Province> provinces = this.provinceService.queryAllProvince();
        if (provinces == null || provinces.size() == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(provinces);
    }

    @GetMapping("{provinceId}")
    public ResponseEntity<List<City>> queryAllCityFromProvinceId(@PathVariable("provinceId") String provinceId) {
        List<City> cities = this.provinceService.queryAllCityFromProvinceId(provinceId);
        if (cities == null || cities.size() == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(cities);
    }

    @GetMapping("city/{cityId}")
    public ResponseEntity<List<Area>> queryAllAreaFromCityId(@PathVariable("cityId") String cityId) {
        List<Area> areas = this.provinceService.queryAllAreaFromCityId(cityId);
        if (areas == null || areas.size() == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(areas);
    }
}
