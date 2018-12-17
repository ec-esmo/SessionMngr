/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.service;

import eu.esmo.sessionmng.enums.HttpResponseEnum;
import eu.esmo.sessionmng.service.impl.HttpSignatureServiceImpl;
import eu.esmo.sessionmng.service.impl.KeyStoreServiceImpl;
import eu.esmo.sessionmng.service.impl.MSConfigurationsServiceImplSTUB;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.DelegatingServletInputStream;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tomitribe.auth.signatures.Algorithm;
import org.tomitribe.auth.signatures.Base64;
import org.tomitribe.auth.signatures.Signature;
import org.tomitribe.auth.signatures.Signer;

/**
 *
 * @author nikos
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class TestHttpSignatureService {

    @MockBean
    private ParameterService paramServ;

    private KeyStoreService keyServ;

    @Before
    public void before() throws KeyStoreException, IOException, FileNotFoundException,
            NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {

        ClassLoader classLoader = getClass().getClassLoader();
        String path = classLoader.getResource("testKeys/keystore.jks").getPath();
        Mockito.when(paramServ.getProperty("KEYSTORE_PATH")).thenReturn(path);
        Mockito.when(paramServ.getProperty("KEY_PASS")).thenReturn("selfsignedpass");
        Mockito.when(paramServ.getProperty("STORE_PASS")).thenReturn("keystorepass");
        Mockito.when(paramServ.getProperty("CERT_ALIAS")).thenReturn("selfsigned");
        Mockito.when(paramServ.getProperty("ASYNC_SIGNATURE")).thenReturn("true");

        keyServ = new KeyStoreServiceImpl(paramServ);
    }

    @Test
    public void testSignature() throws IOException, InvalidKeySpecException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        MSConfigurationService msConfigServ = new MSConfigurationsServiceImplSTUB();
        HttpSignatureService httpSigServ = new HttpSignatureServiceImpl(keyServ, msConfigServ);
        System.out.println(httpSigServ.getFakeSignature());
        assertEquals(true, true);
    }

    @Test
    public void testVerifySigNotAllRequiredHeaders() throws InvalidKeySpecException, IOException, KeyStoreException, NoSuchAlgorithmException, NoSuchAlgorithmException, UnrecoverableKeyException {
        String[] requiredHeaders = {"(request-target)", "host", "original-date", "digest", "x-request-id"};
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        when(req.getHeaderNames()).thenReturn(Collections.enumeration(Arrays.asList(requiredHeaders)));
        when(req.getHeader("authorization")).thenReturn("Signature keyId=\"06f336b68ba82890576f92b7d564c709cea0c0f318a09b4fbc5a502a7c93f926\",algorithm=\"rsa-sha256\",headers=\"(request-target)\",signature=\"XlSBIdp8aiElD70VqwLpCjWulg0Pk3Z7ipMMV1bO8Jls6d8sYAu+3BDu/gIzrycc/k5LA473hH6ymVDcQWEl4bVmLS3tll5fciyMsbPFlYELsW2tpw466NlqvMxRdwMheWQC3JPv6WzISdforc2gh6yJoRFtKGi4VaP6EwS80lLgFceoRNJn2c1Z7hpe/9norY01CVZNwX/lCViHkXHxmjcYD7MYdyOxg4QGI9isG7HUPZJlVV3zRj7EOG7iEIG0p+esjU6H07C5s2wk1o7+ywDam7mXMufG90rrXN+F1tPpu/K1wjSmtENUgKOo5RjzHtBXLT8I1vDhv0sThTm+7g==\"");

        MSConfigurationService msConfigServ = new MSConfigurationsServiceImplSTUB();
        HttpSignatureService httpSigServ = new HttpSignatureServiceImpl(keyServ, msConfigServ);
        assertEquals(httpSigServ.verifySignature(req), HttpResponseEnum.HEADER_MISSING);

    }

    @Test
    public void testVerifySigRequiredHeaderMissingValues() throws InvalidKeySpecException, IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        String[] requiredHeaders = {"(request-target)", "host", "original-date", "digest", "x-request-id"};
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        when(req.getHeaderNames()).thenReturn(Collections.enumeration(Arrays.asList(requiredHeaders)));
        when(req.getHeader("authorization")).thenReturn("Signature keyId=\"06f336b68ba82890576f92b7d564c709cea0c0f318a09b4fbc5a502a7c93f926\",algorithm=\"rsa-sha256\",headers=\"(request-target) host original-date digest x-request-id\",signature=\"B0LBIwMEAn7tOE8sZoTo+9/FdWczlpzbZ8cliXmE9M9Zs5EgfoFRZINHT2PW0eAUtfz2Q0CuvcytPUCkNSaoxPnCtN6ZUHiUyh5VOxEmQsczv2eou/V8RD3mti9I7TEbTDApaOdNr/32XSGJ1gmdxZ6cQ5aY4pYzajFS6A8jBcIHuk4K+BxiJ5Z2q/+zOBqjzGHm64zzKPpjhIbTFv8c+bKe58zEz65UGXhEg+kBEsOl49V2oHiqxlukQ5kmxPK0geoMCapY2qYlm8Di8IruwzNzpI8sjuz2gXYZsA9gslxltDIGN4Hm3ccRgsbahnxSXmI0OY/hspRnUPTdLvjtWg==\"");
        when(req.getHeader("host")).thenReturn("host");
        when(req.getHeader("(request-target)")).thenReturn("(request-target)");
        when(req.getHeader("original-date")).thenReturn("original-date");
        when(req.getHeader("digest")).thenReturn("");
        when(req.getHeader("x-request-id")).thenReturn("x-request-id");

        MSConfigurationService msConfigServ = new MSConfigurationsServiceImplSTUB();
        HttpSignatureService httpSigServ = new HttpSignatureServiceImpl(keyServ, msConfigServ);
        assertEquals(httpSigServ.verifySignature(req), HttpResponseEnum.HEADER_MISSING);
    }

    @Test
    public void testHttpSignatureValidSigGetNoParams() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, UnsupportedEncodingException, IOException, FileNotFoundException, CertificateException, InvalidKeySpecException {

        final String method = "GET";
        final String uri = "https://www.esmoSMgr.com/foo/Bar";
        final Map<String, String> signatureHeaders = new HashMap<String, String>();
        signatureHeaders.put("host", "https://www.esmoSMgr.com");
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z");
        String nowDate = formatter.format(date);
        signatureHeaders.put("original-date", nowDate);

        signatureHeaders.put("Content-Type", "application/json");

        String requestId = UUID.randomUUID().toString();
        String[] requiredHeaders = {"(request-target)", "host", "original-date", "digest", "x-request-id"};

        byte[] digest = MessageDigest.getInstance("SHA-256").digest("var1=value1".getBytes());
        signatureHeaders.put("digest", "SHA-256=" + new String(Base64.encodeBase64(digest)));
        signatureHeaders.put("Accept", "*/*");
        signatureHeaders.put("Content-Length", "18");
        signatureHeaders.put("x-request-id", requestId);

        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        when(req.getHeaderNames()).thenReturn(Collections.enumeration(Arrays.asList(requiredHeaders)));
        when(req.getHeader("host")).thenReturn("https://www.esmoSMgr.com");
        when(req.getHeader("(request-target)")).thenReturn("(request-target)");
        when(req.getHeader("original-date")).thenReturn(nowDate);
        when(req.getHeader("digest")).thenReturn( "SHA-256=" + new String(Base64.encodeBase64(digest)));
        when(req.getHeader("x-request-id")).thenReturn(requestId);

        
        String keyId = "06f336b68ba82890576f92b7d564c709cea0c0f318a09b4fbc5a502a7c93f926";
        ClassLoader classLoader = getClass().getClassLoader();
        String path = classLoader.getResource("testKeys/keystore.jks").getPath();
        Mockito.when(paramServ.getProperty("KEYSTORE_PATH")).thenReturn(path);
        Mockito.when(paramServ.getProperty("KEY_PASS")).thenReturn("selfsignedpass");
        Mockito.when(paramServ.getProperty("STORE_PASS")).thenReturn("keystorepass");
        Mockito.when(paramServ.getProperty("CERT_ALIAS")).thenReturn("selfsigned");

        Mockito.when(paramServ.getProperty("ASYNC_SIGNATURE")).thenReturn("true");
        keyServ = new KeyStoreServiceImpl(paramServ);

        Algorithm algorithm = Algorithm.RSA_SHA256;
        // Here it is!
        Signer signer = new Signer(keyServ.getSigningKey(), new Signature(keyId, algorithm, null, "(request-target)", "host", "original-date", "digest", "x-request-id"));
        Signature signed = signer.sign(method, uri, signatureHeaders);

        when(req.getHeader("authorization")).thenReturn(signed.toString());

        when(req.getMethod()).thenReturn(method);
        when(req.getRequestURI()).thenReturn(uri);
        when(req.getInputStream()).thenReturn(
                new DelegatingServletInputStream(
                        new ByteArrayInputStream("var1=value1".getBytes(StandardCharsets.UTF_8))));
        System.out.println(signed.toString());
        MSConfigurationService msConfigServ = new MSConfigurationsServiceImplSTUB();
        HttpSignatureService httpSigServ = new HttpSignatureServiceImpl(keyServ, msConfigServ);
        assertEquals(httpSigServ.verifySignature(req), HttpResponseEnum.AUTHORIZED);

    }
    
    
    @Test
    public void testHttpSignatureValidSigGetWithParams() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, UnsupportedEncodingException, IOException, FileNotFoundException, CertificateException, InvalidKeySpecException {

        final String method = "GET";
        final String uri = "https://www.esmoSMgr.com/foo/Bar?test=123";
        final Map<String, String> signatureHeaders = new HashMap<String, String>();
        signatureHeaders.put("host", "https://www.esmoSMgr.com");
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z");
        String nowDate = formatter.format(date);
        signatureHeaders.put("original-date", nowDate);

        signatureHeaders.put("Content-Type", "application/json");

        String requestId = UUID.randomUUID().toString();
        String[] requiredHeaders = {"(request-target)", "host", "original-date", "digest", "x-request-id"};

        byte[] digest = MessageDigest.getInstance("SHA-256").digest("var1=value1".getBytes());
        signatureHeaders.put("digest", "SHA-256=" + new String(Base64.encodeBase64(digest)));
        signatureHeaders.put("Accept", "*/*");
        signatureHeaders.put("Content-Length", "18");
        signatureHeaders.put("x-request-id", requestId);

        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        when(req.getHeaderNames()).thenReturn(Collections.enumeration(Arrays.asList(requiredHeaders)));
        when(req.getHeader("host")).thenReturn("https://www.esmoSMgr.com");
        when(req.getHeader("(request-target)")).thenReturn("(request-target)");
        when(req.getHeader("original-date")).thenReturn(nowDate);
        when(req.getHeader("digest")).thenReturn( "SHA-256=" + new String(Base64.encodeBase64(digest)));
        when(req.getHeader("x-request-id")).thenReturn(requestId);

        
        String keyId = "06f336b68ba82890576f92b7d564c709cea0c0f318a09b4fbc5a502a7c93f926";
        ClassLoader classLoader = getClass().getClassLoader();
        String path = classLoader.getResource("testKeys/keystore.jks").getPath();
        Mockito.when(paramServ.getProperty("KEYSTORE_PATH")).thenReturn(path);
        Mockito.when(paramServ.getProperty("KEY_PASS")).thenReturn("selfsignedpass");
        Mockito.when(paramServ.getProperty("STORE_PASS")).thenReturn("keystorepass");
        Mockito.when(paramServ.getProperty("CERT_ALIAS")).thenReturn("selfsigned");

        Mockito.when(paramServ.getProperty("ASYNC_SIGNATURE")).thenReturn("true");
        keyServ = new KeyStoreServiceImpl(paramServ);

        Algorithm algorithm = Algorithm.RSA_SHA256;
        // Here it is!
        Signer signer = new Signer(keyServ.getSigningKey(), new Signature(keyId, algorithm, null, "(request-target)", "host", "original-date", "digest", "x-request-id"));
        Signature signed = signer.sign(method, uri, signatureHeaders);

        when(req.getHeader("authorization")).thenReturn(signed.toString());

        when(req.getMethod()).thenReturn(method);
        when(req.getRequestURI()).thenReturn(uri);
        when(req.getInputStream()).thenReturn(
                new DelegatingServletInputStream(
                        new ByteArrayInputStream("var1=value1".getBytes(StandardCharsets.UTF_8))));
        System.out.println(signed.toString());
        MSConfigurationService msConfigServ = new MSConfigurationsServiceImplSTUB();
        HttpSignatureService httpSigServ = new HttpSignatureServiceImpl(keyServ, msConfigServ);
        assertEquals(httpSigServ.verifySignature(req), HttpResponseEnum.AUTHORIZED);

    }
    

}
