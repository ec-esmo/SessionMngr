/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.service;

import eu.esmo.sessionmng.pojo.MSConfigurationResponse;
import eu.esmo.sessionmng.service.impl.MSConfigurationsServiceImplSTUB;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.codec.digest.DigestUtils;
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
        MSConfigurationResponse.MicroService[] resp = stubServ.getConfigurationJSON();
        assertEquals(resp[0].getMsId(), "SAMLms001");
//        assertEquals(resp[0].getMsType(), "SP_AP_IDP"); deprecated attribute in config file
        assertEquals(resp[0].getPublishedAPI()[0].getApiClass().toString(), "AP");

    }

    @Test
    public void testGetMsIDfromRSAFingerprint() throws IOException, NoSuchAlgorithmException {
        String keyId = "7a9ba747ab5ac50e640a07d90611ce612b7bde775457f2e57b804517a87c813b";
        Set<String> msId = stubServ.getMsIDfromRSAFingerprint(keyId);
        assertEquals(msId.contains("ACMms001"), true);

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

    @Test
    public void testFingerPringGeneration() {
        String keyBytes = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA/O2wrcyMOVYD2Zr+xxY1Q+ghFci4TM6oipKs0CsXGOaKTOhDcCCWA3IjQoyxQADLS8uHZDSP8joHDl+TL5mGlL17VNN/x4z4ZgN0feVmB1jJd0ZYo+j40eQ5xgj3Aisq1sA+JxXZbXt0NAr4ieMAy5WN7wuGG+pGD3I/by/fStd1at7iC1Zkl5hgOiDpFmL6AuByZCqXWNkuQMQQjoXDBAzqRdo8mKsgJmpryKIYKfuKjGgg5UXb4Gh+Ft6MZKz06IhOgOEw7Z3/tS1yw4mCJDuqBR83ZpbD3MyaJoEVM5vGI3Ahmfm57Y97bAiaUBkjWLTvDdC6CBBjgHKAxnypgq7/BFuc0yR39SWO4vjtS3gsn0qs5hElRS9zgVDPVTL0nxSbYbp3WEfrKTNN06yDOuKZbD7aYSBx6lvSMyNt5MG9iONPHaPsNsVrZkCS3nmq/5LsNy7HT136tzj8qsTay9xOIuxVVGr5WER/a2yFedaXoYpc+b8R5AIzExB8GpRExPA2q3q36JEc312tTQqbohWyuSQchkTIa8htCi6DYdGys2BBFzM7JgPzz4zj+c2pM8Pek3Cekp8ke3sZ9oRZAXWBFZ+o1Qch67cCbhyJBXZg4eElZkQNhaQI7yTyNb4gbjCsdVe6LdPwS76H4KNVX0KRMZIZU0B/SKq0cTgTyc8CAwEAAQ==";
        byte[] publicBytes = Base64.getDecoder().decode(keyBytes);
        String fingerPrint = DigestUtils.sha256Hex(publicBytes);

        System.out.println(fingerPrint);
    }

}
