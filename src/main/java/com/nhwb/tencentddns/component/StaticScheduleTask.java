package com.nhwb.tencentddns.component;

import com.nhwb.tencentddns.api.EmailApi;
import com.nhwb.tencentddns.api.TencentCloudApi;
import com.nhwb.tencentddns.utils.IPUtils;
import com.tencentcloudapi.dnspod.v20210323.models.RecordListItem;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
public class StaticScheduleTask {
    @Value("${enable.ipv4:false}")
    private Boolean enableIpv4;
    @Value("${enable.ipv6:false}")
    private Boolean enableIpv6;
    @Value("${enable.tencent:false}")
    private Boolean enableTencent;
    @Value("${enable.mail:false}")
    private Boolean enableEmail;
    @Resource
    private EmailApi emailApi;
    @Value("${tencent.domain}")
    private String domainName;
    @Value("${tencent.subDomain}")
    private String subDomain;
    @Value("${spring.mail.username}")
    private String emailAddress;
    @Resource
    private TencentCloudApi.DnsPodApi dnsPodApi;
    @Resource
    private Map<String, String> ipMap;
    @Resource
    private Map<String, RecordListItem> recordMap;

    @Scheduled(fixedDelay = 1000L * 20, initialDelay = 1000L * 15)
    public void ddnsIPv4() {
        if (!enableIpv4) {
            return;
        }
        sleep();
        initIPMap("ipv4");
    }

    @Scheduled(fixedDelay = 1000L * 20, initialDelay = 1000L * 15)
    private void ddnsIPv6() {
        if (!enableIpv6) {
            return;
        }
        sleep();
        initIPMap("ipv6");
    }

    private void sleep() {
        try {
            TimeUnit.MILLISECONDS.sleep((long) (Math.random() * 20000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void initIPMap(String type) {
        String ip = IPUtils.getIP(type);
        if (ip == null) {
            log.error("获取{}失败", type);
            return;
        }

        String oldIP = ipMap.get(type);
        if (!ip.equals(oldIP)) {
            ipMap.put(type, ip);
            log.info("当前{}:{}", type, ip);
            if (enableEmail) {
                new Thread(() -> emailApi.sendIpMessage(emailAddress, type + ":" + ip)).start();
            }
        }

        if (enableTencent) {
            RecordListItem item = recordMap.get(type);
            if (item == null) {
                dnsPodApi.createRecord(type, domainName, ip, subDomain);
            } else if (!ip.equals(item.getValue())) {
                dnsPodApi.modifyRecord(item, domainName, ip);
            } else {
                return;
            }
            Map<String, RecordListItem> map = dnsPodApi.getDescribeRecordMap(domainName, subDomain);
            if (map != null) {
                recordMap.put(type, map.get(type));
                item = recordMap.get(type);
                if (item != null && item.getValue().equals(ip)) {
                    return;
                }
            }
            log.error("腾讯云{}解析失败:{}", type, ip);
            if (enableEmail && !ip.equals(oldIP)) {
                emailApi.sendFailMessage(emailAddress, type + ":" + ip);
            }
        }
    }
}
