package com.gomyck.fastdfs.starter.database;

/**
 * 服务类检查
 *
 * @author gomyck
 * --------------------------------
 * | qq: 474798383                 |
 * | email: hao474798383@163.com   |
 * | blog: https://blog.gomyck.com |
 * --------------------------------
 * @version [1.0.0]
 * @since 2021/7/5
 */
public class ServiceCheck {


    public static void uploadServiceCheck(UploadService us){
        if(us == null) {
            throw new RuntimeException("please add a service impl com.gomyck.fastdfs.starter.database.UploadService");
        }
    }

}
