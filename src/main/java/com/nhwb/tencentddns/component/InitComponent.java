package com.nhwb.tencentddns.component;

import com.nhwb.tencentddns.api.EmailApi;
import com.nhwb.tencentddns.api.TencentCloudApi;
import com.nhwb.tencentddns.utils.IPUtils;
import com.tencentcloudapi.dnspod.v20210323.models.RecordListItem;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InitComponent {
    @Value("${enable.ipv4:false}")
    private Boolean enableIpv4;
    @Value("${enable.ipv6:false}")
    private Boolean enableIpv6;
    @Value("${enable.tencent:false}")
    private Boolean enableTencent;
    @Value("${enable.mail:false}")
    private Boolean enableEmail;
    @Value("${tencent.domain}")
    private String domainName;
    @Value("${spring.mail.username}")
    private String emailAddress;
    @Value("${tencent.subDomain}")
    private String subDomain;

    @Resource
    private TencentCloudApi.DomainApi domainApi;
    @Resource
    private TencentCloudApi.DnsPodApi dnsPodApi;
    @Resource
    private EmailApi emailApi;
    @Resource
    private Map<String, String> ipMap;
    @Resource
    private Map<String, RecordListItem> recordMap;


    @PostConstruct
    public void init() {
        log.info("正在初始化中..............");
        log.info("B站使用教程:https://space.bilibili.com/25678649");
        // 初始化腾讯云
        if (enableTencent) {
            if (haveDomain()) {
                Map<String, RecordListItem> map = dnsPodApi.getDescribeRecordMap(domainName, subDomain);
                if (map != null) {
                    recordMap.putAll(map);
                }
            } else {
                System.exit(0);
            }
        }

        // 初始化IP
        if (enableIpv4) {
            initIPMap("ipv4");
        }
        if (enableIpv6) {
            initIPMap("ipv6");
        }

        if (enableTencent) {
            Map<String, RecordListItem> map = dnsPodApi.getDescribeRecordMap(domainName, subDomain);
            List<String> failList = new ArrayList<>();
            List<String> failTencentList = new ArrayList<>();
            if (map == null) {
                ipMap.forEach((k, v) -> {
                    if (v == null) {
                        failList.add(v);
                    } else {
                        failTencentList.add(k);
                    }
                });
            } else {
                recordMap.putAll(map);
                ipMap.forEach((k, v) -> {
                    if (v == null) {
                        failList.add(k);
                    } else {
                        RecordListItem item = recordMap.get(k);
                        if (item == null) {
                            failTencentList.add(k);
                        } else if (!item.getValue().equals(v)) {
                            failTencentList.add(k);
                        }
                    }
                });
            }

            if (!failTencentList.isEmpty()) {
                log.error("以下IP腾讯云解析失败:{}", failTencentList);
                if (enableEmail) {
                    new Thread(() -> emailApi.sendFailMessage(emailAddress, "以下IP腾讯云解析失败:" + failTencentList)).start();
                }
            }

            if (!failList.isEmpty()) {
                log.error("以下IP解析失败:{}", failList);
                if (enableEmail) {
                    new Thread(() -> emailApi.sendFailMessage(emailAddress, "以下IP解析失败:" + failList)).start();
                }
            }
        }
    }


    private boolean haveDomain() {
        List<String> domainNameList = domainApi.getDomainNameList();
        if (domainNameList == null) {
            log.error("请检查配置或将配置enable.tencent=false，正在关闭程序");
            return false;
        } else if (domainNameList.isEmpty()) {
            log.error("请购买域名或将配置enable.tencent=false，正在关闭程序");
            return false;
        }
        log.info("获取域名列表成功,共{}个,分别是{}", domainNameList.size(), domainNameList);
        if (!domainNameList.contains(domainName)) {
            log.error("域名列表中不包含{}，请检查配置", domainName);
            return false;
        }
        return true;
    }


    private void initIPMap(String type) {
        String ip = IPUtils.getIP(type);
        ipMap.put(type, ip);
        if (ip == null) {
            log.error("获取{}失败", type);
            return;
        }
        log.info("当前{}:{}", type, ip);

        if (enableEmail) {
            new Thread(() -> emailApi.sendIpMessage(emailAddress, type + ":" + ip)).start();
        }

        if (enableTencent) {
            RecordListItem item = recordMap.get(type);
            if (item == null) {
                dnsPodApi.createRecord(type, domainName, ip, subDomain);
            } else if (!ip.equals(item.getValue())) {
                dnsPodApi.modifyRecord(item, domainName, ip);
            }
        }
    }

}