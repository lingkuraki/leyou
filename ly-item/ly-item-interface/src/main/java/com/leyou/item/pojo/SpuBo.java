package com.leyou.item.pojo;

import javax.persistence.Transient;
import java.util.List;

public class SpuBo extends Spu {

    // 商品分类名称
    @Transient
    private String cname;

    // 品牌名称
    @Transient
    private String bname;

    // 商品详情
    @Transient
    private SpuDetail spuDetail;

    // sku列表
    @Transient
    private List<Sku> skus;

    public SpuDetail getSpuDetail() {
        return spuDetail;
    }

    public void setSpuDetail(SpuDetail spuDetail) {
        this.spuDetail = spuDetail;
    }

    public List<Sku> getSkus() {
        return skus;
    }

    public void setSkus(List<Sku> skus) {
        this.skus = skus;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getBname() {
        return bname;
    }

    public void setBname(String bname) {
        this.bname = bname;
    }

    @Override
    public String toString() {
        return "SpuBo{" +
                "cname='" + cname + '\'' +
                ", bname='" + bname + '\'' +
                ", spuDetail=" + spuDetail +
                ", skus=" + skus +
                '}';
    }
}
