/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.builders;

import eu.esmo.sessionmng.model.TO.MngrSessionTO;
import eu.esmo.sessionmng.model.dmo.MngrSession;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author nikos
 */
public class MngrSessionFactory {

    public static MngrSessionTO getMngrSessionTO(MngrSession session) {
        if (session != null) {
            MngrSessionTO sessionTO = new MngrSessionTO();
            sessionTO.setSessionId(session.getSessionId());
            Map<String, String> variables = new HashMap();
            session.getVariable().stream().forEach(sessionVariable -> {
                variables.put(sessionVariable.getName(), sessionVariable.getValue());
            });
            sessionTO.setSessionVariables(variables);
            return sessionTO;
        }
        return null;
    }

    public static MngrSessionTO makeMngrSessionTOFromVariableAndSessionId(String sessionId, String variableName, String variableValue) {
        MngrSessionTO sessionTO = new MngrSessionTO();
        sessionTO.setSessionId(sessionId);
        Map<String, String> variables = new HashMap();
        variables.put(variableName, variableValue);
        sessionTO.setSessionVariables(variables);
        return sessionTO;
    }

}
