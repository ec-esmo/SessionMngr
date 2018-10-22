/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.integration;

import javax.transaction.Transactional;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import eu.esmo.sessionmng.model.dao.SessionRepository;
import eu.esmo.sessionmng.model.dmo.MngrSession;
import eu.esmo.sessionmng.model.dmo.SessionVariable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Resource;
import org.assertj.core.util.Arrays;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 *
 * @author nikos
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@SpringBootTest
public class MngSessionDaoTests {

    @Resource
    private SessionRepository sessionRepo;

    @Test
    public void testMakeNewSession() {
        MngrSession session = new MngrSession();
        sessionRepo.save(session);
        SessionVariable var = new SessionVariable("var1", "value1");
        Set set = new HashSet<>(Arrays.asList(new SessionVariable[]{var}));
        session = new MngrSession("ID1", set);
        sessionRepo.save(session);
    }

    @Test
    public void testGetSessionBySessionId() {
        final String UUID = "uuid2";
        SessionVariable var = new SessionVariable("var1", "value1");
        Set set = new HashSet<>(Arrays.asList(new SessionVariable[]{var}));
        MngrSession expected = new MngrSession(UUID, set);
        sessionRepo.save(expected);
        MngrSession received = sessionRepo.findBySessionId(UUID);
        assertNotNull(received);
        assertEquals(received.getSessionId(), expected.getSessionId());
    }

    @Test
    public void testGetSessionBySessionIdAndVariable() {
        final String UUID = "uuid2";
        final String VARIABLE = "var1";
        final String VALUE= "value1";
        SessionVariable var = new SessionVariable("var1",VALUE);
        SessionVariable var2 = new SessionVariable("var2", "value2");
        Set set = new HashSet<>(Arrays.asList(new SessionVariable[]{var, var2}));
        MngrSession expected = new MngrSession(UUID, set);
        sessionRepo.save(expected);

        String received = sessionRepo.getValueByVariableAndId(UUID, VARIABLE);
        System.out.println("Received the value :: " + received);
        assertEquals(received, VALUE);
    }
    
    
    @Test
    public void testTWoDifferentVariablesWithSameName() {
        final String UUID1 = "uuid1";
        final String UUID2 = "uuid2";
        
        final String VARIABLE_NAME = "var1";
        final String VALUE1= "value1";
        final String VALUE2= "value2";
        
        
        
        SessionVariable var = new SessionVariable(VARIABLE_NAME,VALUE1);
        SessionVariable var2 = new SessionVariable(VARIABLE_NAME,VALUE2)
                ;
        Set set = new HashSet<>(Arrays.asList(new SessionVariable[]{var}));
        Set set2 = new HashSet<>(Arrays.asList(new SessionVariable[]{var2}));


        MngrSession first = new MngrSession(UUID1, set);
        sessionRepo.save(first);

        MngrSession second = new MngrSession(UUID2, set2);
        sessionRepo.save(second);
        
        
        String received = sessionRepo.getValueByVariableAndId(UUID1, VARIABLE_NAME);
        System.out.println("Received the value :: " + received);
        assertEquals(received, VALUE1);
    }
    
    
}
