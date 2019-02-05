/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.service.impl;

import eu.esmo.sessionmng.factory.MSConfigurationResponseFactory;
import eu.esmo.sessionmng.pojo.MSConfigurationResponse;
import eu.esmo.sessionmng.service.MSConfigurationService;
import eu.esmo.sessionmng.service.NetworkService;
import eu.esmo.sessionmng.service.ParameterService;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.NameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 *
 * @author nikos
 */
@Profile("!test")
@Service
public class MSConfigurationServiceImpl implements MSConfigurationService {

    private final ParameterService paramServ;
    private final NetworkService netServ;

    //TODO cache the response for the metadata?
    private final static Logger LOG = LoggerFactory.getLogger(MSConfigurationServiceImpl.class);

    @Autowired
    public MSConfigurationServiceImpl(ParameterService paramServ, NetworkService netServ) {
        this.paramServ = paramServ;
        this.netServ = netServ;
    }

    @Override
    public MSConfigurationResponse.MicroService[] getConfigurationJSON() {
        try {
            String sessionMngrUrl = paramServ.getProperty("CONFIGURATION_MANAGER_URL");
            List<NameValuePair> getParams = new ArrayList();
            return MSConfigurationResponseFactory.makeMSConfigResponseFromJSON(netServ.sendGet(sessionMngrUrl, "/cm/metadata/microservices", getParams));
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
            return null;
        } catch (NoSuchAlgorithmException ex) {
            LOG.error(ex.getMessage());
            return null;
        }
    }

    @Override
    public Optional<String> getMsIDfromRSAFingerprint(String rsaFingerPrint) throws IOException, NoSuchAlgorithmException {
        Optional<MSConfigurationResponse.MicroService> msMatch = Arrays.stream(getConfigurationJSON()).filter(msConfig -> {
            try {
                return DigestUtils.sha256Hex(getPublicKey(msConfig.getRsaPublicKeyBinary()).getEncoded()).equals(rsaFingerPrint);
            } catch (Exception e) {
                LOG.error("error parsing public key! " + msConfig.getRsaPublicKeyBinary());
                LOG.error(e.getLocalizedMessage());
                return false;
            }
        }).findFirst();

        if (msMatch.isPresent()) {
            return Optional.of(msMatch.get().getMsId());
        }

        return Optional.empty();
    }

    @Override
    public Optional<PublicKey> getPublicKeyFromFingerPrint(String rsaFingerPrint) throws InvalidKeyException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        MSConfigurationResponse.MicroService[] config = getConfigurationJSON();

        if (config != null) {
            LOG.info("found metadata");
            Optional<MSConfigurationResponse.MicroService> msMatch = Arrays.stream(getConfigurationJSON()).filter(msConfig -> {
                try {
                    byte[] encodedBytes = getPublicKey(msConfig.getRsaPublicKeyBinary()).getEncoded();
//                    System.out.println(
//                            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCi7jZVwQFxQ2SY4lxjr05IexolQJJobwYzrvE5pk7AcQpG46kuJBzD8ziiqFFCGSNZ07cLWys+b5JmJ6kU44lKLVeGbEpgaO0OTBDLMk2fi5U83T8dezgWgaPFiy/N3sHPpcW2Y3ZePo0UbM7MLzv14TR+jxTOyrmwWwGwDJsz+wIDAQAB"
//                            .equals(msConfig.getRsaPublicKeyBinary()));
//                    
//                    System.out.println(msConfig.getRsaPublicKeyBinary());
                    return DigestUtils.sha256Hex(encodedBytes).equals(rsaFingerPrint);
                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    LOG.error("error parsing msconfig public keys ");
                    LOG.error(e.getMessage());
                    return false;
                }
            }).findFirst();
            if (msMatch.isPresent()) {
                return Optional.of(getPublicKey(msMatch.get().getRsaPublicKeyBinary()));
            }
        } else {
            LOG.error("error connecting to configMngr " + paramServ.getProperty("CONFIGURATION_MANAGER_URL") + "/metadata/microservices");
        }
        return Optional.empty();
    }

    static byte[] getBinarySha256Fingerprint(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public RSAPublicKey getPublicKey(String keyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicBytes = Base64.getDecoder().decode(keyBytes);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);

    }

}
