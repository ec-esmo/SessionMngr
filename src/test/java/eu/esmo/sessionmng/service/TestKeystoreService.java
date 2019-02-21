/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.service;

import eu.esmo.sessionmng.service.impl.KeyStoreServiceImpl;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author nikos
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class TestKeystoreService {

    @MockBean
    private ParameterService paramServ;

    KeyStoreService keyServ;

    @Before
    public void before() throws KeyStoreException, IOException, FileNotFoundException,
            NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {

        ClassLoader classLoader = getClass().getClassLoader();
        String path = classLoader.getResource("testKeys/keystore.jks").getPath();

        Mockito.when(paramServ.getProperty("KEYSTORE_PATH")).thenReturn(path);
        Mockito.when(paramServ.getProperty("KEY_PASS")).thenReturn("selfsignedpass");
        Mockito.when(paramServ.getProperty("STORE_PASS")).thenReturn("keystorepass");
        Mockito.when(paramServ.getProperty("JWT_CERT_ALIAS")).thenReturn("selfsigned");
        Mockito.when(paramServ.getProperty("HTTPSIG_CERT_ALIAS")).thenReturn("selfsigned");

    }

    @Test
    public void testGetSigningKeyRSA() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, UnsupportedEncodingException, IOException, FileNotFoundException, CertificateException {
        Mockito.when(paramServ.getProperty("ASYNC_SIGNATURE")).thenReturn("true");
        keyServ = new KeyStoreServiceImpl(paramServ);

        Key key = keyServ.getHttpSigningKey();
        key.getAlgorithm();
        key.getFormat();
        assertEquals(key.getAlgorithm(), "RSA");
        assertEquals(key.getFormat(), "PKCS#8");
    }

    @Test
    public void testGetPublicKeyRSA() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, IOException, FileNotFoundException, CertificateException {
        Mockito.when(paramServ.getProperty("ASYNC_SIGNATURE")).thenReturn("true");
        keyServ = new KeyStoreServiceImpl(paramServ);
        Key key = keyServ.getHttpSigPublicKey();
        key.getAlgorithm();
        key.getFormat();

        assertEquals(key.getAlgorithm(), "RSA");
        assertEquals(key.getFormat(), "X.509");
    }

    @Test
    public void testGetSingingKeyHS256() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, UnsupportedEncodingException, IOException, FileNotFoundException, CertificateException {
        //Key key = new SecretKeySpec(secretKey.getBytes("UTF-8"), 0, secretKey.length(), "HmacSHA256");
        when(paramServ.getProperty("ASYNC_SIGNATURE")).thenReturn("false");
        when(paramServ.getProperty("SIGNING_SECRET")).thenReturn("thisisasecret");
        keyServ = new KeyStoreServiceImpl(paramServ);
        Key key = keyServ.getHttpSigningKey();
        assertEquals(key.getAlgorithm(), "HmacSHA256");
        assertEquals(key.getFormat(), "RAW");

    }

}
