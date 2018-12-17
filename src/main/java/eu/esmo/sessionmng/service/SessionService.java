/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.service;

import eu.esmo.sessionmng.model.TO.MngrSessionTO;
import eu.esmo.sessionmng.model.dmo.MngrSession;
import java.util.List;
import java.util.Optional;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;

/**
 *
 * @author nikos
 */
public interface SessionService {

    public List<MngrSession> findAll();

    public MngrSessionTO findBySessionId(String sessionId);

    public String getValueByVariableAndId(String sessionId, String variable);
    
    
    public Optional<String> getSessionIdByVariableAndValue(String variableName, String value) throws ArithmeticException;

    public void save(MngrSession session);
    
    public void delete(MngrSession session);
    
    public void delete(String sessionId);
    
    public void makeNewSession(String sessionId);

    public void updateSessionVariable(String sessionId, String variableName, String newValue) throws NotFoundException;

}
