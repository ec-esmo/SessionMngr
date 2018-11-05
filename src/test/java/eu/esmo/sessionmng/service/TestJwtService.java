/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.esmo.sessionmng.model.TO.MngrSessionTO;
import eu.esmo.sessionmng.model.service.JwtService;
import eu.esmo.sessionmng.model.service.KeyStoreService;
import eu.esmo.sessionmng.model.service.impl.JwtServiceImpl;
import eu.esmo.sessionmng.pojo.ResponseCode;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.spec.SecretKeySpec;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

    private JwtService jwtServ;

    @Before
    public void before() {
        jwtServ = new JwtServiceImpl(keyServ);
    }

    @Test
    public void testBuildAndValidateWithHS256Key() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, UnsupportedEncodingException, JsonProcessingException {
        String secretKey = "QjG+wP1CbAH2z4PWlWIDkxP4oRlgK2vos5/jXFfeBw8=";
        Key key = new SecretKeySpec(secretKey.getBytes("UTF-8"), 0, secretKey.length(), "HmacSHA256");

        when(keyServ.getAlgorithm()).thenReturn(SignatureAlgorithm.HS256);
        when(keyServ.getSigningKey()).thenReturn(key);
        when(keyServ.getPublicKey()).thenReturn(key);

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
        when(keyServ.getPublicKey()).thenReturn(key);

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
        when(keyServ.getPublicKey()).thenReturn(key);

        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJwYXlsb2FkIjoie1wic2Vzc2lvbklkXCI6XCJ1dWlkMVwiLFwic2Vzc2lvblZhcmlhYmxlc1wiOntcInZhcjJcIjpcInZhbDJcIixcInZhcjFcIjpcInZhbDFcIn19In0.vffJ1DjuxPj6q6il6Q0SbjKppI7IWmOqCooB5GRxy7A";

        assertEquals(jwtServ.validateJwt(jwt).getError(), "Error Validating JWT");
        assertEquals(jwtServ.validateJwt(jwt).getCode(), ResponseCode.ERROR);

    }

}
