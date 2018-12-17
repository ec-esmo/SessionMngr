/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.filters;

import eu.esmo.sessionmng.enums.HttpResponseEnum;
import eu.esmo.sessionmng.service.HttpSignatureService;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.GenericFilterBean;

/**
 *
 * @author nikos
 */
public class HttpSignatureFilter extends GenericFilterBean {

    private final HttpSignatureService sigServ;
    private final Logger Logger = LoggerFactory.getLogger(HttpSignatureFilter.class);

    @Autowired
    public HttpSignatureFilter(HttpSignatureService sigServ) {
        this.sigServ = sigServ;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        try {
            if (sigServ.verifySignature((HttpServletRequest) request).equals(HttpResponseEnum.AUTHORIZED)) {
                chain.doFilter(request, response);
            } else {
                throw new ServletException("Error Validating Http Signature from request");
            }
        } catch (KeyStoreException ex) {
            Logger.error(ex.getMessage());
        } catch (NoSuchAlgorithmException ex) {
            Logger.error(ex.getMessage());
        } catch (UnrecoverableKeyException ex) {
            Logger.error(ex.getMessage());
        } catch (UnsupportedEncodingException ex) {
            Logger.error(ex.getMessage());
        }

    }

}
