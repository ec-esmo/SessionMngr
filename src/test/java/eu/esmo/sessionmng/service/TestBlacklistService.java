/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.service;

import eu.esmo.sessionmng.SessionMngApplication;
import eu.esmo.sessionmng.service.impl.BlackListServiceImpl;
import eu.esmo.sessionmng.service.TestBlacklistService.TestConfig;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author nikos
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
public class TestBlacklistService {

    private Cache.ValueWrapper mockWrapper;
    private Cache cache;

    @Autowired
    private CacheManager cacheManager;

    @Configuration
    static class TestConfig {

        @Bean
        @Primary
        public CacheManager cacheManager() {
            return PowerMockito.mock(CacheManager.class);
        }
    }

    private BlackListService blackListService;
    private final String NOT_BLACK_LISTED = "notBlacklisted";
    private final String BLACK_LISTED = "blacklisted";

    @Before
    public void before() {
        mockWrapper = Mockito.mock(Cache.ValueWrapper.class);
        cache = PowerMockito.mock(Cache.class);
        Mockito.when(cacheManager.getCache(BlackListServiceImpl.BLACK_LIST_CACHE)).thenReturn(cache);
        blackListService = new BlackListServiceImpl(cacheManager);

        Mockito.when(cache.get(BLACK_LISTED)).thenReturn(mockWrapper);
        Mockito.when(cache.get(NOT_BLACK_LISTED)).thenReturn(null);

    }

    @Test
    public void testIsNOTBlackListed() {
        assertEquals(blackListService.isBlacklisted(NOT_BLACK_LISTED), false);
    }

    @Test
    public void testIsBlackListed() {

        assertEquals(blackListService.isBlacklisted(BLACK_LISTED), true);
    }

}
