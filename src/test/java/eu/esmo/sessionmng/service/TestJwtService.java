/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.esmo.sessionmng.service.impl.JwtServiceImpl;
import eu.esmo.sessionmng.pojo.JwtValidationResponse;
import eu.esmo.sessionmng.enums.ResponseCode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import javax.crypto.spec.SecretKeySpec;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
public class TestJwtService {

    @MockBean
    private KeyStoreService keyServ;

    @MockBean
    private BlackListService blacklistServ;

    private JwtService jwtServ;

    @Before
    public void before() {
        jwtServ = new JwtServiceImpl(keyServ, blacklistServ);
    }

    @Test
    public void testBuildAndValidateWithHS256Key() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, UnsupportedEncodingException, JsonProcessingException {
        String secretKey = "QjG+wP1CbAH2z4PWlWIDkxP4oRlgK2vos5/jXFfeBw8=";
        Key key = new SecretKeySpec(secretKey.getBytes("UTF-8"), 0, secretKey.length(), "HmacSHA256");

        when(keyServ.getAlgorithm()).thenReturn(SignatureAlgorithm.HS256);
        when(keyServ.getHttpSigningKey()).thenReturn(key);
        when(keyServ.getJWTPublicKey()).thenReturn(key);
        when(keyServ.getJwtSigningKey()).thenReturn(key);

//        Map map = new HashMap();
//        map.put("var1", "val1");
//        map.put("var2", "val2");
//        MngrSessionTO payload = new MngrSessionTO("uuid1", map);
        String jwt = jwtServ.makeJwt("sessionId", null, "esmoSessionMngr", "sender", "receiver", Long.valueOf(5));
        assertNotNull(jwt);
//        System.out.println(jwt);

        assertEquals(jwtServ.validateJwt(jwt).getCode(), ResponseCode.OK);
//        System.out.println(jwtServ.validateJwt(jwt).getAdditionalData());
        assertEquals(jwtServ.validateJwt(jwt).getSessionData().getSessionId(), "sessionId");
    }

    @Test
    public void testValidateWithHS256Expired() throws UnsupportedEncodingException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        String secretKey = "QjG+wP1CbAH2z4PWlWIDkxP4oRlgK2vos5/jXFfeBw8=";
        Key key = new SecretKeySpec(secretKey.getBytes("UTF-8"), 0, secretKey.length(), "HmacSHA256");
        when(keyServ.getAlgorithm()).thenReturn(SignatureAlgorithm.HS256);
        when(keyServ.getJWTPublicKey()).thenReturn(key);

        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJwYXlsb2FkIjoie1wic2Vzc2lvbklkXCI6XCI2YzBmNzBhOC1mMzJiLTQ1MzUtYjVmNi0wZDU5NmM1MjgxM2FcIixcInNlc3Npb25WYXJpYWJsZXNcIjp7fX0iLCJpc3MiOiJlcnJvclZhbHVlIiwianRpIjoiZjU2ZDM3MzktOWFkYi00MjkyLTgwZGQtNDk5OGE0OWUwOWU0IiwiaWF0IjoxNTQwMzY4MTc2LCJleHAiOjE1NDAzNjg0NzZ9.lNcpaIZDumVigOpkucVct7Yxk-H9BB2T5hxx0nf5a4Q";

        assertEquals(jwtServ.validateJwt(jwt).getCode(), ResponseCode.ERROR);
        System.out.println(jwtServ.validateJwt(jwt).getAdditionalData());
//        assertEquals(jwtServ.validateJwt(jwt).getSessionData().getSessionVariables().get("var1"), "val1");

    }

    @Test
    public void testValidateWithHS256WrongKey() throws UnsupportedEncodingException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        String secretKey = "QjG+wP1CbAH2z4PWlWIDkxP4oRlgK2vos5/jXFfeBw8=123";
        Key key = new SecretKeySpec(secretKey.getBytes("UTF-8"), 0, secretKey.length(), "HmacSHA256");
        when(keyServ.getAlgorithm()).thenReturn(SignatureAlgorithm.HS256);
        when(keyServ.getJWTPublicKey()).thenReturn(key);

        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJwYXlsb2FkIjoie1wic2Vzc2lvbklkXCI6XCJ1dWlkMVwiLFwic2Vzc2lvblZhcmlhYmxlc1wiOntcInZhcjJcIjpcInZhbDJcIixcInZhcjFcIjpcInZhbDFcIn19In0.vffJ1DjuxPj6q6il6Q0SbjKppI7IWmOqCooB5GRxy7A";

        assertEquals(jwtServ.validateJwt(jwt).getError(), "Error Validating JWT");
        assertEquals(jwtServ.validateJwt(jwt).getCode(), ResponseCode.ERROR);

    }

    @Test
    public void testBlackListedJWT() throws JsonProcessingException, UnsupportedEncodingException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        String secretKey = "QjG+wP1CbAH2z4PWlWIDkxP4oRlgK2vos5/jXFfeBw8=";
        Key key = new SecretKeySpec(secretKey.getBytes("UTF-8"), 0, secretKey.length(), "HmacSHA256");
        when(keyServ.getAlgorithm()).thenReturn(SignatureAlgorithm.HS256);
        when(keyServ.getHttpSigningKey()).thenReturn(key);
        when(keyServ.getJWTPublicKey()).thenReturn(key);
        when(keyServ.getJwtSigningKey()).thenReturn(key);

        String jwt = jwtServ.makeJwt("sessionId", null, "esmoSessionMngr", "sender", "receiver", Long.valueOf(5));
        String jti = jwtServ.validateJwt(jwt).getJti();
        assertEquals(jwtServ.validateJwt(jwt).getCode(), ResponseCode.OK);

        when(blacklistServ.isBlacklisted(jti)).thenReturn(true);
        assertEquals(jwtServ.validateJwt(jwt).getCode(), ResponseCode.ERROR);

    }

//    @Test
    public void testJwtReceipientGen() throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException, UnrecoverableKeyException {

        String jws = "eyJhbGciOiJSUzI1NiJ9.eyJzZXNzaW9uSWQiOiI3Y2YwNGM5NC0wNmE2LTQ4MmYtODY0YS0zMjk2OWU1NTU1MDMiLCJzZW5kZXIiOiJJZFBtczAwMSIsInJlY2VpdmVyIjoiQUNNbXMwMDEiLCJpc3MiOiIiLCJqdGkiOiJjNzQ1NTNjYy1lNDUwLTQ3NTMtODAwMC0zNWQzYTVlZjNiNTIiLCJpYXQiOjE1NTE3ODM4MjYsImV4cCI6MTU1MTc4NDQyNn0.WIWMWFMMGe_0PNxZ5H0-7AW7e6WUBGO1R--dfBuVSUPMcOxVdiA-wQjy8ccj1HxnXYEQMXnkEQpmsSdYoBKNzhkiKNwg0LUlMVOFK-dGwD8moRQk6HxmNU1KbYyg8zFTQqFSqrFz5JVMT_crIfvoxBs060HqCazqkH_A_MtP0arTkCpz7TJ32pfAcT4QhBuI_TnzdTuBpQLwBEPMLpfDfBlGYHNB5qsIsOQ45ip7jvaD-eMpTFrkIdCEjb7q2M4EewJpu8jblr9bzqNlgfirbpDXrcNs7gk3Y4VpUxnzaOmkmRNmsBKkOSvWw7gjj5RGs0wD_wvRUX-290TC6XlQvw";

        ClassLoader classLoader = getClass().getClassLoader();
        String path = classLoader.getResource("testKeys/keystore.jks").getPath();
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());

        File jwtCertFile = new File(path);
        InputStream certIS = new FileInputStream(jwtCertFile);
        keystore.load(certIS, "keystorepass".toCharArray());
        Certificate cert = keystore.getCertificate("selfsigned");
        String sender = Jwts.parser().setSigningKey(cert.getPublicKey()).parseClaimsJws(jws).getBody().get("sender", String.class);
        String receiver = Jwts.parser().setSigningKey(cert.getPublicKey()).parseClaimsJws(jws).getBody().get("receiver", String.class);
        
        assertEquals(sender,"IdPms001");
        assertEquals(receiver,"ACMms001");
    }

}
