/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.service;

import eu.esmo.sessionmng.service.impl.MSConfigurationsServiceImplSTUB;
import eu.esmo.sessionmng.pojo.MSConfigurationResponse;
import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Optional;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author nikos
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class TestMSConfigurationService {

    private MSConfigurationService stubServ;

    @MockBean
    private ParameterService paramServ;

    @Before
    public void before() {
        Mockito.when(paramServ.getProperty("CONFIG_JSON")).thenReturn(null);
        this.stubServ = new MSConfigurationsServiceImplSTUB(paramServ);
    }

    @Test
    public void testReadConfnigJSON() {
        MSConfigurationResponse resp = stubServ.getConfigurationJSON();
        assertEquals(resp.getMs()[0].getMsId(), "SAMLms001");
        assertEquals(resp.getMs()[0].getMsType(), "SP_AP_IDP");
        assertEquals(resp.getMs()[0].getPublishedAPI()[0].getApiClass().toString(), "AP");

    }

    @Test
    public void testGetMsIDfromRSAFingerprint() throws IOException {
        String keyId = keyId = "06f336b68ba82890576f92b7d564c709cea0c0f318a09b4fbc5a502a7c93f926";
        Optional<String> msId = stubServ.getMsIDfromRSAFingerprint(keyId);
        assertEquals(msId.get(), "ACMms001");

    }

    @Test
    public void testGetPublicKeyFromFingerPrint() {
        try {
            String keyId = "06f336b68ba82890576f92b7d564c709cea0c0f318a09b4fbc5a502a7c93f926";
            String expectedString = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkvZf4Lm7dqp17tk/ICI+cCilI3yLfQraHy4pxFYDNn29l9eHnYRFnN9jBKKvOzSxf2zQkigNcHhIi96s7G4/xPL3rVaYepp/xfCKn5vkZeqg1PFOE0HqDKCnIbLxNdnHYDLICQrd1PRTdFHnwRpLouF6B3PCZpQL5XxX3WFzg2KZ2U1NIdVLJjWb3AY1SJ4kIYAOIwn6AZQPum4i5G4M9QQj3KGl164007TUx27rxzBVILpm+knxYjUiipqZ/5kiDdTxYBPR0qDVIhSl3hk9RhSI95s7unrll8rb3E8w1ORrfTQNg1UlpGgww3jZi3GLScLEK3ghwg5H5gL/2SSiEwIDAQAB";
            Optional<PublicKey> pubKey = stubServ.getPublicKeyFromFingerPrint(keyId);
            assertEquals(Base64.getEncoder().encodeToString(pubKey.get().getEncoded()), expectedString);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
