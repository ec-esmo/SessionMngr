/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.model.dao;

import eu.esmo.sessionmng.model.dmo.MngrSession;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author nikos
 */
public interface SessionRepository extends JpaRepository<MngrSession, Long> {

    @Override
    public List<MngrSession> findAll();

    public MngrSession findBySessionId(String sessionId);

    @Query("SELECT v.value FROM MngrSession s JOIN s.variable v where s.sessionId = :sessionId and v.name = :name")
    public String getValueByVariableAndId(@Param("sessionId") String sessionId, @Param("name") String variableName);

}
