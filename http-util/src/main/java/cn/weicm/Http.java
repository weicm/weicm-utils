package cn.weicm;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>Author: weicm</p>
 * <p>Date: 2018/1/30 18:17</p>
 * <p>Desp: 基于Apache fluent-hc 实现的 Http 工具；线程安全</p>
 */
public class Http {
    //连接超时时间
    private static final Integer CONNECT_TIMEOUT = 10000;
    //读取超时时间
    private static final Integer READ_TIMEOUT = 10000;
    //代理主机名
    private static final String PROXY_HOST = null;
    //代理主机端口
    private static final Integer PROXY_PORT = null;
    //代理协议
    private static final String PROXY_SCHEMA = null;
    private static Http instance;
    private Http(){}
    public static Http getInstance() {
        if (null == instance) {
            synchronized (Http.class) {
                if (null == instance) {
                    instance = new Http();
                }
            }
        }
        return instance;
    }

    private static final Logger log = LoggerFactory.getLogger(Http.class);

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/2/27 14:26</p>
     * <p>Desp: GET请求，返回字符串，不带参数</p>
     * @param url 请求URL
     * @return 应答体
     */
    public String get(String url) {
        return get(url, null);
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/2/27 14:27</p>
     * <p>Desp: GET请求，返回字符串，带参数</p>
     * @param url 请求URL
     * @param params 请求参数
     * @return 应答体
     */
    public String get(String url, Map<String, String> params) {
        Content content = executeGet(url, params);
        return getString(content);
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/2/27 14:28</p>
     * <p>Desp: GET请求，返回字节数组，不带参数</p>
     * @param url 请求URL
     * @return 应答体
     */
    public byte[] getBytes(String url) {
        return getBytes(url, null);
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/2/27 14:29</p>
     * <p>Desp: GET请求，返回字节数组，带参数</p>
     * @param url 请求URL
     * @param params 请求参数
     * @return 应答体
     */
    public byte[] getBytes(String url, Map<String, String> params) {
        Content content = executeGet(url, params);
        return getBytes(content);
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/2/27 14:29</p>
     * <p>Desp: 自定义GET请求，格式和fluent-hc一致，返回Request对象</p>
     * @param url 请求URL
     * @param params 请求参数
     * @return 请求对象Request
     */
    public Request doGet(String url, Map<String, String> params) {
        url = buildGetUrl(url, params);
        Request request = Request.Get(url);
        return request;
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/2/27 14:31</p>
     * <p>Desp: 通用配置的GET请求，默认配置：链接超时时间、读取超时时间</p>
     * @param url 请求URL
     * @param params 请求参数
     * @return 应答上下文对象Content
     */
    private Content executeGet(String url, Map<String, String> params) {
        Request request = null;
        try {
            url = buildGetUrl(url, params);
            request = Request.Get(url);
            request.connectTimeout(CONNECT_TIMEOUT).socketTimeout(READ_TIMEOUT);
            request = buildProxy(request, PROXY_HOST, PROXY_PORT, PROXY_SCHEMA);
            return request.execute().returnContent();
        } catch (IOException e) {
            log.error("Get execute exception! url: " + request, e);
        }
        return null;
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/2/27 14:33</p>
     * <p>Desp: 根绝请求RUL和请求参数构建GET请求URL，并对请求参数做URL编码</p>
     * @param url 请求URL
     * @param params 请求参数
     * @return 追加了请求参数的URL，请求参数做了URL编码
     */
    private String buildGetUrl(String url, Map<String, String> params) {
        if(MapUtils.isNotEmpty(params)) {
            List<NameValuePair> pairs = new ArrayList<>(params.size());
            for (String key : params.keySet()) {
                pairs.add(new BasicNameValuePair(key, String.valueOf(params.get(key))));
            }
            url += "?" + URLEncodedUtils.format(pairs, Consts.UTF_8);
        }
        return url;
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/2/27 14:35</p>
     * <p>Desp: 从应答上下文获得String类型的应答结果</p>
     * @param content 应答上下文
     * @return String类型的应答结果
     */
    private String getString(Content content) {
        return null == content? null : content.asString(Consts.UTF_8);
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/2/27 14:36</p>
     * <p>Desp: 从应答上下文获得byte[]类型的应答结果</p>
     * @param content 应答上下文
     * @return byte[]类型的应答结果
     */
    private byte[] getBytes(Content content) {
        return null == content? null : content.asBytes();
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/2/27 14:37</p>
     * <p>Desp: POST请求，返回String，不带参数</p>
     * @param url 请求RUL
     * @return String类型的应答体
     */
    public String post(String url) {
        return post(url, null);
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/2/27 14:38</p>
     * <p>Desp: POST请求，返回String，带json类型的请求参数</p>
     * @param url 请求URL
     * @param json 请求参数
     * @return String类型的应答体
     */
    public String post(String url, String json) {
        Content content = executePost(url, buildJsonEntity(json));
        return getString(content);
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/2/27 14:39</p>
     * <p>Desp: POST请求，返回byte[]，不带参数</p>
     * @param url 请求URL
     * @return byte[]类型的应答体
     */
    public byte[] postBytes(String url) {
        return postBytes(url, null);
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/2/27 14:40</p>
     * <p>Desp: POST请求，返回byte[]，带json类型的参数</p>
     * @param url 请求URL
     * @param json 请求参数
     * @return byte[]类型的应答体
     */
    public byte[] postBytes(String url, String json) {
        Content content = executePost(url, buildJsonEntity(json));
        return getBytes(content);
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/2/27 14:41</p>
     * <p>Desp: POST表单请求，返回String，带参数</p>
     * @param url 请求URL
     * @param params 请求参数
     * @return String类型的应答体
     */
    public String postForm(String url, Map<String, String> params) {
        Content content = executePost(url, buildFormEntity(params));
        return getString(content);
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/2/27 14:43</p>
     * <p>Desp: POST表单请求，返回byte[]，带参数</p>
     * @param url 请求URL
     * @param params 请求参数
     * @return byte[]类型的应答体
     */
    public byte[] postFormBytes(String url, Map<String, String> params) {
        Content content = executePost(url, buildFormEntity(params));
        return getBytes(content);
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/2/27 14:44</p>
     * <p>Desp: 上传文件，返回String</p>
     * @param url 请求URL
     * @param params 请求参数
     * @param files 上传文件列表
     * @return String类型的应答体
     */
    public String upload(String url, Map<String, String> params, List<File> files) {
        Content content = executePost(url, buildMultipartEntity(params, files));
        return getString(content);
    }


    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/2/27 14:45</p>
     * <p>Desp: 自定义POST请求，格式和fluent-hc一致，返回Request对象</p>
     * @param url 请求URL
     * @param entity 请求体
     * @return 请求对象Request
     */
    private Request doPost(String url, HttpEntity entity) {
        Request request = Request.Post(url).body(entity);
        return request;
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/2/27 14:47</p>
     * <p>Desp: 通用配置的POST请求，默认配置：链接超时时间、读取超时时间</p>
     * @param url 请求URL
     * @param entity 请求体对象
     * @return 应答上下文对象Context
     */
    private Content executePost(String url, HttpEntity entity) {
        try {
            Request request = Request.Post(url).body(entity);
            request.connectTimeout(CONNECT_TIMEOUT).socketTimeout(READ_TIMEOUT);
            request = buildProxy(request, PROXY_HOST, PROXY_PORT, PROXY_SCHEMA);
            return request.execute().returnContent();
        } catch (IOException e) {
            log.error("Post execute exception! url: " + url, e);
        }
        return null;
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/2/27 14:49</p>
     * <p>Desp: 根据json字符串构建上下文类型为json的请求体对象</p>
     * @param json json字符串
     * @return json类型的请求体对象
     */
    private HttpEntity buildJsonEntity(String json) {
        return new StringEntity(json, ContentType.APPLICATION_JSON);
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/2/27 14:50</p>
     * <p>Desp: 根据请求参数构建表单类型的请求体对象 UrlEncodedFormEntity</p>
     * @param params 请求参数
     * @return 请求体对象
     */
    private HttpEntity buildFormEntity(Map<String, String> params) {
        List<NameValuePair> pairs = new ArrayList<>(params.size());
        for (String key : params.keySet()) {
            pairs.add(new BasicNameValuePair(key, String.valueOf(params.get(key))));
        }
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(pairs);
        } catch (UnsupportedEncodingException e) {
            log.error("Build FormEntity exception! params: " + params, e);
        }
        return entity;
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/2/27 14:53</p>
     * <p>Desp: 根据请求参数和上传文件列表构建上传文件类型的请求体对象 MultipartEntity</p>
     * @param params 请求参数
     * @param files 上传文件里诶包
     * @return 请求体对象
     */
    private HttpEntity buildMultipartEntity(Map<String, String> params, List<File> files) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        for (File file : files) {
            builder.addBinaryBody(file.getName(), file, ContentType.APPLICATION_OCTET_STREAM, file.getName());
        }
        for (String key : params.keySet()) {
            //设置ContentType为UTF-8,默认为text/plain; charset=ISO-8859-1,传递中文参数会乱码
            builder.addTextBody(key, params.get(key), ContentType.create("text/plain", Consts.UTF_8));
        }
        return builder.build();
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/2/27 14:56</p>
     * <p>Desp: 根据指定的主机名、端口号、协议，给请求对象设置代理</p>
     * @param request 请求对象
     * @param hostName 代理主机名
     * @param port 代理主机端口
     * @param schemeName 代理协议
     * @return 设置过代理的请求对象Request
     */
    private Request buildProxy(Request request, String hostName, Integer port, String schemeName) {
        if (StringUtils.isNotEmpty(hostName) && port != null) {
            //设置代理
            if (StringUtils.isEmpty(schemeName)) {
                schemeName = HttpHost.DEFAULT_SCHEME_NAME;
            }
            request.viaProxy(new HttpHost(hostName, port, schemeName));
        }
        return request;
    }
}