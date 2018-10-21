/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng;

import eu.esmo.sessionmng.model.dmo.MngrSession;
import eu.esmo.sessionmng.model.dmo.SessionVariable;
import eu.esmo.sessionmng.model.service.SessionService;
import java.util.HashSet;
import java.util.Set;
import javax.transaction.Transactional;
import org.assertj.core.util.Arrays;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author nikos
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@SpringBootTest
public class SessionServiceTests {

    @Autowired
    private SessionService sessionServ;

    @Test
    public void testUpdateSessionVariable() throws ChangeSetPersister.NotFoundException {

        final String UUID = "uuid";
        SessionVariable var = new SessionVariable("var1", "value1");
        Set set = new HashSet<>(Arrays.asList(new SessionVariable[]{var}));
        MngrSession testSession = new MngrSession(UUID, set);
        sessionServ.save(testSession);

        sessionServ.updateSessionVariable(UUID, "var1", "nikos");
        assertEquals(sessionServ.getValueByVariableAndId(UUID, "var1"),"nikos");

    }

}
