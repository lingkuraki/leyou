package com.leyou.page.service;

import com.leyou.page.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

@Service
public class FileService {

    @Autowired
    private PageService pageService;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${ly.thymeleaf.destPath}")
    private String destPath; // D:/nginx-1.12.2/html/item

    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

    /**
     * 创建html页面
     *
     * @param id 商品id
     * @throws Exception
     */
    public void createHtml(Long id) throws Exception {
        // 创建上下文
        Context context = new Context();
        // 把数据加入上下文
        context.setVariables(this.pageService.loadData(id));

        // 创建输出流，关联到一个临时文件
        File temp = new File(id + ".html");
        // 目标页面文件
        File dest = this.createPath(id);
        // 备份原页面文件
        File bak = new File(id + "_bak.html");
        try (PrintWriter writer = new PrintWriter(temp, "UTF-8")) {
            // 利用thymeleaf模板引擎生成，静态页面
            templateEngine.process("item", context, writer);
            if (dest.exists()) {
                // 如果目标文件已经存在，先备份
                dest.renameTo(bak);
            }
            // 将新页面覆盖旧页面
            FileCopyUtils.copy(temp, dest);
            // 成功后将备份页面删除
            bak.delete();
        } catch (IOException e) {
            // 异常失败后，将备份页面恢复
            bak.renameTo(dest);
            // 抛出异常，声明页面生成失败
            LOGGER.error("页面静态化出错：{}" + e, id);
            throw new Exception(e);
        } finally {
            // 删除临时页面
            if (temp.exists()) temp.delete();
        }
    }

    private File createPath(Long id) {
        if (id == null) return null;
        File dest = new File(this.destPath);
        if (!dest.exists()) {
            dest.mkdirs();
        }
        return new File(dest, id + ".html");
    }

    /**
     * 异步创建html页面
     *
     * @param id 商品iid
     */
    public void asynCreatHtml(Long id) {
        ThreadUtils.execute(() -> {
            try {
                createHtml(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    /**
     * 删除页面
     *
     * @param id 商品id
     */
    public void deleteHtml(Long id) {
        File file = new File(this.destPath, id + ".html");
        file.deleteOnExit();
    }
}
