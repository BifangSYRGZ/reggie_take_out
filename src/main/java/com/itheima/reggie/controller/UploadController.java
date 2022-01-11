package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.stream.FileImageInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * @neirong: 上传文件
 * @banben:
 */
@RestController
@RequestMapping("/common")
public class UploadController {

    @Value("${imgDir.name}")
    private String url;

    @Autowired
    private HttpServletResponse response;
    @PostMapping("/upload")
    public R uploadfile(MultipartFile file) throws IOException {
        System.out.println(file);

        System.out.println(file.getBytes());

        //防止图面名称一样导致图片被覆盖
        String newid = UUID.randomUUID().toString();
        String oldid = file.getOriginalFilename();
        //获取图片的后缀名
        String hzid = oldid.substring(oldid.lastIndexOf("."));
        //组合名字
        newid=newid+hzid;
        file.transferTo(new File(url+newid));
        return R.success(newid);

    }
    @GetMapping("/download")
    public void download(String name) throws IOException {
        //找到图片并且读取图片
        FileInputStream fis = new FileInputStream(url + name);
        ServletOutputStream os = response.getOutputStream();
        byte[] bytes=  new byte[1024];
        while (true){
            int len = fis.read(bytes);
            if (len==-1){
                //读取完成
                break;
            }
            os.write(bytes,0,len);
        }
        //关闭流
        os.close();
        fis.close();

    }
}
