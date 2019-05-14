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
import java.security.spec.InvalidKeySpecException;
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

    @Test
    public void testFingerPrintGeneration() throws KeyStoreException, IOException, FileNotFoundException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, InvalidKeySpecException {

        when(paramServ.getProperty("ASYNC_SIGNATURE")).thenReturn("false");
        when(paramServ.getProperty("SIGNING_SECRET")).thenReturn("thisisasecret");
        keyServ = new KeyStoreServiceImpl(paramServ);

        String inputPKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCi7jZVwQFxQ2SY4lxjr05IexolQJJobwYzrvE5pk7AcQpG46kuJBzD8ziiqFFCGSNZ07cLWys+b5JmJ6kU44lKLVeGbEpgaO0OTBDLMk2fi5U83T8dezgWgaPFiy/N3sHPpcW2Y3ZePo0UbM7MLzv14TR+jxTOyrmwWwGwDJsz+wIDAQAB";
        assertEquals(keyServ.getFingerPrintFromStringPubKey(inputPKey), "7a9ba747ab5ac50e640a07d90611ce612b7bde775457f2e57b804517a87c813b");

        inputPKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxEaxtwgIlJlJ4/4sIXkd6vxDzKFxhQMoSV6jJsAxE5EnixdE9RY6p7ieQ0OweA9+TEaj+yugbWo/nR9TFmBpr5R0JC9A4XZ9uzGmPKlBCyRDAGuOA2Sp0KGkJHSQ5/s7IHujr5LZdo1v09QvfHPtSe+3+fzFhP/N67F20PO4n2dlE3tOEFdfZpNS7kLNQpQ+kRd6iUxDu2JwC+VfTV0SJsre8X+XtjrZDKJiq170JTpo1xcspq7IUveYaDTrDuCNpGSxKXm647+1wmUkCBdfcXB5w64UV/XW3vpis/EmgxhzTtMUG2Tj3M0BpSqNE5TWE6HP8DLscL1LlYLa4UBZRQIDAQAB";
        System.out.println(keyServ.getFingerPrintFromStringPubKey(inputPKey.replaceAll(" ", "")));

        assertEquals("29967df09f05d915400f1df2127c4887ec3529c616908a492fd433d95e8bddaf", keyServ.getFingerPrintFromStringPubKey(inputPKey.replaceAll(" ", "")));

    }

}
