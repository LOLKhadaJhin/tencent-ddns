package com.nhwb.tencentddns.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Slf4j
public class IPUtils {

    public static String getIP(String type) {
        try {
            URL url = new URL("ipv6".equals(type) ? "http://ipv6.icanhazip.com/" : "http://ipv4.icanhazip.com/");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            conn.disconnect();
            return content.toString().trim();
        } catch (Exception e) {
            log.error("获取IP失败:{}", e.getMessage());
            return null;
        }
    }


    public static List<String> getIpv6() {
        try {
            List<String> list = new ArrayList<>();
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                // 忽略非活动或虚拟的网络接口
                if (!networkInterface.isUp() || networkInterface.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    // 只处理IPv6地址，并且忽略环回地址
                    if (inetAddress instanceof java.net.Inet6Address && !inetAddress.isLoopbackAddress()) {
                        list.add(inetAddress.getHostAddress());
                    }
                }
            }
            return list;
        } catch (SocketException e) {
            log.error("获取IP失败:{}", e.getMessage());
            return null;
        }
    }
}
