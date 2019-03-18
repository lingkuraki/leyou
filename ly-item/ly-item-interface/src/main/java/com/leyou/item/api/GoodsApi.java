package com.leyou.item.api;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuBo;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 商品服务接口
 */
public interface GoodsApi {

    /**
     * 分页查询
     *
     * @param page     页数
     * @param rows     行数
     * @param saleable 是否上架
     * @param key      关键字
     * @return 符合查询条件的spu
     */
    @GetMapping("/spu/page")
    PageResult<SpuBo> querySpuBuPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "key", required = false) String key);

    /**
     * 根据spu上林id查询详情
     *
     * @param id 商品id
     * @return 返回商品详细信息
     */
    @GetMapping("/spu/detail/{id}")
    SpuDetail querySpuDetailById(@PathVariable("id") Long id);

    /**
     * 根据spu的id查询sku
     *
     * @param id spuId
     * @return 返回该spu下所有sku集合
     */
    @GetMapping("sku/list")
    List<Sku> querySkuBySpuId(@RequestParam("id") Long id);

    /**
     * 根据id查询spu
     *
     * @param id 商品id，spuId
     * @return 返回Spu商品
     */
    @GetMapping("spu/{id}")
    SpuBo querySpuById(@PathVariable("id") Long id);
}
