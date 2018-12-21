/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.pojo;

import eu.esmo.sessionmng.enums.ResponseCode;
import eu.esmo.sessionmng.model.TO.MngrSessionTO;

/**
 *
 * @author nikos
 */
public class JwtValidationResponse {

    private ResponseCode code;
    private MngrSessionTO sessionData;
    private String additionalData;
    private String error;
    private String jti;
    private String sender;
    private String receiver;

    public JwtValidationResponse(ResponseCode code, MngrSessionTO sessionData, String additionalData, String error, String jti, String sender, String receiver) {
        this.code = code;
        this.sessionData = sessionData;
        this.additionalData = additionalData;
        this.error = error;
        this.jti = jti;
        this.sender = sender;
        this.receiver = receiver;
    }

    public JwtValidationResponse() {
    }

    public ResponseCode getCode() {
        return code;
    }

    public void setCode(ResponseCode code) {
        this.code = code;
    }

    public MngrSessionTO getSessionData() {
        return sessionData;
    }

    public void setSessionData(MngrSessionTO sessionData) {
        this.sessionData = sessionData;
    }

    public String getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(String additionalData) {
        this.additionalData = additionalData;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

}
