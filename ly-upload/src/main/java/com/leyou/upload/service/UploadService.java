package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.upload.controller.UploadController;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;

@Service
public class UploadService {

    @Autowired
    private FastFileStorageClient storageClient;
    // 支持的文件类型
    private static final List<String> CONTENT_TYPES = Arrays.asList("image/jpeg", "image/gif");
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadService.class);
    public String upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        // 检验文件的类型
        String contentType = file.getContentType();
        if (!CONTENT_TYPES.contains(contentType)) {
            // 文件类型不合法，直接返回null
            LOGGER.info("文件类型不合法：{}", originalFilename);
            return null;
        }
        try {
            // 检验文件的内容
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage == null) {
                LOGGER.info("文件内容不合法:{}", originalFilename);
                return null;
            }
            // 保存到服务器
            String ext = StringUtils.substringAfterLast(originalFilename, ".");
            // 上传并保存图片，参数：1-上传的文件流，2-文件的大小，3-文件的后缀，4-可以不管他
            StorePath storePath = this.storageClient.uploadFile(file.getInputStream(), file.getSize(), ext, null);
            // 生成url地址，返回
            return "http://image.leyou.com/" + storePath.getFullPath();
        } catch (Exception e) {
            LOGGER.info("服务器内部错误：{}", originalFilename);
            e.printStackTrace();
        }
        return null;
    }

   /* private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    // 支持的文件类型
    private static final List<String> suffixes = Arrays.asList("image/png", "image/jpeg");

    public String upload(MultipartFile file) {
        try {
            // 1.图片信息校验
            // 1）检验文件类型
            String type = file.getContentType();
            if (!suffixes.contains(type)) {
                logger.info("上传失败，文件类型不匹配：{}", type);
                return null;
            }
            // 2) 校验图片内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                logger.info("上传失败，文件内容不符合要求");
                return null;
            }
            // 2.保存图片
            // 2.1 生成保存目录
            File dir = new File("D:/heima/upload");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // 2.2 保存图片
            file.transferTo(new File(dir, file.getOriginalFilename()));
            // 2.3 拼接图片地址
            String url = "http://image.leyou.com/upload/" + file.getOriginalFilename();
            return url;
        } catch (Exception e) {
            return null;
        }
    }*/
}