package com.github.chenhq.agent.compile;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

public class ClassModificationDemo {

private int version;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String toString(){
        return "ClassCreationDemo: "+version;
    }

    public static void main(String[] args) {
        System.out.println(new ClassModificationDemo());

        HttpClient client = new DefaultHttpClient();
        String uri = "http://www.baidu.com/";
        HttpGet get = new HttpGet(uri);
        ResponseHandler<String> responseHandle = new BasicResponseHandler();

        String responseBody = "";
		try {
			responseBody = client.execute(get, responseHandle);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println(responseBody);


    }

}