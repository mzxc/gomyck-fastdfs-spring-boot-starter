package com.gomyck.fastdfs.starter.controller;

import com.gomyck.util.R;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author gomyck
 * @version 1.0.0
 * @contact qq: 474798383
 * @blog https://blog.gomyck.com
 * @since 2020-05-07
 */
@RestController
@RequestMapping("download/simpleDownload")
public class DoorChainHandler {

    /**
     *
     *
     *
     * @param md5s
     * @return
     */
    public R getDoorChain(String[] md5s){


        return R.ok();
    }


}
