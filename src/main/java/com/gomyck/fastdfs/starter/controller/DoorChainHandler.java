package com.gomyck.fastdfs.starter.controller;

import com.gomyck.util.servlet.R;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 单文件下载控制器
 *
 * @author gomyck
 * --------------------------------
 * | qq: 474798383                 |
 * | email: hao474798383@163.com   |
 * --------------------------------
 * @version [1.0.0]
 * @since 2021/5/31
 */
@RestController
@RequestMapping("download/simpleDownload")
public class DoorChainHandler {

    public R getDoorChain(String[] md5s){


        return R.ok();
    }


}
