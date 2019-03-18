package com.leyou.item.pojo;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name = "tb_category")
@Data
public class Category implements Serializable {

    @Id
    private Long id;
    private String name;
    private Long parentId;
    private Boolean isParent;
    private Integer sort;

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", parentId=" + parentId +
                ", isParent=" + isParent +
                ", sort=" + sort +
                '}';
    }
}
