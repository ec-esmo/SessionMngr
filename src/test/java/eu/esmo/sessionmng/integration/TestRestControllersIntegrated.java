/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.integration;

import eu.esmo.sessionmng.SessionMngApplication;
import eu.esmo.sessionmng.model.TO.MngrSessionTO;
import eu.esmo.sessionmng.model.service.SessionService;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 *
 * @author nikos
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SessionMngApplication.class)
@AutoConfigureMockMvc
public class TestRestControllersIntegrated {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SessionService sessionServ;

    @Test
    public void testsStartSession() throws Exception {
        MvcResult result = mvc.perform(post("/startSession"))
                .andExpect(status().isOk())
                .andReturn();
        
        MngrSessionTO createdSession = sessionServ.findBySessionId(result.getResponse().getContentAsString());
        assertNotNull(createdSession);
        assertEquals(createdSession.getSessionId(),result.getResponse().getContentAsString());
        assertEquals(createdSession.getSessionVariables().size(),0);

    }

}
