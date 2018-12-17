package com.technologies.highstreet.netconf.server.basetypes;

public class BaseHTTPResponse {

	public static final BaseHTTPResponse UNKNOWN = new BaseHTTPResponse(-1, "");
	public final int code;
	public final String body;

	public BaseHTTPResponse(int code,String body)
	{
		this.code=code;
		this.body=body;
	}

	@Override
	public String toString() {
		return "BaseHTTPResponse [code=" + code + ", body=" + body + "]";
	}

	public boolean isSuccessful() {
		return this.code>=200 && this.code<300;
	}
}
