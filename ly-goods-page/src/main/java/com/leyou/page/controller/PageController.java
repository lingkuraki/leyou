package com.leyou.page.controller;

import com.leyou.page.service.FileService;
import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("item")
public class PageController {

    @Autowired
    private PageService pageService;

    @Autowired
    private FileService fileService;

    /**
     * 跳转到商品详情页
     *
     * @param model 模型
     * @param id    商品id
     * @return 返回跳转页面名
     */
    @GetMapping("{id}.html")
    public String toItemPage(Model model, @PathVariable("id") Long id) {
        // 加载所需的数据
        Map<String, Object> modelMap = this.pageService.loadData(id);
        // 加入模型
        model.addAllAttributes(modelMap);
        // 页面静态化
        this.fileService.asynCreatHtml(id);
        return "item";
    }
}
