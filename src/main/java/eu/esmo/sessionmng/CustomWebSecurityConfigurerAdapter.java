/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng;

import eu.esmo.sessionmng.filters.HttpSignatureFilter;
import eu.esmo.sessionmng.service.HttpSignatureService;
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

    @Autowired
    private HttpSignatureService sigServ;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterAfter(
                new HttpSignatureFilter(sigServ), BasicAuthenticationFilter.class)
                .csrf().disable();
    }
}
