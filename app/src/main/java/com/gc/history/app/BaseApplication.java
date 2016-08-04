package com.gc.history.app;

import android.app.Application;
import android.content.Context;


import com.gc.history.service.NetService;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class BaseApplication extends Application {

    private static Context context;
    private static OkHttpClient client;
    private static Retrofit retrofit;
    private static NetService service;
    private final static long READ_TIME_OUT = 8000;
    private final static String BASEURL = "http://v.juhe.cn";

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        initHttpClient();
        initRetrofit();
        initNetService();
    }

    /**
     * 初始化OkHttpClient
     */
    private void initHttpClient() {
        client = new OkHttpClient.Builder()
                .readTimeout(READ_TIME_OUT, TimeUnit.MILLISECONDS)
                .build();
    }

    /**
     * 初始化Retrofit，设置支持RxJava和Gson
     */
    private void initRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASEURL)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
    }

    private void initNetService() {
        service = retrofit.create(NetService.class);
    }

    /**
     * 获取全局上下文
     *
     * @return 全局上下文
     */
    public static Context getContext() {
        return context;
    }

    /**
     * 网络请求实例
     *
     * @return okHttpClient
     */
    public static OkHttpClient getHttpClient() {
        return client;
    }

    public static NetService getService() {
        return service;
    }

}
