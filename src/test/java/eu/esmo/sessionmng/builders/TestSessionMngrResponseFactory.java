/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.builders;

import eu.esmo.sessionmng.factory.SessionMngrResponseFactory;
import eu.esmo.sessionmng.model.TO.MngrSessionTO;
import eu.esmo.sessionmng.pojo.JwtValidationResponse;
import eu.esmo.sessionmng.enums.ResponseCode;
import eu.esmo.sessionmng.pojo.SessionMngrResponse;
import java.util.HashMap;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author nikos
 */
public class TestSessionMngrResponseFactory {
    
    
    @Test
    public void testSessionMngrResponseGeneration(){
        JwtValidationResponse valResp = new JwtValidationResponse();
        valResp.setCode(ResponseCode.OK);
        valResp.setAdditionalData("additionalData");
        valResp.setError("error");
        valResp.setJti("jti");
        valResp.setSessionData(new MngrSessionTO("sessionID", new HashMap()));
        
        SessionMngrResponse resp = SessionMngrResponseFactory.makeSessionMngrResponseFromValidationResponse(valResp);
        
        assertEquals(resp.getAdditionalData(),valResp.getAdditionalData());
        assertEquals(resp.getCode(),valResp.getCode());
        assertEquals(resp.getError(),valResp.getError());
        assertEquals(resp.getSessionData().getSessionId(),valResp.getSessionData().getSessionId());
        assertEquals(resp.getSessionData().getSessionVariables().size(),valResp.getSessionData().getSessionVariables().size());
    
    
    }
    
    @Test
    public void testMarshallingUpdateSeessionStrings(){
    
     String testString = "{\"dataObject\": { \"sessionId\":\"6d409367-294d-400a-b83b-df261254b9d1\",\"dataObject\":{\"spRequest\":{\"issuer\":\"https://moodle.uji.es/saml/sp/metadata.xml\",\"type\":\"Request\",\"recipient\":null,\"id\":\"6c0f70a8-f32b-4535-b5f6-0d596c52813a\",\"attributes\":[{\"name\":\"http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName\",\"friendlyName\":\"CurrentGivenName\",\"isMandatory\":true},{\"name\":\"http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier\",\"friendlyName\":\"PersonIdentifier\",\"isMandatory\":true},{\"name\":\"http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName\",\"friendlyName\":\"FamilyName\",\"isMandatory\":true},{\"name\":\"http://eidas.europa.eu/attributes/naturalperson/DateOfBirth\",\"friendlyName\":\"DateOfBirth\",\"isMandatory\":true},{\"name\":\"eduPersonAffiliation\",\"isMandatory\":false}],\"properties\":{\"LoA\":\"http://eidas.europa.eu/LoA/substantial\",\"AuthnContext-Comparison\":\"minimum\",\"NameIDPolicy-AllowCreate\":\"true\",\"NameIDPolicy-Format\":\"urn:oasis:names:tc:SAML:2.0:nameid-format:persistent\",\"SPType\":\"public\",\"ProviderName\":\"Q2891006E_EA0018173\",\"IssueInstant\":\"2018-12-20T12:35:48Z\"}},\"spMetadata\":{\"entityId\":\"https://moodle.uji.es/saml/sp/metadata.xml\",\"defaultDisplayName\":\"UJI Virtual Learning Service\",\"location\":\"ES|Spain\",\"protocol\":\"SAML2-EIDAS\",\"microservice\":[\"SAMLms001\"],\"endpoints\":{\"type\":\"AssertionConsumerService\",\"method\":\"HTTP-POST\",\"url\":\"https://moodle.uji.es/saml/sp/acs.php\"},\"securityKeys\":[{\"keyType\":\"RSAPublicKey\",\"usage\":\"signing\",\"key\":\"MDAACaFgw...xFgy=\"},{\"keyType\":\"RSAPublicKey\",\"usage\":\"encryption\",\"key\":\"MDAACaFgw...xFgy=\"}],\"encryptResponses\":false,\"supportedEncryptionAlg\":[\"AES256\",\"AES512\"],\"signResponses\":true,\"supportedSigningAlg\":[\"RSA-SHA256\"]}}}}";
    
    
    }
    
    
    
}
