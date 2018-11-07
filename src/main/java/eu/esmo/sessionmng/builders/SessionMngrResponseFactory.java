/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.builders;

import eu.esmo.sessionmng.pojo.JwtValidationResponse;
import eu.esmo.sessionmng.pojo.SessionMngrResponse;

/**
 *
 * @author nikos
 */
public class SessionMngrResponseFactory {
    

    public static SessionMngrResponse makeSessionMngrResponseFromValidationResponse(JwtValidationResponse jwtResp){
        return new SessionMngrResponse(jwtResp.getCode(), jwtResp.getSessionData(), jwtResp.getAdditionalData(), jwtResp.getError());
    }


}
