package com.orange.signsatwork;

import com.orange.signsatwork.biz.domain.AuthTokenInfo;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;

public class SpringRestClient {
 

    
    public static final String AUTH_SERVER_URI = "https://api.dailymotion.com/oauth/token";
    
    public static final String QPM_PASSWORD_GRANT = "?grant_type=password&client_id=accfab055d184ff9bcf3&client_secret=3dcd460d28d887fa25bc29c8031039a7edf52187&username=telsignes@gmail.com&password=?TelSignes!";
    


    /*
     * Prepare HTTP Headers.
     */
    private static HttpHeaders getHeaders(){
    	HttpHeaders headers = new HttpHeaders();
    	headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    	return headers;
    }
    
    /*
     * Add HTTP Authorization header, using Basic-Authentication to send client-credentials.
     */
    private static HttpHeaders getHeadersWithClientCredentials(){
    	String plainClientCredentials="my-trusted-client:secret";
    	//String base64ClientCredentials = new String(Base64.encodeBase64(plainClientCredentials.getBytes()));
    	
    	HttpHeaders headers = getHeaders();
    	//headers.add("Authorization", "Basic " + base64ClientCredentials);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    	return headers;
    }    
    
    /*
     * Send a POST request [on /oauth/token] to get an access-token, which will then be send with each request.
     */
    @SuppressWarnings({ "unchecked"})
	public static AuthTokenInfo sendTokenRequest(){
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("grant_type", "password");
        body.add("client_id", "accfab055d184ff9bcf3");
        body.add("client_secret", "3dcd460d28d887fa25bc29c8031039a7edf52187");
        body.add("username","telsignes@gmail.com");
        body.add("password","?TelSignes!");

        HttpEntity<?> request = new HttpEntity<Object>(body, getHeadersWithClientCredentials());


        ResponseEntity<Object> response = restTemplate.exchange(AUTH_SERVER_URI, HttpMethod.POST, request, Object.class);
        LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>)response.getBody();
        AuthTokenInfo tokenInfo = null;
        
        if(map!=null){
        	tokenInfo = new AuthTokenInfo();
        	tokenInfo.setAccess_token((String)map.get("access_token"));
        	tokenInfo.setToken_type((String)map.get("token_type"));
        	tokenInfo.setRefresh_token((String)map.get("refresh_token"));
        	tokenInfo.setExpires_in((int)map.get("expires_in"));
        	tokenInfo.setScope((String)map.get("scope"));
        	System.out.println(tokenInfo);
        	//System.out.println("access_token ="+map.get("access_token")+", token_type="+map.get("token_type")+", refresh_token="+map.get("refresh_token")
        	//+", expires_in="+map.get("expires_in")+", scope="+map.get("scope"));;
        }else{
            System.out.println("No user exist----------");
            
        }
        return tokenInfo;
    }

}