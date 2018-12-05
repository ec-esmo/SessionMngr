/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.filters;

import eu.esmo.sessionmng.enums.HttpResponseEnum;
import eu.esmo.sessionmng.service.HttpSignatureService;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.GenericFilterBean;

/**
 *
 * @author nikos
 */
public class HttpSignatureFilter extends GenericFilterBean {

    private HttpSignatureService sigServ;

    @Autowired
    public HttpSignatureFilter(HttpSignatureService sigServ) {
        this.sigServ = sigServ;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (sigServ.verifySignature((HttpServletRequest) request).equals(HttpResponseEnum.AUTHORIZED)) {
            chain.doFilter(request, response);
        } else {
            throw new ServletException("Error Validating Http Signature from request");
        }

    }

}
