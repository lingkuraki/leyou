package com.leyou.item.controller;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;

    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecGroups(@PathVariable("cid") Long cid) {
        List<SpecGroup> specGroupList = this.specificationService.querySpecGroups(cid);
        if (specGroupList == null || specGroupList.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(specGroupList);
    }

    @PostMapping("group")
    public ResponseEntity<Void> addSpecGroup(@RequestBody SpecGroup specGroup) {
        this.specificationService.addSpecGroup(specGroup);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("group")
    public ResponseEntity<Void> updateSpecGroup(@RequestBody SpecGroup specGroup) {
        this.specificationService.updateSpecGroup(specGroup);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("group/{id}")
    public ResponseEntity<Void> deleteSpecGroup(@PathVariable("id") Long id) {
        this.specificationService.deleteSpecGroup(id);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/params/{gid}")
    public ResponseEntity<List<SpecParam>> querySpecParam(@PathVariable(value = "gid", required = false) Long gid) {
        List<SpecParam> specParamList = this.specificationService.querySpecParams(gid);
        if (specParamList == null || specParamList.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(specParamList);
    }

    @GetMapping("/params")
    public ResponseEntity<List<SpecParam>> querySpecParams(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "Searching", required = false) Boolean searching,
            @RequestParam(value = "generic", required = false) Boolean generic) {
        List<SpecParam> list = this.specificationService.querySpecParams(gid, cid, searching, generic);
        if (list == null || list.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(list);
    }

    @PostMapping("param")
    public ResponseEntity<Void> addSpecParam(@RequestBody SpecParam specParam) {
        this.specificationService.addSpecParam(specParam);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("param")
    public ResponseEntity<Void> updateSpecParam(@RequestBody SpecParam specParam) {
        this.specificationService.updateSpecParam(specParam);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("param/{id}")
    public ResponseEntity<Void> deleteSpecParam(@PathVariable("id") Long id) {
        this.specificationService.deleteSpecParam(id);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecsByCid(@PathVariable("cid") Long cid) {
        List<SpecGroup> specList = this.specificationService.querySpecsByCid(cid);
        if (CollectionUtils.isEmpty(specList)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(specList);
    }
}
