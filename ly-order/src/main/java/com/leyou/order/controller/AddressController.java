package com.leyou.order.controller;

import com.leyou.order.pojo.Address;
import com.leyou.order.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @PostMapping
    public ResponseEntity<Void> addAddress(@RequestBody Address address) {
        System.out.println("address = " + address);
        this.addressService.addAddress(address);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping
    public ResponseEntity<Void> updateAddress(@RequestBody Address address) {
        System.out.println("updateAddress = " + address);
        this.addressService.updateAddress(address);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<Address>> queryAllAddress() {
        List<Address> addresses = this.addressService.queryAllAddress();
        if (addresses == null || addresses.size() == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("{id}")
    public ResponseEntity<Address> queryAddress(@PathVariable("id") Long id) {
        Address address = this.addressService.queryAddress(id);
        if (address == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(address);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable("id") Long id) {
        this.addressService.deleteAddress(id);
        return ResponseEntity.ok().build();
    }
}
