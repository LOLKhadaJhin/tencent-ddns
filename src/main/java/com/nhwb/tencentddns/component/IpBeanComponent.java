package com.nhwb.tencentddns.component;

import com.nhwb.tencentddns.api.EmailApi;
import com.nhwb.tencentddns.api.TencentCloudApi;
import com.tencentcloudapi.dnspod.v20210323.models.RecordListItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class IpBeanComponent {
    @Value("${enable.mail:false}")
    private Boolean enableEmail;
    @Value("${enable.tencent:false}")
    private Boolean enableTencent;
    @Value("${tencent.region}")
    private String region;
    @Value("${tencent.domain}")
    private String domain;
    @Value("${tencent.subDomain}")
    private String subDomain;
    @Value("${tencent.secretId}")
    private String secretId;
    @Value("${tencent.secretKey}")
    private String secretKey;


    private final ApplicationContext applicationContext;

    public IpBeanComponent(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public EmailApi getEmailApi() {
        if (enableEmail) {
            JavaMailSender javaMailSender = applicationContext.getBean(JavaMailSender.class);
            return new EmailApi(javaMailSender);
        }
        return null;
    }

    @Bean
    public TencentCloudApi.DomainApi domainApi() {
        if (enableTencent) {
            return new TencentCloudApi.DomainApi(secretId, secretKey, region);
        }
        return null;
    }

    @Bean
    public TencentCloudApi.DnsPodApi dnsPodApi() {
        if (enableTencent) {
            return new TencentCloudApi.DnsPodApi(secretId, secretKey, region);
        }
        return null;
    }

    @Bean
    public Map<String, String> ipMap() {
        return new HashMap<>();
    }

    @Bean
    public Map<String, RecordListItem> recordMap() {
        return new HashMap<>();
    }
}
