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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Resource;
import org.assertj.core.util.Arrays;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 *
 * @author nikos
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@SpringBootTest
public class TestMngSessionDaoIntegration {

    @Resource
    private SessionRepository sessionRepo;

    @Test
    @Transactional
    public void testMakeNewSession() {
        MngrSession session = new MngrSession();
        sessionRepo.save(session);
        SessionVariable var = new SessionVariable("var1", "value1");
        Set set = new HashSet<>(Arrays.asList(new SessionVariable[]{var}));
        session = new MngrSession("ID1", set);
        sessionRepo.save(session);
    }

    @Test
    @Transactional
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
    @Transactional
    public void testGetSessionBySessionIdAndVariable() {
        final String UUID = "uuid2";
        final String VARIABLE = "var1";
        final String VALUE = "value1";
        SessionVariable var = new SessionVariable("var1", VALUE);
        SessionVariable var2 = new SessionVariable("var2", "value2");
        Set set = new HashSet<>(Arrays.asList(new SessionVariable[]{var, var2}));
        MngrSession expected = new MngrSession(UUID, set);
        sessionRepo.save(expected);

        String received = sessionRepo.getValueByVariableAndId(UUID, VARIABLE);
        System.out.println("Received the value :: " + received);
        assertEquals(received, VALUE);
    }

    @Test
    @Transactional
    public void testTWoDifferentVariablesWithSameName() {
        final String UUID1 = "uuid1";
        final String UUID2 = "uuid2";

        final String VARIABLE_NAME = "var1";
        final String VALUE1 = "value1";
        final String VALUE2 = "value2";

        SessionVariable var = new SessionVariable(VARIABLE_NAME, VALUE1);
        SessionVariable var2 = new SessionVariable(VARIABLE_NAME, VALUE2);
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

    @Test
    @Transactional
    public void testDeleteSession() {
        MngrSession session = new MngrSession();
        sessionRepo.save(session);
        SessionVariable var = new SessionVariable("var1", "value1");
        Set set = new HashSet<>(Arrays.asList(new SessionVariable[]{var}));
        session = new MngrSession("ID1", set);
        sessionRepo.save(session);

        Map<String, String> variableMap = new HashMap();
        sessionRepo.findBySessionId("ID1").getVariable().stream().forEach(v -> {
            variableMap.put(v.getName(), v.getValue());
        });
        assertEquals(variableMap.get("var1"), "value1");

        sessionRepo.deleteBySessionId(session.getSessionId());

        assertEquals(sessionRepo.findBySessionId("ID1"), null);
    }

    @Test
    @Transactional
    public void testGetSessionIdByNameAndValue() {
        MngrSession session = new MngrSession();
        SessionVariable var = new SessionVariable("var1", "value1");
        Set set = new HashSet<>(Arrays.asList(new SessionVariable[]{var}));
        session = new MngrSession("ID1", set);
        sessionRepo.save(session);
        assertEquals(sessionRepo.getSessionIdByVariableAndValue("var1", "value1").get().iterator().next(), "ID1");
    }

    @Test
    @Transactional
    public void testGetSessionIdByNameAndValueWrongValue() {
        MngrSession session = new MngrSession();
        SessionVariable var = new SessionVariable("var1", "value1");
        Set set = new HashSet<>(Arrays.asList(new SessionVariable[]{var}));
        session = new MngrSession("ID1", set);
        sessionRepo.save(session);
        assertEquals(sessionRepo.getSessionIdByVariableAndValue("var1", "value2"), Optional.empty());
    }

    @Test
    @Transactional
    public void testGetSessionIdByNameAndValueWrongName() {
        MngrSession session = new MngrSession();
        SessionVariable var = new SessionVariable("var2", "value1");
        Set set = new HashSet<>(Arrays.asList(new SessionVariable[]{var}));
        session = new MngrSession("ID1", set);
        sessionRepo.save(session);
        assertEquals(sessionRepo.getSessionIdByVariableAndValue("var1", "value1"), Optional.empty());
    }

    @Test
    @Transactional
    public void testGetSessionIdByNameAndValueMultipleSessionsWithCombination() {
        MngrSession session = new MngrSession();
        SessionVariable var = new SessionVariable("var1", "value1");
        Set set = new HashSet<>(Arrays.asList(new SessionVariable[]{var}));
        session = new MngrSession("ID1", set);
        sessionRepo.save(session);

        MngrSession session2 = new MngrSession();
        SessionVariable var2 = new SessionVariable("var1", "value1");
        Set set2 = new HashSet<>(Arrays.asList(new SessionVariable[]{var2}));
        session2 = new MngrSession("ID2", set2);
        sessionRepo.save(session2);

        Assert.assertTrue(sessionRepo.getSessionIdByVariableAndValue("var1", "value1").get().contains("ID1"));
        assertEquals(sessionRepo.getSessionIdByVariableAndValue("var1", "value1").get().contains("ID2"), true);
    }

}
