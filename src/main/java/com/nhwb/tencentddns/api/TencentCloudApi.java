package com.nhwb.tencentddns.api;

import com.alibaba.fastjson2.JSONObject;
import com.tencentcloudapi.common.AbstractClient;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.dnspod.v20210323.models.*;
import com.tencentcloudapi.domain.v20180808.models.DescribeDomainNameListRequest;
import com.tencentcloudapi.domain.v20180808.models.DescribeDomainNameListResponse;
import com.tencentcloudapi.domain.v20180808.models.DomainList;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class TencentCloudApi {
    //域名 API
    public static class DomainApi extends AbstractClient {
        private static final String service = "domain";
        private static final String endpoint = "domain.tencentcloudapi.com";
        private static final String version = "2018-08-08";

        public DomainApi(String secretId, String secretKey, String region) {
            this(new Credential(secretId, secretKey), region);
        }

        public DomainApi(Credential credential, String region) {
            this(credential, region, new ClientProfile());
        }

        public DomainApi(Credential credential, String region, ClientProfile profile) {
            super(DomainApi.endpoint, DomainApi.version, credential, region, profile);
        }

        /**
         * 本接口 (  DescribeDomainNameList ) 我的域名列表。
         *
         * @param req DescribeDomainNameListRequest
         * @return DescribeDomainNameListResponse
         * @throws TencentCloudSDKException
         */
        public DescribeDomainNameListResponse DescribeDomainNameList(DescribeDomainNameListRequest req) throws TencentCloudSDKException {
            req.setSkipSign(false);
            return this.internalRequest(req, "DescribeDomainNameList", DescribeDomainNameListResponse.class);
        }

        public List<String> getDomainNameList() {
            try {
                DescribeDomainNameListRequest req1 = new DescribeDomainNameListRequest();
                DescribeDomainNameListResponse response = DescribeDomainNameList(req1);
                DomainList[] domainSet = response.getDomainSet();
                if (domainSet == null || domainSet.length == 0) {
                    log.error("您密钥填写正确，但该账号下没有该域名");
                    return new ArrayList<>();
                }
                List<String> list = new ArrayList<>();
                for (DomainList v : domainSet) {
                    list.add(v.getDomainName());
                }
                return list;
            } catch (Exception e) {
                log.error("获取域名列表失败", e);
            }
            return null;
        }
    }

    //DNS API
    public static class DnsPodApi extends AbstractClient {
        private static final String service = "dnspod";
        private static final String endpoint = "dnspod.tencentcloudapi.com";
        private static final String version = "2021-03-23";

        public DnsPodApi(String secretId, String secretKey, String region) {
            this(new Credential(secretId, secretKey), region);
        }

        public DnsPodApi(Credential credential, String region) {
            this(credential, region, new ClientProfile());
        }

        public DnsPodApi(Credential credential, String region, ClientProfile profile) {
            super(DnsPodApi.endpoint, DnsPodApi.version, credential, region, profile);
        }

        /**
         * 获取某个域名下的解析记录列表
         * 备注：
         * 1. 新添加的解析记录存在短暂的索引延迟，如果查询不到新增记录，请在 30 秒后重试
         * 2.  API获取的记录总条数会比控制台多2条，原因是： 为了防止用户误操作导致解析服务不可用，对2021-10-29 14:24:26之后添加的域名，在控制台都不显示这2条NS记录。
         *
         * @param req DescribeRecordListRequest
         * @return DescribeRecordListResponse
         * @throws TencentCloudSDKException
         */
        public DescribeRecordListResponse DescribeRecordList(DescribeRecordListRequest req) throws TencentCloudSDKException {
            req.setSkipSign(false);
            return this.internalRequest(req, "DescribeRecordList", DescribeRecordListResponse.class);
        }

        /**
         * 添加记录
         * 备注：新添加的解析记录存在短暂的索引延迟，如果查询不到新增记录，请在 30 秒后重试
         *
         * @param req CreateRecordRequest
         * @return CreateRecordResponse
         * @throws TencentCloudSDKException
         */
        public CreateRecordResponse CreateRecord(CreateRecordRequest req) throws TencentCloudSDKException {
            req.setSkipSign(false);
            return this.internalRequest(req, "CreateRecord", CreateRecordResponse.class);
        }

        /**
         * 修改记录
         *
         * @param req ModifyRecordRequest
         * @return ModifyRecordResponse
         * @throws TencentCloudSDKException
         */
        public ModifyRecordResponse ModifyRecord(ModifyRecordRequest req) throws TencentCloudSDKException {
            req.setSkipSign(false);
            return this.internalRequest(req, "ModifyRecord", ModifyRecordResponse.class);
        }

        /**
         * 删除记录
         *
         * @param req DeleteRecordRequest
         * @return DeleteRecordResponse
         * @throws TencentCloudSDKException
         */
        public DeleteRecordResponse DeleteRecord(DeleteRecordRequest req) throws TencentCloudSDKException {
            req.setSkipSign(false);
            return this.internalRequest(req, "DeleteRecord", DeleteRecordResponse.class);
        }

        public Map<String, RecordListItem> getDescribeRecordMap(String domainName, String subDomain) {
            try {
                DescribeRecordListRequest recordListRequest = new DescribeRecordListRequest();
                recordListRequest.setDomain(domainName);
                DescribeRecordListResponse recordList = DescribeRecordList(recordListRequest);
                RecordListItem[] dnspodReqList = recordList.getRecordList();
                if (dnspodReqList == null) {
                    return null;
                }
                Map<String, RecordListItem> map = new HashMap<>();
                for (RecordListItem v : dnspodReqList) {
                    if (v.getName().equals(subDomain)) {
                        if ("AAAA".equals(v.getType())) {
                            map.put("ipv6", v);
                        } else if ("A".equals(v.getType())) {
                            map.put("ipv4", v);
                        }
                    }
                }
                return map;
            } catch (Exception e) {
                log.error("获取域名解析记录失败", e);
                return null;
            }
        }

        public void createRecord(String type, String domainName, String ip, String subDomain) {
            try {
                CreateRecordRequest createRecordRequest = new CreateRecordRequest();
                createRecordRequest.setDomain(domainName);
                createRecordRequest.setRecordType("ipv4".equals(type) ? "A" : "AAAA");
                createRecordRequest.setRecordLine("默认");
                createRecordRequest.setSubDomain(subDomain);
                createRecordRequest.setStatus("ENABLE");
                createRecordRequest.setValue(ip);
                CreateRecord(createRecordRequest);
                log.info("添加域名解析记录成功");
            } catch (Exception e) {
                log.error("添加域名解析记录失败", e);
            }
        }

        public void modifyRecord(RecordListItem item, String domainName, String ip) {
            try {
                ModifyRecordRequest modifyRecordRequest = new ModifyRecordRequest();
                modifyRecordRequest.setDomain(domainName);
                modifyRecordRequest.setRecordType(item.getType());
                modifyRecordRequest.setRecordId(item.getRecordId());
                modifyRecordRequest.setRecordLine("默认");
                modifyRecordRequest.setStatus("ENABLE");
                modifyRecordRequest.setValue(ip);
                ModifyRecord(modifyRecordRequest);
            } catch (Exception e) {
                log.error("修改域名解析记录失败", e);
            }
        }
    }
}

