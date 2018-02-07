/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.rains.transaction.common.holder.httpclient;

import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author xiaoyu
 */
public class OkHttpTools {

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private static final OkHttpTools OK_HTTP_TOOLS = new OkHttpTools();
    private Gson gson = new Gson();

    private OkHttpTools() {

    }

    public static OkHttpTools getInstance() {
        return OK_HTTP_TOOLS;
    }

    public Request buildPost(String url, Map<String, String> params) {

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");

        FormBody.Builder formBuilder = new FormBody.Builder();
        if (params != null) {
            for (String key : params.keySet()) {
                formBuilder.add(key, params.get(key));
            }
        }

        return new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .post(formBuilder.build())
                .build();
    }


    public String post(String url, String json) throws IOException {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();

    }

    public <T> T get(String url, Map<String, String> params, Class<T> classOfT) throws IOException {
        return execute(buildPost(url, params), classOfT);
    }


    public <T> T get(String url, Type type) throws IOException {
        return execute(buildPost(url, null), type);
    }


    private <T> T execute(Request request, Class<T> classOfT) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();
        return gson.fromJson(response.body().string(), classOfT);
    }

    private String execute(Request request) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public <T> T execute(Request request, Type type) throws IOException {

        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();
        return gson.fromJson(response.body().string(), type);

    }

}
