///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package eu.esmo.sessionmng.integration;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import eu.esmo.sessionmng.service.HttpSignatureService;
//import eu.esmo.sessionmng.service.KeyStoreService;
//import eu.esmo.sessionmng.service.ParameterService;
//import eu.esmo.sessionmng.service.impl.HttpSignatureServiceImpl;
//import eu.esmo.sessionmng.service.impl.NetworkServiceImpl;
//import java.io.IOException;
//import java.security.Key;
//import java.security.KeyStoreException;
//import java.security.NoSuchAlgorithmException;
//import java.security.UnrecoverableKeyException;
//import java.security.spec.InvalidKeySpecException;
//import org.apache.commons.codec.digest.DigestUtils;
//import org.junit.Before;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.junit4.SpringRunner;
//
///**
// *
// * @author nikos
// */
//@ActiveProfiles("test")
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class TestCmIntegration {
//
//    @Autowired
//    private KeyStoreService keyServ;
//
//    @Autowired
//    private ParameterService paramServ;
//
//    private NetworkServiceImpl netServ;
//    private ObjectMapper mapper;
//
//    @Before
//    public void init() throws InvalidKeySpecException, IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
//        mapper = new ObjectMapper();
//        Key signingKey = this.keyServ.getHttpSigningKey();
//        String fingerPrint = DigestUtils.sha256Hex(keyServ.getHttpSigPublicKey().getEncoded());
//        HttpSignatureService sigServ = new HttpSignatureServiceImpl(fingerPrint, signingKey);
//        netServ = new NetworkServiceImpl(keyServ);
//    }
//
////    @Test
////    public void getEndpointFromMSConfig() throws InvalidKeySpecException, IOException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
////
////        MSConfigurationService msConf = new MSConfigurationServiceImpl(paramServ, netServ);
////
////        Assert.assertNotNull((msConf.getConfigurationJSON()));
////
////    }
//}
