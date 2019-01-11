/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng;

import eu.esmo.sessionmng.service.HttpSignatureService;
import eu.esmo.sessionmng.service.KeyStoreService;
import eu.esmo.sessionmng.service.MSConfigurationService;
import eu.esmo.sessionmng.service.ParameterService;
import eu.esmo.sessionmng.service.impl.HttpSignatureServiceImpl;
import eu.esmo.sessionmng.service.impl.KeyStoreServiceImpl;
import eu.esmo.sessionmng.service.impl.MSConfigurationsServiceImplSTUB;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 *
 * @author nikos
 */
@Profile("test")
@Configuration
public class TestRestControllersConfig {

    private ParameterService paramServ;
    private KeyStoreService keyServ;

    @Bean
    @Primary
    public ParameterService paramServ() {
        ParameterService paramServ = Mockito.mock(ParameterService.class);
        Mockito.when(paramServ.getProperty("CONFIG_JSON")).thenReturn(null);
        return paramServ;
    }

    @Bean
    @Primary
    public HttpSignatureService sigServ() throws InvalidKeySpecException, IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException {
//        MSConfigurationService msConfigServ = new MSConfigurationsServiceImplSTUB(paramServ);
        return new HttpSignatureServiceImpl(keyStoreService());
    }

    @Bean
    @Primary
    public KeyStoreService keyStoreService() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
        ClassLoader classLoader = getClass().getClassLoader();
        String path = classLoader.getResource("testKeys/keystore.jks").getPath();
        Mockito.when(paramServ().getProperty("KEYSTORE_PATH")).thenReturn(path);
        Mockito.when(paramServ().getProperty("KEY_PASS")).thenReturn("selfsignedpass");
        Mockito.when(paramServ().getProperty("STORE_PASS")).thenReturn("keystorepass");
        Mockito.when(paramServ().getProperty("JWT_CERT_ALIAS")).thenReturn("selfsigned");
        Mockito.when(paramServ().getProperty("HTTPSIG_CERT_ALIAS")).thenReturn("selfsigned");
        Mockito.when(paramServ().getProperty("ASYNC_SIGNATURE")).thenReturn("true");
        return new KeyStoreServiceImpl(paramServ());
    }
}
