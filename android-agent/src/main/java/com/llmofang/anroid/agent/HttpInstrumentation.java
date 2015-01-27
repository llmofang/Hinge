  package com.llmofang.anroid.agent;
  
 

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.protocol.HttpContext;


import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;


 
  public final class HttpInstrumentation
  {
//    @WrapReturn(className="java/net/URL", methodName="openConnection", methodDesc="()Ljava/net/URLConnection;")
//    public static URLConnection openConnection(URLConnection connection)
//    {
//      if ((connection instanceof HttpsURLConnection)) {
//        return new HttpsURLConnectionExtension((HttpsURLConnection)connection);
//      }
//      if ((connection instanceof HttpURLConnection)) {
//        return new HttpURLConnectionExtension((HttpURLConnection)connection);
//      }
//
//      return connection;
//    }
//
//    @WrapReturn(className="java.net.URL", methodName="openConnection", methodDesc="(Ljava/net/Proxy;)Ljava/net/URLConnection;")
//    public static URLConnection openConnectionWithProxy(URLConnection connection)
//    {
//      if ((connection instanceof HttpsURLConnection)) {
//        return new HttpsURLConnectionExtension((HttpsURLConnection)connection);
//      }
//      if ((connection instanceof HttpURLConnection)) {
//        return new HttpURLConnectionExtension((HttpURLConnection)connection);
//      }
//
//      return connection;
//    }
//
//    @ReplaceCallSite
//    public static HttpResponse execute(HttpClient httpClient, HttpHost target, HttpRequest request, HttpContext context)
//      throws IOException
//    {
//      TransactionState transactionState = new TransactionState();
//      try {
//        return _(httpClient.execute(target, _(target, request, transactionState), context), transactionState);
//      }
//      catch (IOException e) {
//        httpClientError(transactionState, e);
//        throw e;
//      }
//    }
//
//    @ReplaceCallSite
//    public static <T> T execute(HttpClient httpClient, HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context)
//      throws IOException, ClientProtocolException
//    {
//      TransactionState transactionState = new TransactionState();
//      try {
//        return httpClient.execute(target, _(target, request, transactionState), _(responseHandler, transactionState), context);
//      }
//      catch (ClientProtocolException e) {
//        httpClientError(transactionState, e);
//        throw e;
//      }
//      catch (IOException e) {
//        httpClientError(transactionState, e);
//        throw e;
//      }
//    }
//
    @ReplaceCallSite()
    public static <T> T execute(HttpClient httpClient, HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws Exception {
      HttpHost proxy = new HttpHost("192.168.31.219", 8080);

      httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);

      try{
        return  httpClient.execute(target,request,responseHandler);
      }catch (Exception e){
        e.printStackTrace();
        throw e;

      }
    }
//
//    @ReplaceCallSite
//    public static HttpResponse execute(HttpClient httpClient, HttpHost target, HttpRequest request) throws IOException
//    {
//      TransactionState transactionState = new TransactionState();
//      try {
//        return _(httpClient.execute(target, _(target, request, transactionState)), transactionState);
//      }
//      catch (IOException e) {
//        httpClientError(transactionState, e);
//        throw e;
//      }
//    }
//
//    @ReplaceCallSite
//    public static HttpResponse execute(HttpClient httpClient, HttpUriRequest request, HttpContext context) throws IOException
//    {
//      TransactionState transactionState = new TransactionState();
//      try {
//        return _(httpClient.execute(_(request, transactionState), context), transactionState);
//      }
//      catch (IOException e) {
//        httpClientError(transactionState, e);
//        throw e;
//      }
//    }
//
//    @ReplaceCallSite
//    public static <T> T execute(HttpClient httpClient, HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context)
//      throws IOException, ClientProtocolException
//    {
//      TransactionState transactionState = new TransactionState();
//      try {
//        return httpClient.execute(_(request, transactionState), _(responseHandler, transactionState), context);
//      }
//      catch (ClientProtocolException e) {
//        httpClientError(transactionState, e);
//        throw e;
//      }
//      catch (IOException e) {
//        httpClientError(transactionState, e);
//        throw e;
//      }
//    }
//
    @ReplaceCallSite
    public static <T> T execute(HttpClient httpClient, HttpUriRequest request, ResponseHandler<? extends T> responseHandler)
      throws IOException, ClientProtocolException
    {
      HttpHost proxy = new HttpHost("192.168.31.219", 8080);
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
//
//    @ReplaceCallSite
//    public static HttpResponse execute(HttpClient httpClient, HttpUriRequest request) throws IOException {
//      TransactionState transactionState = new TransactionState();
//      try {
//        return _(httpClient.execute(request, transactionState), transactionState);
//      }
//      catch (IOException e) {
//        httpClientError(transactionState, e);
//        throw e;
//      }
//    }
//
//    private static void httpClientError(TransactionState transactionState, Exception e) {
//      if (!transactionState.isComplete()) {
//        TransactionStateUtil.setErrorCodeFromException(transactionState, e);
//        TransactionData transactionData = transactionState.end();
//
//        if (transactionData != null)
//          TaskQueue.queue(new HttpTransactionMeasurement(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), transactionData.getErrorCode(), transactionData.getTimestamp(), transactionData.getTime(), transactionData.getBytesSent(), transactionData.getBytesReceived(), transactionData.getAppData()));
//      }
//    }
//
//    private static HttpUriRequest _(HttpUriRequest request, TransactionState transactionState)
//    {
//      return TransactionStateUtil.inspectAndInstrument(transactionState, request);
//    }
//
//    private static HttpRequest _(HttpHost host, HttpRequest request, TransactionState transactionState) {
//      return TransactionStateUtil.inspectAndInstrument(transactionState, host, request);
//    }
//
//    private static HttpResponse _(HttpResponse response, TransactionState transactionState) {
//      return TransactionStateUtil.inspectAndInstrument(transactionState, response);
//    }
//
//    private static <T> ResponseHandler<? extends T> _(ResponseHandler<? extends T> handler, TransactionState transactionState) {
//      return ResponseHandlerImpl.wrap(handler, transactionState);
//    }
  }

