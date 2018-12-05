/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.builders;

import eu.esmo.sessionmng.factory.SessionMngrResponseFactory;
import eu.esmo.sessionmng.model.TO.MngrSessionTO;
import eu.esmo.sessionmng.pojo.JwtValidationResponse;
import eu.esmo.sessionmng.enums.ResponseCode;
import eu.esmo.sessionmng.pojo.SessionMngrResponse;
import java.util.HashMap;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author nikos
 */
public class TestSessionMngrResponseFactory {
    
    
    @Test
    public void testSessionMngrResponseGeneration(){
        JwtValidationResponse valResp = new JwtValidationResponse();
        valResp.setCode(ResponseCode.OK);
        valResp.setAdditionalData("additionalData");
        valResp.setError("error");
        valResp.setJti("jti");
        valResp.setSessionData(new MngrSessionTO("sessionID", new HashMap()));
        
        SessionMngrResponse resp = SessionMngrResponseFactory.makeSessionMngrResponseFromValidationResponse(valResp);
        
        assertEquals(resp.getAdditionalData(),valResp.getAdditionalData());
        assertEquals(resp.getCode(),valResp.getCode());
        assertEquals(resp.getError(),valResp.getError());
        assertEquals(resp.getSessionData().getSessionId(),valResp.getSessionData().getSessionId());
        assertEquals(resp.getSessionData().getSessionVariables().size(),valResp.getSessionData().getSessionVariables().size());
    
    
    }
    
}
