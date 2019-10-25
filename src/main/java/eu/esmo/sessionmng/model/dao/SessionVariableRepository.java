/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.model.dao;

import eu.esmo.sessionmng.model.dmo.SessionVariable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author nikos
 */
public interface SessionVariableRepository  extends JpaRepository<SessionVariable, Long> {


    
}
