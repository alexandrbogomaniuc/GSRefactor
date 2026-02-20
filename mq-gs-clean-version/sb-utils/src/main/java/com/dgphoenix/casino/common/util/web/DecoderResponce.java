package com.dgphoenix.casino.common.util.web;

import java.util.StringTokenizer;

public class DecoderResponce {
	private final String decriptedStr;
	private final String username;
	private final String passKey;
	private final String brandId;

	public DecoderResponce(String decriptedStr) {
		this.decriptedStr = decriptedStr;
		
		StringTokenizer st = new StringTokenizer(decriptedStr, CredentialsDecoder.DELIMETER);
		this.brandId = st.nextToken();
		this.username = st.nextToken();
		this.passKey = st.nextToken();
	}

	public String getDecriptedStr() {
		return decriptedStr;
	}

	public String getUserName() {
		return username;
	}

	public String getPassKey() {
		return passKey;
	}

	public String getBrandId() {
		return brandId;
	}

	@Override
	public String toString() {
		return "DecoderResponce [decriptedStr=" + decriptedStr + ", brandId=" + brandId + ", passKey=" + passKey
				+ ", username=" + username + "]";
	}

	
}
