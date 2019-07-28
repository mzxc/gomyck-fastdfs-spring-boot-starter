package com.gomyck.fastdfs.starter.database;

/**
 * @author 郝洋 QQ:474798383
 * @version [版本号/1.0]
 * @see [相关类/方法]
 * @since [2019-07-25]
 */
public class ServiceCheck {


    public static void uploadServiceCheck(UploadService us){
        if(us == null) {
            throw new RuntimeException("please add a service impl com.gomyck.fastdfs.starter.database.UploadService");
        }
    }

}
