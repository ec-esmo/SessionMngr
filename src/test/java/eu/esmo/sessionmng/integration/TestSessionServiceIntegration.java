/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.integration;

import eu.esmo.sessionmng.model.dmo.MngrSession;
import eu.esmo.sessionmng.model.dmo.SessionVariable;
import eu.esmo.sessionmng.service.SessionService;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.transaction.Transactional;
import org.assertj.core.util.Arrays;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author nikos
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@SpringBootTest
public class TestSessionServiceIntegration {

    @Autowired
    private SessionService sessionServ;

    @Test
    @Transactional
    public void testGetSessionIdByNameAndValueMultipleSessionsWithCombination() throws ChangeSetPersister.NotFoundException, IOException {
        MngrSession session = new MngrSession();
        SessionVariable var = new SessionVariable("var1", "value1");
        Set set = new HashSet<>(Arrays.asList(new SessionVariable[]{var}));
        session = new MngrSession("ID1", set);
        sessionServ.save(session);

        Assert.assertTrue(sessionServ.getSessionIdByVariableAndValue("var1", "value1").get().contains("ID1"));
        sessionServ.replaceSession("ID1",  "{ \"key1\":\"value1\"}");
        assertEquals(sessionServ.getValueByVariableAndId("ID1", "var1"), null);
        assertEquals(sessionServ.getValueByVariableAndId("ID1", "key1"), "value1");
    }

}
