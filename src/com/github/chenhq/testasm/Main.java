package com.github.chenhq.testasm;

public class Main {

	public static void main(String[] args) {
		String resp = HttpClient1.execHttp();
		System.out.println("Resp: " + resp);

	}

}
