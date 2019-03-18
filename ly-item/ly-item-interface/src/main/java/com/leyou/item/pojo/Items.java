package com.leyou.item.pojo;

import lombok.Data;

import java.util.List;

@Data
public class Items {

    private Category category;

    private List<Category> categories;
}