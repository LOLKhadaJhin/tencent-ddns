package com.nhwb.tencentddns.controller;

import com.nhwb.tencentddns.api.TencentCloudApi;
import com.tencentcloudapi.dnspod.v20210323.models.RecordListItem;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class DdnsController {
    @Value("${tencent.domain}")
    private String domain;
    @Value("${tencent.subDomain}")
    private String subDomain;
    @Resource
    private TencentCloudApi.DomainApi domainApi;
    @Resource
    private Map<String, String> ipMap;
    @Resource
    private Map<String, RecordListItem> recordMap;

    @GetMapping("/domain")
    public String domain() {
        if (domainApi == null) {
            return "(未开启腾讯云)";
        }
        return ("@".equals(subDomain) ? "" : (subDomain + ".")) + domain;
    }

    @GetMapping("/domain/list")
    public String domainList() {
        if (domainApi == null) {
            return "(未开启腾讯云)";
        }
        List<String> domainNameList = domainApi.getDomainNameList();
        if (domainNameList == null) {
            return "(获取域名列表失败，访问频繁或密钥错误)";
        } else if (domainNameList.isEmpty()) {
            return "(您密钥填写正确，但该账号下没有该域名)";
        }
        return domainNameList.toString();
    }

    @GetMapping("/ip/list")
    public String ipList() {
        if (ipMap.isEmpty()) {
            return "(未获得IP)";
        }
        List<String> list = new ArrayList<>();
        ipMap.forEach((k, v) -> list.add(k + ":" + (v == null ? "未获得IP" : v)));
        return list.toString();
    }

    @GetMapping("/record/list")
    public String recordList() {
        if (domainApi == null) {
            return "(未开启腾讯云)";
        }
        if (recordMap.isEmpty()) {
            return "(未解析)";
        }
        List<String> list = new ArrayList<>();
        recordMap.forEach((k, v) -> {
            if (v != null) {
                list.add(k + ":" + v.getValue() + " 已" + ("ENABLE".equals(v.getStatus()) ? "启用" : "禁用"));
            }
        });
        return list.isEmpty() ? "(未解析)" : list.toString();
    }

}
