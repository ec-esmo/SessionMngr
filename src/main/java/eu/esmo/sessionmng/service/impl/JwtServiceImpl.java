/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.esmo.sessionmng.model.TO.MngrSessionTO;
import eu.esmo.sessionmng.service.BlackListService;
import eu.esmo.sessionmng.service.JwtService;
import eu.esmo.sessionmng.service.KeyStoreService;
import eu.esmo.sessionmng.pojo.JwtValidationResponse;
import eu.esmo.sessionmng.enums.ResponseCode;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import java.io.UnsupportedEncodingException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 *
 * @author nikos
 */
@Service
public class JwtServiceImpl implements JwtService {

    private final static Logger LOG = LoggerFactory.getLogger(JwtServiceImpl.class);
    private KeyStoreService keyServ;
    private BlackListService blackListServ;

    @Autowired
    public JwtServiceImpl(KeyStoreService keyServ, BlackListService blacklistServ) {
        this.keyServ = keyServ;
        this.blackListServ = blacklistServ;
    }

    @Override
    public String makeJwt(String payload, String data, String issuer, String sender, String receiver, Long minutesToExpire) throws NullPointerException, JsonProcessingException, UnsupportedEncodingException, KeyStoreException, NoSuchAlgorithmException, NoSuchAlgorithmException, UnrecoverableKeyException {

        if (StringUtils.isEmpty(payload)) {
            throw new NullPointerException("payload cannot be empty");
        }
//        ObjectMapper mapper = new ObjectMapper();
        JwtBuilder builder = Jwts.builder()
                .claim("sessionId", payload)
                .claim("sender", sender)
                .claim("receiver", receiver);
        if (!StringUtils.isEmpty(data)) {
            builder.claim("data", data);
        }
        LocalDateTime nowDate = LocalDateTime.now();
        LocalDateTime expireDate = LocalDateTime.now().plusMinutes(minutesToExpire);
        builder.setIssuer(issuer)
                .setId(UUID.randomUUID().toString())
                //                .setIssuedAt(Date.from(nowDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .setIssuedAt(Date.from(nowDate.atZone(ZoneId.systemDefault())
                        .toInstant()))
                .setExpiration(Date.from(expireDate.atZone(ZoneId.systemDefault())
                        .toInstant()));

        return builder.signWith(keyServ.getSigningKey(), keyServ.getAlgorithm()).compact();
    }

    @Override
    public JwtValidationResponse validateJwt(String jws) {
        try {
            if (jws != null) {
                String sessionId = Jwts.parser().setSigningKey(keyServ.getPublicKey()).parseClaimsJws(jws).getBody().get("sessionId", String.class);
                String extraData = Jwts.parser().setSigningKey(keyServ.getPublicKey()).parseClaimsJws(jws).getBody().get("data", String.class);
                String jti = Jwts.parser().setSigningKey(keyServ.getPublicKey()).parseClaimsJws(jws).getBody().getId();

                if (blackListServ.isBlacklisted(jti)) {
                    throw new KeyStoreException("JWT is blacklisted");
                } else {
                    MngrSessionTO responseSession = new MngrSessionTO();
                    responseSession.setSessionId(sessionId);
                    return new JwtValidationResponse(ResponseCode.OK, responseSession, extraData, null, jti);
                }
            }
        } catch (KeyStoreException e) {
            LOG.error("Error Validating jtw, jti blacklisted ", e.getMessage());
            return new JwtValidationResponse(ResponseCode.ERROR, null, null, "JWT is blacklisted", null);

        } catch (Exception e) {
            LOG.error("Error Validating jtw ", e.getMessage());
            return new JwtValidationResponse(ResponseCode.ERROR, null, null, "Error Validating JWT", null);
        }
        LOG.error("JWS was emptry ", jws);
        return new JwtValidationResponse(ResponseCode.ERROR, null, null, "JWT token is empty", null);
    }

}
