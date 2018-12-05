/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.integration;

import eu.esmo.sessionmng.service.BlackListService;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 * @author nikos
 */
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
public class BlackListIntegrationTest {

    @Autowired
    private BlackListService blackListServ;

    @Test
    public void testBlackListItem() {
        blackListServ.addToBlacklist("blackListedJti");
        assertEquals(blackListServ.isBlacklisted("blackListedJti"), true);
    }

    @Test
    public void testBlackListExpirationTime() throws InterruptedException {
        blackListServ.addToBlacklist("expiredJti");
        Thread.sleep(1000);
        assertEquals(blackListServ.isBlacklisted("expiredJti"), true);
        Thread.sleep(1000);
        // memcache holds items for 2 seconds in test profile
        assertEquals(blackListServ.isBlacklisted("expiredJti"), false);

    }

}
