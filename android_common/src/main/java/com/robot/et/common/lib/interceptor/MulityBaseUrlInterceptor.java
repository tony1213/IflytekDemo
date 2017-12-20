package com.robot.et.common.lib.interceptor;

import com.robot.et.common.lib.http.config.HttpConfig;

import java.io.IOException;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Tony 2017-12-19 15:09
 */

public class MulityBaseUrlInterceptor implements Interceptor {

    private List<String> mulityUrlList;

    public MulityBaseUrlInterceptor(List<String> listUrl){
        this.mulityUrlList = listUrl;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        //获取request
        Request request = chain.request();
        //获取request的创建者builder
        Request.Builder builder = request.newBuilder();
        //从request中获取headers，通过给定的键url_name
        List<String> headerValues = request.headers("url_pattern");
        if (headerValues != null && headerValues.size() > 0) {
            //如果有这个header，先将配置的header删除，因此header仅用作app和okhttp之间使用
            builder.removeHeader(HttpConfig.HEADER_KEY);
            //匹配获得新的BaseUrl
            String headerValue = headerValues.get(0);
            HttpUrl newBaseUrl = null;
            if ("turing".equals(headerValue)) {
                newBaseUrl = HttpUrl.parse(mulityUrlList.get(0));
            } else if ("server".equals(headerValue)) {
                newBaseUrl = HttpUrl.parse(mulityUrlList.get(1));
            } else{
                newBaseUrl = HttpUrl.parse(mulityUrlList.get(2));
            }

            //从request中获取原有的HttpUrl实例oldHttpUrl
            HttpUrl oldHttpUrl = request.url();
            //重建新的HttpUrl，修改需要修改的url部分
            HttpUrl newFullUrl = oldHttpUrl
                    .newBuilder()
                    .scheme(newBaseUrl.scheme())
                    .host(newBaseUrl.host())
                    .port(newBaseUrl.port())
                    .build();

            //重建这个request，通过builder.url(newFullUrl).build()；
            //然后返回一个response至此结束修改
            return chain.proceed(builder.url(newFullUrl).build());
        } else {
            return chain.proceed(request);
        }
    }
}
