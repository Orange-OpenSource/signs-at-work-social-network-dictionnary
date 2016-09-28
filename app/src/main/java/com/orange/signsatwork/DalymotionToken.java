package com.orange.signsatwork;


import com.orange.signsatwork.biz.domain.AuthTokenInfo;
import lombok.Getter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Getter
public class DalymotionToken {

    private AuthTokenInfo authTokenInfo;
    private DailymotionCache dailymotionCache;

    @PostConstruct
    public void retrieveToken() {
        SpringRestClient springRestClient = new SpringRestClient();
        try {
            this.authTokenInfo = springRestClient.sendTokenRequest();
            this.dailymotionCache = new DailymotionCache();
        } catch(RuntimeException r) {
            r.printStackTrace();
        }

    }
}
