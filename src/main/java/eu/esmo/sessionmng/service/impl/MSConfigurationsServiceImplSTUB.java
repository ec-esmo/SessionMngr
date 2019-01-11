/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.service.impl;

import eu.esmo.sessionmng.factory.MSConfigurationResponseFactory;
import eu.esmo.sessionmng.service.MSConfigurationService;
import eu.esmo.sessionmng.pojo.MSConfigurationResponse;
import eu.esmo.sessionmng.pojo.MSConfigurationResponse.MicroService;
import eu.esmo.sessionmng.service.ParameterService;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.Scanner;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 *
 * @author nikos
 */
@Service
@Profile("test")
public class MSConfigurationsServiceImplSTUB implements MSConfigurationService {

    private final static Logger log = LoggerFactory.getLogger(MSConfigurationsServiceImplSTUB.class);

    private ParameterService paramServ;

    @Autowired
    public MSConfigurationsServiceImplSTUB(ParameterService paramServ) {
        this.paramServ = paramServ;
    }

    @Override
    public MSConfigurationResponse getConfigurationJSON() {

        try {
            String configPath = StringUtils.isEmpty(paramServ.getProperty("CONFIG_JSON")) ? "configurationResponse.json" : paramServ.getProperty("CONFIG_JSON");
            return MSConfigurationResponseFactory.makeMSConfigResponseFromJSON(getFile(configPath));
        } catch (IOException e) {
            log.error("file not found ", e);
            return null;
        }

    }

    private String getFile(String fileName) {
        StringBuilder result = new StringBuilder("");
        //Get file from resources folder
        ClassLoader classLoader = getClass().getClassLoader();
        File file;
        if (StringUtils.isEmpty(paramServ.getProperty("CONFIG_JSON"))) {
            file = new File(classLoader.getResource(fileName).getFile());
        } else {
            file = new File(paramServ.getProperty("CONFIG_JSON"));
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();

    }

    @Override
    public Optional<String> getMsIDfromRSAFingerprint(String rsaFingerPrint) throws IOException {
        String configPath = StringUtils.isEmpty(paramServ.getProperty("CONFIG_JSON")) ? "configurationResponse.json" : paramServ.getProperty("CONFIG_JSON");
        MSConfigurationResponse configResp = MSConfigurationResponseFactory.makeMSConfigResponseFromJSON(getFile(configPath));
        Optional<MicroService> msMatch = Arrays.stream(configResp.getMs()).filter(msConfig -> {
            return DigestUtils.sha256Hex(msConfig.getRsaPublicKeyBinary()).equals(rsaFingerPrint);
        }).findFirst();

        if (msMatch.isPresent()) {
            return Optional.of(msMatch.get().getMsId());
        }

        return Optional.empty();
    }

    @Override
    public Optional<PublicKey> getPublicKeyFromFingerPrint(String rsaFingerPrint) throws InvalidKeyException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String configPath = StringUtils.isEmpty(paramServ.getProperty("CONFIG_JSON")) ? "configurationResponse.json" : paramServ.getProperty("CONFIG_JSON");
        MSConfigurationResponse configResp = MSConfigurationResponseFactory.makeMSConfigResponseFromJSON(getFile(configPath));
        Optional<MicroService> msMatch = Arrays.stream(configResp.getMs()).filter(msConfig -> {
            return DigestUtils.sha256Hex(msConfig.getRsaPublicKeyBinary()).equals(rsaFingerPrint);
        }).findFirst();

        if (msMatch.isPresent()) {
            //Base64.getEncoder().encodeToString(key.getEncoded())
            byte[] decoded = Base64.getDecoder().decode(msMatch.get().getRsaPublicKeyBinary());
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return Optional.of(keyFactory.generatePublic(keySpec));
        }
        return Optional.empty();
    }

}
