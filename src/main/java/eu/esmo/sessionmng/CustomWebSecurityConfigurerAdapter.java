/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng;

import eu.esmo.sessionmng.filters.HttpSignatureFilter;
import eu.esmo.sessionmng.service.HttpSignatureService;
import eu.esmo.sessionmng.service.KeyStoreService;
import eu.esmo.sessionmng.service.MSConfigurationService;
import eu.esmo.sessionmng.service.impl.HttpSignatureServiceImpl;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.spec.InvalidKeySpecException;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 *
 * @author nikos
 */
@Configuration
public class CustomWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    //@Autowired
    private HttpSignatureService sigServ;
    //@Autowired
    private MSConfigurationService confServ;
    
    private KeyStoreService keysServ; 

    @Autowired
    public CustomWebSecurityConfigurerAdapter(KeyStoreService keysServ, MSConfigurationService confServ) throws KeyStoreException, UnsupportedEncodingException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeySpecException, IOException {

        this.sigServ = new HttpSignatureServiceImpl(DigestUtils.sha256Hex(keysServ.getHttpSigPublicKey().getEncoded()), keysServ.getHttpSigningKey());
        this.confServ = confServ;
        this.keysServ = keysServ;

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/sm/**")
                .addFilterBefore(new HttpSignatureFilter(this.keysServ, this.confServ), BasicAuthenticationFilter.class)
                .csrf().disable();
    }

}
