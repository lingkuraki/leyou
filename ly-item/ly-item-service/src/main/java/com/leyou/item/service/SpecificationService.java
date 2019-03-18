package com.leyou.item.service;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationService {

    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    public List<SpecGroup> querySpecGroups(Long cid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        List<SpecGroup> specGroupList = this.specGroupMapper.select(specGroup);
        return specGroupList;
    }

    public List<SpecParam> querySpecParams(Long gid) {
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        return this.specParamMapper.select(specParam);
    }

    public List<SpecParam> querySpecParams(Long gid, Long cid, Boolean searching, Boolean generic) {
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(searching);
        specParam.setGeneric(generic);
        return this.specParamMapper.select(specParam);
    }

    public void addSpecGroup(SpecGroup specGroup) {
        this.specGroupMapper.insert(specGroup);
    }

    public void updateSpecGroup(SpecGroup specGroup) {
        Example example = new Example(SpecGroup.class);
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", specGroup.getId());
        this.specGroupMapper.updateByExampleSelective(specGroup, example);
    }

    public void deleteSpecGroup(Long id) {
        this.specGroupMapper.deleteByPrimaryKey(id);
    }

    public void addSpecParam(SpecParam specParam) {
        if (StringUtils.isBlank(specParam.getUnit())) {
            specParam.setUnit("");
        }
        this.specParamMapper.insert(specParam);
    }

    public void updateSpecParam(SpecParam specParam) {
        this.specParamMapper.updateByPrimaryKeySelective(specParam);
    }

    public void deleteSpecParam(Long id) {
        this.specParamMapper.deleteByPrimaryKey(id);
    }

    public List<SpecGroup> querySpecsByCid(Long cid) {
        // 查询规格组
        List<SpecGroup> specGroupList = this.querySpecGroups(cid);
        // 查询当前分类下的所有规格参数
        List<SpecParam> specParamList = this.querySpecParams(null, cid, null, null);
        // 把specParam放入一个Map中，key是组id，即groupId，值是组内的所有参数
        Map<Long, List<SpecParam>> map = new HashMap<>();
        specParamList.forEach(specParam -> {
            // 判断当前参数所述的组是否存在于map集合中
            if (!map.containsKey(specParam.getGroupId())) {
                map.put(specParam.getGroupId(), new ArrayList<>());
            }
            // 将specParam存入到集合中
            map.get(specParam.getGroupId()).add(specParam);
        });
        // 循环存储specParam数据
        specGroupList.forEach(specGroup -> specGroup.setParams(map.get(specGroup.getId())));
        return specGroupList;
    }
}
