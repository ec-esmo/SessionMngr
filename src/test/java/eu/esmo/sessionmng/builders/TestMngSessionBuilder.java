/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.builders;

import eu.esmo.sessionmng.builders.MngrSessionBuilder;
import eu.esmo.sessionmng.model.TO.MngrSessionTO;
import eu.esmo.sessionmng.model.dmo.MngrSession;
import eu.esmo.sessionmng.model.dmo.SessionVariable;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author nikos
 */
public class TestMngSessionBuilder {
    

    @Test
    public void testBuildTO(){
    
        MngrSession session = new MngrSession();
        session.setId(Long.valueOf(1));
        session.setSessionId("sessionId");
        Set<SessionVariable> variables = new HashSet<>();
        SessionVariable v1 = new SessionVariable("name1", "val1");
        SessionVariable v2 = new SessionVariable("name2", "val2");
        variables.add(v2);
        variables.add(v1);
        session.setVariable(variables);
        
        
        MngrSessionTO to = MngrSessionBuilder.buildMngrSession(session);
        
        assertEquals(to.getSessionId(),session.getSessionId());
        assertEquals(to.getSessionId(),"sessionId");
        assertEquals(to.getSessionVariables().get("name1"),"val1");
        assertEquals(to.getSessionVariables().get("name2"),"val2");
    
    }

    
     @Test
    public void testBuildTOFromVariable(){
        MngrSessionTO to = MngrSessionBuilder.buildMngrSessionFromVariable("sessionId","name1","val1");
        assertEquals(to.getSessionId(),"sessionId");
        assertEquals(to.getSessionVariables().get("name1"),"val1");
        assertEquals(to.getSessionVariables().get("name2"),null);
    
    }
    
}
