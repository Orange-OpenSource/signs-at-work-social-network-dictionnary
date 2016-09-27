package com.orange.signsatwork.biz.domain;

import java.util.concurrent.TimeUnit;

public class AuthTokenInfo {

	private final long creationTime = System.currentTimeMillis();
	private String access_token;
	private String token_type;
	private String refresh_token;
	private int expires_in;
	private String scope;
	public String getAccess_token() {
		return access_token;
	}
	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}
	public String getToken_type() {
		return token_type;
	}
	public void setToken_type(String token_type) {
		this.token_type = token_type;
	}
	public String getRefresh_token() {
		return refresh_token;
	}
	public void setRefresh_token(String refresh_token) {
		this.refresh_token = refresh_token;
	}
	public int getExpires_in() {
		return expires_in;
	}
	public void setExpires_in(int expires_in) {
		this.expires_in = expires_in;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	@Override
	public String toString() {
		return "AuthTokenInfo [access_token=" + access_token + ", token_type=" + token_type + ", refresh_token="
				+ refresh_token + ", expires_in=" + expires_in + ", scope=" + scope + "]";
	}

	public boolean isExpired() {
		return isExpired(System.currentTimeMillis());
	}

	boolean isExpired(long currentTime) {
		if (expires_in == 0) {
			return false;
		}

		return (currentTime - TimeUnit.SECONDS.toMillis(expires_in)) >= creationTime;
	}
	
}
