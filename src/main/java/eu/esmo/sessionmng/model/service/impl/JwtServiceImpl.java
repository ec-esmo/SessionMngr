/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.model.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.esmo.sessionmng.model.TO.MngrSessionTO;
import eu.esmo.sessionmng.model.service.JwtService;
import eu.esmo.sessionmng.model.service.KeyStoreService;
import eu.esmo.sessionmng.pojo.ResponseCode;
import eu.esmo.sessionmng.pojo.SessionMngrResponse;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import java.io.UnsupportedEncodingException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.time.LocalDate;
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

    @Autowired
    public JwtServiceImpl(KeyStoreService keyServ) {
        this.keyServ = keyServ;
    }

    @Override
    public String makeJwt(MngrSessionTO payload, String data, String issuer, Long minutesToExpire) throws NullPointerException, JsonProcessingException, UnsupportedEncodingException, KeyStoreException, NoSuchAlgorithmException, NoSuchAlgorithmException, UnrecoverableKeyException {

        if (StringUtils.isEmpty(payload)) {
            throw new NullPointerException("payload cannot be empty");
        }
        ObjectMapper mapper = new ObjectMapper();
        JwtBuilder builder = Jwts.builder()
                .claim("payload", mapper.writeValueAsString(payload));
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
    public SessionMngrResponse validateJwt(String jws) {
        try {
            if (jws != null) {
                String payloadString = Jwts.parser().setSigningKey(keyServ.getPublicKey()).parseClaimsJws(jws).getBody().get("payload", String.class);
                String extraData = Jwts.parser().setSigningKey(keyServ.getPublicKey()).parseClaimsJws(jws).getBody().get("data", String.class);

                ObjectMapper mapper = new ObjectMapper();
                return new SessionMngrResponse(ResponseCode.OK, mapper.readValue(payloadString, MngrSessionTO.class), extraData, null);
            }
        } catch (Exception e) {
            LOG.error("Error Validating jtw ", e);
            return new SessionMngrResponse(ResponseCode.ERROR, null, null, "Error Validating JWT");
        }
        LOG.error("JWS was emptry ", jws);
        return new SessionMngrResponse(ResponseCode.ERROR, null, null, "JWT token is empty");
    }

}
