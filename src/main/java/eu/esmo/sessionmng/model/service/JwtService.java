/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.model.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.esmo.sessionmng.pojo.SessionMngrResponse;
import java.io.UnsupportedEncodingException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

/**
 *
 * @author nikos
 */
public interface JwtService {

    public String makeJwt(String sessionId,String data, String issuer, String sender, String receiver, Long minutesToExpire) throws JsonProcessingException, UnsupportedEncodingException, KeyStoreException,
            NoSuchAlgorithmException, NoSuchAlgorithmException, UnrecoverableKeyException;


    public SessionMngrResponse validateJwt(String jws);


}
