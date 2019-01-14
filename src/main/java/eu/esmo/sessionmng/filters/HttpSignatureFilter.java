/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.filters;

import eu.esmo.sessionmng.enums.HttpResponseEnum;
import eu.esmo.sessionmng.service.HttpSignatureService;
import eu.esmo.sessionmng.service.MSConfigurationService;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.compress.utils.IOUtils;
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
    private final MSConfigurationService confServ;
    private final Logger Logger = LoggerFactory.getLogger(HttpSignatureFilter.class);

    @Autowired
    public HttpSignatureFilter(HttpSignatureService sigServ, MSConfigurationService confServ) {
        this.sigServ = sigServ;
        this.confServ = confServ;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        try {

            HttpServletRequest currentRequest = (HttpServletRequest) request;

            if (currentRequest.getMethod().toLowerCase().equals("post")) {
                MultiReadHttpServletRequest multiReadRequest = new MultiReadHttpServletRequest(currentRequest);
                if (sigServ.verifySignature((HttpServletRequest) multiReadRequest, confServ).equals(HttpResponseEnum.AUTHORIZED)) {
                    chain.doFilter(multiReadRequest, response);
                } else {
                    throw new ServletException("Error Validating Http Signature from request");
                }

            } else {
                if (sigServ.verifySignature((HttpServletRequest) request, confServ).equals(HttpResponseEnum.AUTHORIZED)) {
                    chain.doFilter(request, response);
                } else {
                    throw new ServletException("Error Validating Http Signature from request");
                }

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

    public class MultiReadHttpServletRequest extends HttpServletRequestWrapper {

        private ByteArrayOutputStream cachedBytes;

        public MultiReadHttpServletRequest(HttpServletRequest request) {
            super(request);
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (cachedBytes == null) {
                cacheInputStream();
            }

            return new CachedServletInputStream();
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(getInputStream()));
        }

        private void cacheInputStream() throws IOException {
            /* Cache the inputstream in order to read it multiple times. For
     * convenience, I use apache.commons IOUtils
             */
            cachedBytes = new ByteArrayOutputStream();
            IOUtils.copy(super.getInputStream(), cachedBytes);
        }

        /* An inputstream which reads the cached request body */
        public class CachedServletInputStream extends ServletInputStream {

            private ByteArrayInputStream input;

            public CachedServletInputStream() {
                /* create a new input stream from the cached request body */
                input = new ByteArrayInputStream(cachedBytes.toByteArray());
            }

            @Override
            public int read() throws IOException {
                return input.read();
            }

            @Override
            public boolean isFinished() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean isReady() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void setReadListener(ReadListener rl) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        }
    }

}
