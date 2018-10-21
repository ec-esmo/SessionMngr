/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.model.service;

import eu.esmo.sessionmng.model.dao.SessionRepository;
import eu.esmo.sessionmng.model.dao.SessionVariableRepository;
import eu.esmo.sessionmng.model.dmo.MngrSession;
import eu.esmo.sessionmng.model.dmo.SessionVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

/**
 *
 * @author nikos
 */
@Service
@Transactional
public class SessionService {

    @Autowired
    private SessionRepository sessionRepo;

    @Autowired
    private SessionVariableRepository varRepo;

    public List<MngrSession> findAll() {
        return sessionRepo.findAll();
    }

    public MngrSession findBySessionId(String sessionId) {
        return sessionRepo.findBySessionId(sessionId);
    }

    public String getValueByVariableAndId(@Param("sessionId") String sessionId, @Param("variable") String variable) {
        return sessionRepo.getValueByVariableAndId(sessionId, variable);
    }

    public void save(MngrSession session) {
        sessionRepo.save(session);
    }

    
    //TODO
    /*
        Maybe this can be done more easily with sql?
    */
    public void updateSessionVariable(String sessionId, String variableName, String newValue) throws NotFoundException {
        MngrSession existingSession = sessionRepo.findBySessionId(sessionId);
        if (existingSession != null) {
            ArrayList<SessionVariable> variables = new ArrayList();
            variables.addAll(existingSession.getVariable());
            Optional<SessionVariable> matchVariable = variables.stream().
                    filter(v -> {
                        return v.getName().equals(variableName);
                    })
                    .findFirst();
            SessionVariable varToUpdate;
            if (!matchVariable.isPresent()) {
                varToUpdate = new SessionVariable(variableName, newValue);
            } else {
                varToUpdate = matchVariable.get();
                existingSession.getVariable().remove(varToUpdate);
            }

            varToUpdate.setValue(newValue);
            existingSession.getVariable().add(varToUpdate);
            sessionRepo.save(existingSession);
        } else {
            throw new NotFoundException();
        }

    }

}
