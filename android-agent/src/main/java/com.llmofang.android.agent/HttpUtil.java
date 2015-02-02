package com.llmofang.android.agent;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class HttpUtil{
  public static Proxy getProxy()
  {
      Proxy proxy = new Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress("10.0.0.100", 8080));
      return  proxy;
  }





}