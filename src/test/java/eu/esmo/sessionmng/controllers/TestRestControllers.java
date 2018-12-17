/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.controllers;

import eu.esmo.sessionmng.SessionMngApplication;
import eu.esmo.sessionmng.model.TO.MngrSessionTO;
import eu.esmo.sessionmng.model.dao.SessionRepository;
import eu.esmo.sessionmng.model.dmo.MngrSession;
import eu.esmo.sessionmng.service.HttpSignatureService;
import eu.esmo.sessionmng.service.KeyStoreService;
import eu.esmo.sessionmng.service.MSConfigurationService;
import eu.esmo.sessionmng.service.ParameterService;
import eu.esmo.sessionmng.service.SessionService;
import eu.esmo.sessionmng.service.impl.HttpSignatureServiceImpl;
import eu.esmo.sessionmng.service.impl.KeyStoreServiceImpl;
import eu.esmo.sessionmng.service.impl.MSConfigurationsServiceImplSTUB;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 * @author nikos
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@WebMvcTest(RestControllers.class)
//@ContextConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SessionMngApplication.class)
@AutoConfigureMockMvc
public class TestRestControllers {

    @Autowired
    private MockMvc mvc;

//    @Autowired
    private HttpSignatureService sigServ;

    @MockBean
    private SessionService sessionServ;

    @MockBean
    private SessionRepository sessionRep;

    @MockBean
    private ParameterService paramServ;

    private KeyStoreService keyServ;

    @Before
    public void before() throws KeyStoreException, IOException, FileNotFoundException,
            NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, InvalidKeySpecException {

        ClassLoader classLoader = getClass().getClassLoader();
        String path = classLoader.getResource("testKeys/keystore.jks").getPath();
        Mockito.when(paramServ.getProperty("KEYSTORE_PATH")).thenReturn(path);
        Mockito.when(paramServ.getProperty("KEY_PASS")).thenReturn("selfsignedpass");
        Mockito.when(paramServ.getProperty("STORE_PASS")).thenReturn("keystorepass");
        Mockito.when(paramServ.getProperty("CERT_ALIAS")).thenReturn("selfsigned");
        Mockito.when(paramServ.getProperty("ASYNC_SIGNATURE")).thenReturn("true");

        keyServ = new KeyStoreServiceImpl(paramServ);

        MSConfigurationService msConfigServ = new MSConfigurationsServiceImplSTUB();
        sigServ = new HttpSignatureServiceImpl(keyServ, msConfigServ);

    }

    @Test
    public void testGetSessionDataNoVariableName() throws Exception {
        Map sessionVars = new HashMap();
        sessionVars.put("var1", "val1");
        sessionVars.put("var2", "val2");
        MngrSessionTO session = new MngrSessionTO("somesession", sessionVars);
        when(sessionServ.findBySessionId("somesession")).thenReturn(session);

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z");
        String nowDate = formatter.format(date);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest("".getBytes());
        String requestId = UUID.randomUUID().toString();
        mvc.perform(get("/rest/getSessionData?sessionId=somesession")
                .header("authorization", sigServ.generateSignature("hostUrl", "GET", "/rest/getSessionData?sessionId=somesession", null, "application/x-www-form-urlencoded", requestId))
                .header("host", "hostUrl")
                .header("(request-target)", "GET /getSessionData?sessionId=somesession")
                .header("original-date", nowDate)
                .header("digest", "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digest)))
                .header("x-request-id", requestId)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("OK")))
                .andExpect(jsonPath("$.sessionData.sessionId", is("somesession")))
                .andExpect(jsonPath("$.sessionData.sessionVariables.var1", is("val1")));
    }

    @Test
    public void testGetSessionDataWithVariableName() throws Exception {
        when(sessionServ.getValueByVariableAndId("somesession", "var1")).thenReturn("val1");

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z");
        String nowDate = formatter.format(date);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest("".getBytes());
        String requestId = UUID.randomUUID().toString();

        mvc.perform(get("/rest/getSessionData?sessionId=somesession&variableName=var1")
                .header("authorization", sigServ.generateSignature("hostUrl", "GET", "/rest/getSessionData?sessionId=somesession&variableName=var1", null, "application/x-www-form-urlencoded", requestId))
                .header("host", "hostUrl")
                .header("(request-target)", "GET /getSessionData?sessionId=somesession")
                .header("original-date", nowDate)
                .header("digest", "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digest)))
                .header("x-request-id", requestId)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("OK")))
                .andExpect(jsonPath("$.sessionData.sessionId", is("somesession")))
                .andExpect(jsonPath("$.sessionData.sessionVariables.var1", is("val1")))
                .andExpect(jsonPath("$.sessionData.sessionVariables.var2").doesNotExist());
    }

    @Test
    public void testGetSessionDataWithNonExistingVariable() throws Exception {
        when(sessionServ.getValueByVariableAndId("somesession", "var1")).thenReturn(null);

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z");
        String nowDate = formatter.format(date);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest("".getBytes());
        String requestId = UUID.randomUUID().toString();

        mvc.perform(get("/rest/getSessionData?sessionId=somesession&variableName=var1")
                .header("authorization", sigServ.generateSignature("hostUrl", "GET", "/rest/getSessionData?sessionId=somesession&variableName=var1", null, "application/x-www-form-urlencoded", requestId))
                .header("host", "hostUrl")
                .header("(request-target)", "GET /getSessionData?sessionId=somesession")
                .header("original-date", nowDate)
                .header("digest", "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digest)))
                .header("x-request-id", requestId)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("OK")))
                .andExpect(jsonPath("$.sessionData.sessionId", is("somesession")))
                .andExpect(jsonPath("$.sessionData.sessionVariables.var1", nullValue()))
                .andExpect(jsonPath("$.sessionData.sessionVariables.var2").doesNotExist());
    }

    @Test
    public void testUpdateSessionDataExistingSession() throws Exception {
        MngrSession existingSession = new MngrSession("sessionId", new HashSet());
        when(sessionRep.findBySessionId("sessionId")).thenReturn(existingSession);

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z");
        String nowDate = formatter.format(date);
        String requestId = UUID.randomUUID().toString();

        Map<String, String> postParams = new HashMap();
        postParams.put("sessionId", "sessionId");
        postParams.put("variableName", "var1");
        postParams.put("dataObject", "dataObject");
        byte[] digest = MessageDigest.getInstance("SHA-256").digest("".getBytes()); // post parameters are added as uri parameters not in the body when form-encoding

        mvc.perform(post("/rest/updateSessionData")
                .header("authorization", sigServ.generateSignature("hostUrl", "POST", "/rest/updateSessionData", null, "application/x-www-form-urlencoded", requestId))
                .header("host", "hostUrl")
                .header("(request-target)", "POST /updateSessionData")
                .header("original-date", nowDate)
                .header("digest", "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digest)))
                .header("x-request-id", requestId)
                .param("sessionId", "sessionId")
                .param("variableName", "var1")
                .param("dataObject", "dataObject")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("OK")));
    }

    @Test
    public void testUpdateSessionDataNOTExistingSession() throws Exception {
//        when(sessionServ.updateSessionVariable("somesession",Mockito.any(String.class),Mockito.any(String.class))); //.thenThrow(new ChangeSetPersister.NotFoundException());

        doThrow(new ChangeSetPersister.NotFoundException()).when(sessionServ).updateSessionVariable("somesession", "var1", "dataObject");

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z");
        String nowDate = formatter.format(date);
        String requestId = UUID.randomUUID().toString();

        Map<String, String> postParams = new HashMap();
        postParams.put("sessionId", "somesession");
        postParams.put("variableName", "var1");
        postParams.put("dataObject", "dataObject");
        byte[] digest = MessageDigest.getInstance("SHA-256").digest("".getBytes()); // post parameters are added as uri parameters not in the body when form-encoding

        mvc.perform(post("/rest/updateSessionData")
                .header("authorization", sigServ.generateSignature("hostUrl", "POST", "/rest/updateSessionData", null, "application/x-www-form-urlencoded", requestId))
                .header("host", "hostUrl")
                .header("(request-target)", "POST /updateSessionData")
                .header("original-date", nowDate)
                .header("digest", "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digest)))
                .header("x-request-id", requestId)
                .param("sessionId", "somesession")
                .param("variableName", "var1")
                .param("dataObject", "dataObject")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("ERROR")))
                .andExpect(jsonPath("$.error", is("failed to update variable var1 NOT Found")));
    }

    @Test
    public void testGetSessionFromIdPUUUID() throws Exception {

        java.util.Optional<String> sessionIdOpt = java.util.Optional.of("sessionId");
        when(sessionServ.getSessionIdByVariableAndValue("varName", "varValue")).thenReturn(sessionIdOpt);

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z");
        String nowDate = formatter.format(date);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest("".getBytes());
        String requestId = UUID.randomUUID().toString();
        mvc.perform(get("/rest/getSession?varName=varName&varValue=varValue")
                .header("authorization", sigServ.generateSignature("hostUrl", "GET", "/rest/getSession?varName=varName&varValue=varValue", null, "application/x-www-form-urlencoded", requestId))
                .header("host", "hostUrl")
                .header("(request-target)", "GET /getSessionData?sessionId=somesession")
                .header("original-date", nowDate)
                .header("digest", "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digest)))
                .header("x-request-id", requestId)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionData.sessionId", is("sessionId")));

    }

    @Test
    public void testGetSessionFromIdPUUUIDSessionNotFound() throws Exception {

        java.util.Optional<String> sessionIdOpt = java.util.Optional.empty();
        when(sessionServ.getSessionIdByVariableAndValue("varName", "varValue")).thenReturn(sessionIdOpt);

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z");
        String nowDate = formatter.format(date);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest("".getBytes());
        String requestId = UUID.randomUUID().toString();
        mvc.perform(get("/rest/getSession?varName=varName&varValue=varValue")
                .header("authorization", sigServ.generateSignature("hostUrl", "GET", "/rest/getSession?varName=varName&varValue=varValue", null, "application/x-www-form-urlencoded", requestId))
                .header("host", "hostUrl")
                .header("(request-target)", "GET /getSessionData?sessionId=somesession")
                .header("original-date", nowDate)
                .header("digest", "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digest)))
                .header("x-request-id", requestId)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("ERROR")))
                .andExpect(jsonPath("$.error", is("No sessios found")));

    }

    @Test
    public void testGetSessionFromIdPUUUIDSessionManySessionsMatching() throws Exception {

        doThrow(new ArithmeticException()).when(sessionServ).getSessionIdByVariableAndValue("varName", "varValue");

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z");
        String nowDate = formatter.format(date);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest("".getBytes());
        String requestId = UUID.randomUUID().toString();
        mvc.perform(get("/rest/getSession?varName=varName&varValue=varValue")
                .header("authorization", sigServ.generateSignature("hostUrl", "GET", "/rest/getSession?varName=varName&varValue=varValue", null, "application/x-www-form-urlencoded", requestId))
                .header("host", "hostUrl")
                .header("(request-target)", "GET /getSessionData?sessionId=somesession")
                .header("original-date", nowDate)
                .header("digest", "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digest)))
                .header("x-request-id", requestId)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("ERROR")))
                .andExpect(jsonPath("$.error", is("More than one sessions match criteria!")));

    }

}
