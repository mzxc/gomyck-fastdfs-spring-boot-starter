package com.gomyck.fastdfs.starter;

import com.github.tobato.fastdfs.FdfsClientConfig;
import com.gomyck.cache.redis.starter.core.redis.RedisCache;
import com.gomyck.fastdfs.starter.database.SimpleUploadService;
import com.gomyck.fastdfs.starter.database.UploadService;
import com.gomyck.fastdfs.starter.lock.FileLock;
import com.gomyck.fastdfs.starter.lock.SimpleMapFileLock;
import com.gomyck.fastdfs.starter.lock.SimpleRedisFileLock;
import com.gomyck.fastdfs.starter.profile.FileServerProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


@Configuration
@Import(FdfsClientConfig.class)
@ComponentScan("com.gomyck.fastdfs.starter")
@EnableConfigurationProperties({FileServerProfile.class})
public class GomyckFastDFSConfiguration {

    Logger log = LoggerFactory.getLogger(GomyckFastDFSConfiguration.class);

    @Bean
    @ConditionalOnBean(value = RedisCache.class)
    public FileLock initFileLockRedis(){
        log.info("Initializing redis file lock....");
        return new SimpleRedisFileLock();
    }

    @Bean
    @ConditionalOnProperty("gomyck.config.redis")
    @ConditionalOnBean(value = RedisCache.class)
    public UploadService initUploadService(){
        log.info("Initializing redis file upload Service....");
        return new SimpleUploadService();
    }

    @Bean
    @ConditionalOnMissingBean(FileLock.class)
    public FileLock initFileLockDefault(){
        log.info("Initializing memory file lock, if you want open redis file lock, please config gomyck.config.redis: true in yml and conif the jedisPool");
        return new SimpleMapFileLock();
    }
}
