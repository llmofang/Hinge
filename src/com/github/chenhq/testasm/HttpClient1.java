package com.github.chenhq.testasm;

import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpClient1 {
	
	public static String execHttp() {
        HttpClient client = new DefaultHttpClient();
        String uri = "http://www.baidu.com";
        HttpGet get = new HttpGet(uri);
        ResponseHandler<String> responseHandle = new BasicResponseHandler();
        
        String responseBody="";
        try {
			responseBody = client.execute(get, responseHandle);
		} catch (IOException e) {
			//e.printStackTrace();
		}
        
        return responseBody;
		
	}

}
