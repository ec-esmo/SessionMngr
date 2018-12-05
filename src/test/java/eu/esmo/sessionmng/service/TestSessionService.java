/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.service;

import eu.esmo.sessionmng.model.dao.SessionRepository;
import eu.esmo.sessionmng.model.dmo.MngrSession;
import eu.esmo.sessionmng.model.dmo.SessionVariable;
import eu.esmo.sessionmng.service.impl.SessionServiceImpl;
import java.util.HashSet;
import java.util.Set;
import org.assertj.core.util.Arrays;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.when;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author nikos
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class TestSessionService {

    private SessionService sessionServ;
    private final String VALID_SESSION = "valid";
    private final String INVALID_SESSION = "invalid";

    @MockBean
    private SessionRepository sessionRepo;

    @Before
    public void before() {
        sessionServ = new SessionServiceImpl(sessionRepo);

        Set<SessionVariable> variables = new HashSet();
        SessionVariable v1 = new SessionVariable("var1", "val1");
        SessionVariable v2 = new SessionVariable("var2", "val2");
        variables.add(v2);
        variables.add(v1);
        MngrSession validSession = new MngrSession("sessionId", variables);

        when(sessionRepo.findBySessionId(VALID_SESSION)).thenReturn(validSession);
        when(sessionRepo.findBySessionId(INVALID_SESSION)).thenReturn(null);
        when(sessionRepo.getValueByVariableAndId("sessionId", "var1")).thenReturn("val1");

    }

    @Test
    public void testUpdateSessionVariable() throws ChangeSetPersister.NotFoundException {
        
        assertEquals(sessionServ.getValueByVariableAndId("sessionId", "var1"), "val1");
    }

    
    @Test(expected = ChangeSetPersister.NotFoundException.class)
    public void testUpdateSessionVariableErrorSession() throws ChangeSetPersister.NotFoundException {
        sessionServ.updateSessionVariable(INVALID_SESSION, "", "");
    }
    
    @Test
    public void testFindBySessionId() {
        assertEquals(sessionServ.getValueByVariableAndId("sessionId", "var1"), "val1");
    }

}
