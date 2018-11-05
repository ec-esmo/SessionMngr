/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.esmo.sessionmng.SessionMngApplication;
import eu.esmo.sessionmng.controllers.TestRestControllers.TestConfig;
import eu.esmo.sessionmng.model.TO.MngrSessionTO;
import eu.esmo.sessionmng.model.dao.SessionRepository;
import eu.esmo.sessionmng.model.dmo.MngrSession;
import eu.esmo.sessionmng.model.service.JwtService;
import eu.esmo.sessionmng.model.service.ParameterService;
import eu.esmo.sessionmng.model.service.SessionService;
import eu.esmo.sessionmng.pojo.ResponseCode;
import java.io.UnsupportedEncodingException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 * @author nikos
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(RestControllers.class)
@ContextConfiguration(classes = {TestConfig.class, SessionMngApplication.class})
public class TestRestControllers {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private SessionService sessionServ;

    @MockBean
    private SessionRepository sessionRep;

    @MockBean
    private ParameterService paramServ;

    @MockBean
    private JwtService jwtServ;

    @Configuration
    static class TestConfig {
    }

    @Before
    public void before() {

    }

    @Test
    public void testGetSessionDataNoVariableName() throws Exception {
        Map sessionVars = new HashMap();
        sessionVars.put("var1", "val1");
        sessionVars.put("var2", "val2");
        MngrSessionTO session = new MngrSessionTO("somesession", sessionVars);

        when(sessionServ.findBySessionId("somesession")).thenReturn(session);
        mvc.perform(get("/getSessionData?sessionId=somesession"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("OK")))
                .andExpect(jsonPath("$.sessionData.sessionId", is("somesession")))
                .andExpect(jsonPath("$.sessionData.sessionVariables.var1", is("val1")));
    }

    @Test
    public void testGetSessionDataWithVariableName() throws Exception {
        when(sessionServ.getValueByVariableAndId("somesession", "var1")).thenReturn("val1");

        mvc.perform(get("/getSessionData?sessionId=somesession&variableName=var1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("OK")))
                .andExpect(jsonPath("$.sessionData.sessionId", is("somesession")))
                .andExpect(jsonPath("$.sessionData.sessionVariables.var1", is("val1")))
                .andExpect(jsonPath("$.sessionData.sessionVariables.var2").doesNotExist());
    }

    @Test
    public void testGetSessionDataWithNonExistingVariable() throws Exception {
        when(sessionServ.getValueByVariableAndId("somesession", "var1")).thenReturn(null);

        mvc.perform(get("/getSessionData?sessionId=somesession&variableName=var1"))
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

        mvc.perform(post("/updateSessionData")
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
        doThrow(new ChangeSetPersister.NotFoundException()).when(sessionServ).updateSessionVariable("somesession","var1","dataObject");
        mvc.perform(post("/updateSessionData")
                .param("sessionId", "somesession")
                .param("variableName", "var1")
                .param("dataObject", "dataObject")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("ERROR")))
                .andExpect(jsonPath("$.error",is("failed to update variable var1 NOT Found")));
    }
    
    
    
    
    
    
    
    
    
//    @Test
//    public void testStartSession() throws Exception {
//        when(sessionServ.getValueByVariableAndId("somesession", "var1")).thenReturn("val1");
//
//        mvc.perform(get("/getSessionData?sessionId=somesession&variableName=var1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.sessionId", is("somesession")))
//                .andExpect(jsonPath("$.sessionVariables.var1", is("val1")))
//                .andExpect(jsonPath("$.sessionVariables.var2").doesNotExist());
//    }
}
