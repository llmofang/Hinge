  package com.llmofang.android.agent;
  
 

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;


  public class OkHttpInstrumentation
  {
//    public static HttpURLConnection open(HttpURLConnection connection)
//    {
//      if ((connection instanceof HttpsURLConnection))
//        return new HttpsURLConnectionExtension((HttpsURLConnection)connection);
//      if (connection != null) {
//        return new HttpURLConnectionExtension(connection);
//      }
//      return null;
//    }
//
//    public static HttpURLConnection openWithProxy(HttpURLConnection connection)
//    {
//      if ((connection instanceof HttpsURLConnection))
//        return new HttpsURLConnectionExtension((HttpsURLConnection)connection);
//      if (connection != null) {
//        return new HttpURLConnectionExtension(connection);
//      }
//      return null;
//    }

    public static Call newCall(OkHttpClient client,Request request)
    {
      Proxy proxy = new Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress("10.0.0.10", 8080));
      client.setProxy(proxy);
      return client.newCall(request);
    }
  }

