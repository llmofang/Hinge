package com.llmofang.android.agent;



import org.python.antlr.ast.Context;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class Agent {
    public static final String appId;
    public static final string PHONE_NUMBER;
    public static final IMIE;
    public static final IMSI;


    private Agent() {


    }

    private static Agent single=null;

    public synchronized  static Agent getInstance() {
        if (single == null) {
            single = new Agent();
        }
        return single;
    }


    private void initialize()
    {
        TelephonyManager tm = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceid = tm.getDeviceId();//获取智能设备唯一编号
        String te1  = tm.getLine1Number();//获取本机号码
        String imei = tm.getSimSerialNumber();//获得SIM卡的序号
        String imsi = tm.getSubscriberId();//得到用户Id

    }

    public Proxy getProxy()
    {
        Proxy proxy = new Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress("10.0.0.100", 8080));
        return  proxy;
    }

}
