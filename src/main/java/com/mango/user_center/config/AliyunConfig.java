package com.mango.user_center.config;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
/**
 * @desc
 *
 * @author gaojun
 * @email 15037584397@163.com
 */
@Configuration
@PropertySource(value = {"classpath:AliyunOss.properties"})
@ConfigurationProperties(prefix = "aliyun")
@Data
public class AliyunConfig {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String urlPrefix;
    @Bean
    public OSS ossClient() {
        return new OSSClient(endpoint, accessKeyId, accessKeySecret);
    }
}
