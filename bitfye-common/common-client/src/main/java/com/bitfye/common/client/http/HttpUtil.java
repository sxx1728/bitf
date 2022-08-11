package com.bitfye.common.client.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * @author ming.jia
 * @version 1.0
 * @description TODO
 * @date 2017/6/2 16:06
 **/
@Slf4j
public class HttpUtil {

    private final static String DEFAULT_ENCODING = "UTF-8";

    /**
     * 向指定URL发送GET方法的请求
     * @param url 发送请求的URL
     * @param paramMap 请求参数，请求参数应该是name1=value1&name2=value2的形式。
     * @return URL所代表远程资源的响应
     */
    public static String sendGet(String url, Map<String, String> paramMap) {
        //将map转化为查询字符串
        String param = createQueryString(paramMap);
        StringBuffer result = new StringBuffer();
        BufferedReader in = null;
        try {
            String urlName = "";
            if(StringUtils.isNotEmpty(param)) {
                urlName = url + "?" + param;
            } else {
                urlName = url;
            }
            URL realUrl = new URL(urlName);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("contentType","UTF-8");
            conn.setRequestProperty("Accept-Charset","UTF-8");
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            // 建立实际的连接
            conn.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = conn.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), DEFAULT_ENCODING));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result.toString();
    }
    /**
     * 向指定URL发送POST方法的请求
     * @param url 发送请求的URL
     * @param paramMap 请求参数，请求参数应该是name1=value1&name2=value2的形式。
     * @return URL所代表远程资源的响应
     */
    public static String sendPost(String url, Map<String, String> paramMap) {
        //将map转化为查询字符串
        String param = createQueryString(paramMap);
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuffer result = new StringBuffer();
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), DEFAULT_ENCODING));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            log.info("发送POST请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        log.info(result.toString());
        return result.toString();
    }

    /**
     * 功能：用一个Map生成一个QueryString，参数的顺序不可预知。
     *
     * @param queryMap
     * @return
     */
    public static String createQueryString(Map<String, String> queryMap) {
        if (queryMap == null) {
            return null;
        }
        try {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : queryMap.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                String key = entry.getKey().trim();
                String value = URLEncoder.encode(entry.getValue().trim(),DEFAULT_ENCODING);

                sb.append(String.format("%s=%s&", key, value));
            }
            return sb.substring(0, sb.length() - 1);
        } catch (StringIndexOutOfBoundsException e) {
            log.error("createQueryString error",e);
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }


}
