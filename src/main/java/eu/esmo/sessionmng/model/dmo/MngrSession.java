/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.model.dmo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author nikos
 */
@Entity
@Table(name = "MngrSession")
public class MngrSession implements Serializable {

    @Id
    @GeneratedValue
    private long id;

    private String sessionId;

    @Basic
    private LocalDateTime created;

    //Unidirectional mapping
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "variable_id")
    private Set<SessionVariable> variable;

    public MngrSession() {
    }

    public MngrSession(long id, LocalDateTime created, String sessionId, Set<SessionVariable> variable) {
        this.id = id;
        this.sessionId = sessionId;
        this.variable = variable;
        this.created = created;
    }

    public MngrSession(String sessionId, Set<SessionVariable> variable, LocalDateTime created) {
        this.sessionId = sessionId;
        this.variable = variable;
        this.created = created;
    }

    public Set<SessionVariable> getVariable() {
        return variable;
    }

    public void setVariable(Set<SessionVariable> variable) {
        this.variable = variable;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

}
