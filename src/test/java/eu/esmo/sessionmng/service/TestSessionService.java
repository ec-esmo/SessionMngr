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
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        MngrSession validSession = new MngrSession("sessionId", variables, LocalDateTime.now());

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

    @Test(expected = ArithmeticException.class)
    public void testExceptionThrowForMutlipMatchingSessions() {
        List<String> matchingIDs = new ArrayList<String>();
        matchingIDs.add("id1");
        matchingIDs.add("id2");
        when(sessionRepo.getSessionIdByVariableAndValue("varName", "varValue")).thenReturn(java.util.Optional.of(matchingIDs));
        sessionServ.getSessionIdByVariableAndValue("varName", "varValue");

    }

    @Test(expected = ChangeSetPersister.NotFoundException.class)
    public void testReplaceSessionVarialbeErrorSession() throws ChangeSetPersister.NotFoundException, IOException {
        sessionServ.replaceSession(INVALID_SESSION, "");
    }

//    @Test
//    public void testReplaceSessionVarialbe() throws ChangeSetPersister.NotFoundException {
//
//        Set<SessionVariable> variables = new HashSet();
//        SessionVariable v1 = new SessionVariable("var1", "val1");
//        SessionVariable v2 = new SessionVariable("var2", "val2");
//        variables.add(v2);
//        variables.add(v1);
//        MngrSession replacableSession = new MngrSession("replacableSession", variables);
//        when(sessionRepo.findBySessionId("replacableSession")).thenReturn(replacableSession);
//
//        assertEquals(sessionServ.getValueByVariableAndId("replacableSession", "var1"),"val1");
//        sessionServ.replaceSession("replacableSession", "data", "thisIsReplaced");
//        assertEquals(sessionServ.getValueByVariableAndId("replacableSession", "var1"),null);
//
//    }
}
