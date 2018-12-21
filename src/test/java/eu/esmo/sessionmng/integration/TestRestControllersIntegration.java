/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.esmo.sessionmng.model.TO.MngrSessionTO;
import eu.esmo.sessionmng.service.JwtService;
import eu.esmo.sessionmng.service.ParameterService;
import eu.esmo.sessionmng.service.SessionService;
import eu.esmo.sessionmng.pojo.SessionMngrResponse;
import eu.esmo.sessionmng.service.HttpSignatureService;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 * @author nikos
 */
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TestRestControllersIntegration {

    @Autowired
    private MockMvc mvc;

//    @MockBean
//    private KeyStoreService keyServ;
//    @MockBean
//    private ParameterService paramServ;
    @Autowired
    private ParameterService paramServ;

    @Autowired
    private JwtService jwtServ;

    @Autowired
    private SessionService sessionServ;

    @Autowired
    private HttpSignatureService sigServ;

    @Test
    public void testsStartSession() throws Exception {

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z");
        String nowDate = formatter.format(date);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest("".getBytes());
        String requestId = UUID.randomUUID().toString();

        MvcResult result = mvc.perform(post("/sm/startSession")
                .header("authorization", sigServ.generateSignature("hostUrl", "POST", "/sm/startSession", null, "application/x-www-form-urlencoded", requestId))
                .header("host", "hostUrl")
                .header("(request-target)", "POST /sm/startSession")
                .header("original-date", nowDate)
                .header("digest", "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digest)))
                .header("x-request-id", requestId))
                .andExpect(status().isCreated())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        SessionMngrResponse resp = mapper.readValue(result.getResponse().getContentAsString(), SessionMngrResponse.class);

        MngrSessionTO createdSession = sessionServ.findBySessionId(resp.getSessionData().getSessionId());
        assertNotNull(createdSession);
        assertEquals(result.getResponse().getContentAsString(), "{\"code\":\"NEW\",\"sessionData\":{\"sessionId\":\"" + resp.getSessionData().getSessionId() + "\",\"sessionVariables\":{}},\"additionalData\":null,\"error\":null}");

    }

    @Test
    public void deleteExistingSession() throws Exception {

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z");
        String nowDate = formatter.format(date);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest("".getBytes());
        String requestId = UUID.randomUUID().toString();

        MvcResult result = mvc.perform(post("/sm/startSession")
                .header("authorization", sigServ.generateSignature("hostUrl", "POST", "/sm/startSession", null, "application/x-www-form-urlencoded", requestId))
                .header("host", "hostUrl")
                .header("(request-target)", "POST /sm/startSession")
                .header("original-date", nowDate)
                .header("digest", "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digest)))
                .header("x-request-id", requestId))
                .andExpect(status().isCreated())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        SessionMngrResponse resp = mapper.readValue(result.getResponse().getContentAsString(), SessionMngrResponse.class);
        String sessionId = resp.getSessionData().getSessionId();
        mvc.perform(delete("/sm/endSession")
                .header("authorization", sigServ.generateSignature("hostUrl", "DELETE", "/sm/endSession", null, "application/x-www-form-urlencoded", requestId))
                .header("host", "hostUrl")
                .header("(request-target)", "DELETE /sm/startSession")
                .header("original-date", nowDate)
                .header("digest", "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digest)))
                .header("x-request-id", requestId)
                .param("sessionId", sessionId)
        )
                .andExpect(status().isOk());
        assertEquals(sessionServ.findBySessionId(sessionId), null);
    }

    @Test
    public void testUpdateSessionDataExistingSession() throws Exception {

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z");
        String nowDate = formatter.format(date);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest("".getBytes());
        String requestId = UUID.randomUUID().toString();

        MvcResult result = mvc.perform(post("/sm/startSession")
                .header("authorization", sigServ.generateSignature("hostUrl", "POST", "/sm/startSession", null, "application/x-www-form-urlencoded", requestId))
                .header("host", "hostUrl")
                .header("(request-target)", "POST /sm/startSession")
                .header("original-date", nowDate)
                .header("digest", "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digest)))
                .header("x-request-id", requestId))
                .andExpect(status().isCreated())
                .andReturn();
        ObjectMapper mapper = new ObjectMapper();
        SessionMngrResponse resp = mapper.readValue(result.getResponse().getContentAsString(), SessionMngrResponse.class);
        String sessionId = resp.getSessionData().getSessionId();

        mvc.perform(post("/sm/updateSessionData")
                .header("authorization", sigServ.generateSignature("hostUrl", "POST", "/sm/updateSessionData", null, "application/x-www-form-urlencoded", requestId))
                .header("host", "hostUrl")
                .header("(request-target)", "POST /sm/updateSessionData")
                .header("original-date", nowDate)
                .header("digest", "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digest)))
                .header("x-request-id", requestId)
                .param("sessionId", sessionId)
                .param("variableName", "var1")
                .param("dataObject", "dataObject")
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is("OK")));

        assertEquals(sessionServ.findBySessionId(sessionId).getSessionVariables().get("var1"), "dataObject");

    }

    @Test
    public void testGenerateTokenExistingSession() throws Exception {
        String secretKey = "QjG+wP1CbAH2z4PWlWIDkxP4oRlgK2vos5/jXFfeBw8=";
        Key key = new SecretKeySpec(secretKey.getBytes("UTF-8"), 0, secretKey.length(), "HmacSHA256");

//        when(keyServ.getAlgorithm()).thenReturn(SignatureAlgorithm.HS256);
//        when(keyServ.getSigningKey()).thenReturn(key);
//        when(keyServ.getJWTPublicKey()).thenReturn(key);
        when(paramServ.getProperty("ISSUER")).thenReturn("EMSO_SESSION_MANAGER");
        when(paramServ.getProperty("EXPIRES")).thenReturn("5");

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z");
        String nowDate = formatter.format(date);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest("".getBytes());
        String requestId = UUID.randomUUID().toString();

        MvcResult result = mvc.perform(post("/sm/startSession")
                .header("authorization", sigServ.generateSignature("hostUrl", "POST", "/sm/startSession", null, "application/x-www-form-urlencoded", requestId))
                .header("host", "hostUrl")
                .header("(request-target)", "POST /sm/startSession")
                .header("original-date", nowDate)
                .header("digest", "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digest)))
                .header("x-request-id", requestId))
                .andExpect(status().isCreated())
                .andReturn();
        ObjectMapper mapper = new ObjectMapper();
        SessionMngrResponse resp = mapper.readValue(result.getResponse().getContentAsString(), SessionMngrResponse.class);
        String sessionId = resp.getSessionData().getSessionId();

        MvcResult jwtResult = mvc.perform(get("/sm/generateToken")
                .header("authorization", sigServ.generateSignature("hostUrl", "GET", "/sm/generateToken", null, "application/x-www-form-urlencoded", requestId))
                .header("host", "hostUrl")
                .header("(request-target)", "GET /sm/generateToken")
                .header("original-date", nowDate)
                .header("digest", "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digest)))
                .header("x-request-id", requestId)
                .param("sessionId", sessionId)
                .param("sender", "senderId")
                .param("receiver", "ACMms001")
                .param("data", "extraData")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("NEW"))).andReturn();

        SessionMngrResponse jwtResponse = mapper.readValue(jwtResult.getResponse().getContentAsString(), SessionMngrResponse.class);
        assertNotNull(jwtResponse);
        assertNotNull(jwtResponse.getAdditionalData());
        assertEquals(jwtResponse.getSessionData(), null);

        assertEquals(jwtServ.validateJwt(jwtResponse.getAdditionalData()).getSessionData().getSessionId(), sessionId);

    }

    @Test
    public void testGenerateTokenFAKESession() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String secretKey = "QjG+wP1CbAH2z4PWlWIDkxP4oRlgK2vos5/jXFfeBw8=";
        Key key = new SecretKeySpec(secretKey.getBytes("UTF-8"), 0, secretKey.length(), "HmacSHA256");

//        when(keyServ.getAlgorithm()).thenReturn(SignatureAlgorithm.HS256);
//        when(keyServ.getSigningKey()).thenReturn(key);
//        when(keyServ.getJWTPublicKey()).thenReturn(key);
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z");
        String nowDate = formatter.format(date);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest("".getBytes());
        String requestId = UUID.randomUUID().toString();

        when(paramServ.getProperty("ISSUER")).thenReturn("EMSO_SESSION_MANAGER");
        when(paramServ.getProperty("EXPIRES")).thenReturn("5");

        MvcResult jwtResult = mvc.perform(get("/sm/generateToken")
                .header("authorization", sigServ.generateSignature("hostUrl", "GET", "/sm/generateToken", null, "application/x-www-form-urlencoded", requestId))
                .header("host", "hostUrl")
                .header("(request-target)", "GET /sm/generateToken")
                .header("original-date", nowDate)
                .header("digest", "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digest)))
                .header("x-request-id", requestId)
                .param("sessionId", "fakeSession")
                .param("sender", "senderId")
                .param("receiver", "ACMms001")
                .param("data", "extraData")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("ERROR"))).andReturn();

        SessionMngrResponse jwtResponse = mapper.readValue(jwtResult.getResponse().getContentAsString(), SessionMngrResponse.class);
        assertNotNull(jwtResponse);
        assertNotNull(jwtResponse.getError());
        assertEquals(jwtResponse.getError(), "sessionId not found");

    }

    @Test
    public void validateToken() throws JsonProcessingException, UnsupportedEncodingException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, Exception {

        String secretKey = "QjG+wP1CbAH2z4PWlWIDkxP4oRlgK2vos5/jXFfeBw8=";
        Key key = new SecretKeySpec(secretKey.getBytes("UTF-8"), 0, secretKey.length(), "HmacSHA256");

//        when(keyServ.getAlgorithm()).thenReturn(SignatureAlgorithm.HS256);
//        when(keyServ.getSigningKey()).thenReturn(key);
//        when(keyServ.getJWTPublicKey()).thenReturn(key);
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z");
        String nowDate = formatter.format(date);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest("".getBytes());
        String requestId = UUID.randomUUID().toString();

        String jwt = jwtServ.makeJwt("sessionId", "extraData", "ISSUER", "sender", "ACMms001", Long.valueOf(5));
        mvc.perform(get("/sm/validateToken")
                .header("authorization", sigServ.generateSignature("hostUrl", "GET", "/sm/validateToken", null, "application/x-www-form-urlencoded", requestId))
                .header("host", "hostUrl")
                .header("(request-target)", "GET /sm/validateToken")
                .header("original-date", nowDate)
                .header("digest", "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digest)))
                .header("x-request-id", requestId)
                .param("token", jwt)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("OK")))
                .andExpect(jsonPath("$.sessionData.sessionId", is("sessionId")));

    }

    @Test
    public void replayJwtToken() throws JsonProcessingException, UnsupportedEncodingException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, Exception {

        String secretKey = "QjG+wP1CbAH2z4PWlWIDkxP4oRlgK2vos5/jXFfeBw8=";
//        Key key = new SecretKeySpec(secretKey.getBytes("UTF-8"), 0, secretKey.length(), "HmacSHA256");

//        when(keyServ.getAlgorithm()).thenReturn(SignatureAlgorithm.HS256);
//        when(keyServ.getSigningKey()).thenReturn(key);
//        when(keyServ.getJWTPublicKey()).thenReturn(key);
//        String jwt = jwtServ.makeJwt("sessionId", "extraData", "ISSUER", "sender", "ACMms001", Long.valueOf(5));
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z");
        String nowDate = formatter.format(date);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest("".getBytes());
        String requestId = UUID.randomUUID().toString();
        String authHeader = sigServ.generateSignature("hostUrl", "GET", "/sm/validateToken", null, "application/x-www-form-urlencoded", requestId);

        String jwt = jwtServ.makeJwt("sessionId", "extraData", "ISSUER", "sender", "ACMms001", Long.valueOf(5));
        mvc.perform(get("/sm/validateToken")
                .header("authorization", authHeader)
                .header("host", "hostUrl")
                .header("(request-target)", "GET /sm/validateToken")
                .header("original-date", nowDate)
                .header("digest", "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digest)))
                .header("x-request-id", requestId)
                .param("token", jwt)
        )
                .andExpect(jsonPath("$.code", is("OK")))
                .andExpect(jsonPath("$.sessionData.sessionId", is("sessionId")));

        requestId = UUID.randomUUID().toString();
        nowDate = formatter.format(new Date());
        mvc.perform(get("/sm/validateToken")
                .header("authorization", sigServ.generateSignature("hostUrl", "GET", "/sm/validateToken", null, "application/x-www-form-urlencoded", requestId))
                .header("host", "hostUrl")
                .header("(request-target)", "GET /sm/validateToken")
                .header("original-date", nowDate)
                .header("digest", "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digest)))
                .header("x-request-id", requestId)
                .param("token", jwt)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("ERROR")));
    }

}
