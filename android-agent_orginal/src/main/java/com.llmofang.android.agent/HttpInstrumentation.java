  package com.llmofang.android.agent;
  
 


import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;


  public final class HttpInstrumentation
  {
    private static final String ORGINAL_PROXY="";
    //private static final String PROXY="115.238.145.51";
    private static final String PROXY="10.0.0.10";
    //private static final  int PORT=59901;
    private static final  int PORT=8080;
    @ReplaceCallSite
    public static URLConnection openConnection(URL url) throws IOException {
      Proxy proxy = new Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(PROXY, PORT));
      try {
        return url.openConnection(proxy);
      } catch (IOException e) {
        e.printStackTrace();
        throw e;
      }
    }

    @ReplaceCallSite
    public static URLConnection openConnectionWithProxy(URL url,Proxy proxy) throws IOException {
      Proxy newproxy = new Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(PROXY, PORT));
      try {
        return url.openConnection(newproxy);
      } catch (IOException e) {
        e.printStackTrace();
        throw e;
      }
    }

    @ReplaceCallSite
    public static org.apache.http.HttpResponse execute(HttpClient httpClient, HttpHost target, HttpRequest request, HttpContext context)
      throws IOException
    {
      HttpHost proxy = new HttpHost(PROXY, PORT);
      httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);

      try{
        return  httpClient.execute(target,request,context);
      }catch (ClientProtocolException e) {
        throw e;
      }
      catch (IOException e) {
        throw e;
      }
    }

    @ReplaceCallSite
    public static <T> T execute(HttpClient httpClient, HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context)
      throws IOException, ClientProtocolException
    {
      HttpHost proxy = new HttpHost(PROXY, PORT);
      httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);

      try{
        return  httpClient.execute(target,request,responseHandler,context);
      }catch (ClientProtocolException e) {
        throw e;
      }
      catch (IOException e) {
        throw e;
      }
    }

    @ReplaceCallSite()
    public static <T> T execute(HttpClient httpClient, HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws Exception {
      HttpHost proxy = new HttpHost(PROXY, PORT);
      httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);

      try{
        return  httpClient.execute(target,request,responseHandler);
      }catch (ClientProtocolException e) {
        throw e;
      }
      catch (IOException e) {
        throw e;
      }
    }

    @ReplaceCallSite
    public static HttpResponse execute(HttpClient httpClient, HttpHost target, HttpRequest request) throws IOException
    {
      HttpHost proxy = new HttpHost(PROXY, PORT);
      httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
      try{
        return  httpClient.execute(target, request);
      }catch (ClientProtocolException e) {
        throw e;
      }
      catch (IOException e) {
        throw e;
      }
    }

    @ReplaceCallSite
    public static HttpResponse execute(HttpClient httpClient, HttpUriRequest request, HttpContext context) throws IOException
    {
      HttpHost proxy = new HttpHost(PROXY, PORT);
      httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
      try{
        return  httpClient.execute(request, context);
      }catch (ClientProtocolException e) {
        throw e;
      }
      catch (IOException e) {
        throw e;
      }
    }

    @ReplaceCallSite
    public static <T> T execute(HttpClient httpClient, HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context)
      throws IOException, ClientProtocolException
    {
      HttpHost proxy = new HttpHost(PROXY, PORT);
      httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
      try{
        return  httpClient.execute(request,responseHandler,context);
      }catch (ClientProtocolException e) {
        throw e;
      }
      catch (IOException e) {
        throw e;
      }
    }

    @ReplaceCallSite
    public static <T> T execute(HttpClient httpClient, HttpUriRequest request, ResponseHandler<? extends T> responseHandler)
      throws IOException, ClientProtocolException
    {
      HttpHost proxy = new HttpHost(PROXY, PORT);
      httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
      try {
        return httpClient.execute(request,responseHandler);
      }
      catch (ClientProtocolException e) {
        throw e;
      }
      catch (IOException e) {
        throw e;
      }
    }

    @ReplaceCallSite
    public static HttpResponse execute(HttpClient httpClient, HttpUriRequest request) throws IOException {
      HttpHost proxy = new HttpHost(PROXY, PORT);
      httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
      try {
        return httpClient.execute(request);
      }
      catch (ClientProtocolException e) {
        throw e;
      }
      catch (IOException e) {
        throw e;
      }
    }

  }

