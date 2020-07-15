/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng;

import com.google.code.ssm.Cache;
import com.google.code.ssm.CacheFactory;
import com.google.code.ssm.config.AddressProvider;
import com.google.code.ssm.config.DefaultAddressProvider;
import com.google.code.ssm.providers.CacheConfiguration;
import com.google.code.ssm.providers.spymemcached.MemcacheClientFactoryImpl;
import com.google.code.ssm.spring.ExtendedSSMCacheManager;
import com.google.code.ssm.spring.SSMCache;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
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
public class TestMemCacheConfig {

    private final static Logger log = LoggerFactory.getLogger(MemCacheConfig.class);
    private String _memcachedHost = "127.0.0.1"; //Machine where memcached is running
    private int _memcachedPort = 11111;    //Port on which memcached is running
    public final static String BLACKLIST = "blackList";

    @Bean
    @Primary
    public CacheManager cacheManager() {
        //Extended manager used as it will give custom-expiry value facility in future if needed
        ExtendedSSMCacheManager ssmCacheManager = new ExtendedSSMCacheManager();

        log.info("injecting TEST CACHE MANAGER");

        //We can create more than one cache, hence list
        List<SSMCache> cacheList = new ArrayList<SSMCache>();

        SSMCache testCache = createNewCache(_memcachedHost, _memcachedPort,
                BLACKLIST, 2);
        cacheList.add(testCache);

        SSMCache dummy = createNewCache(_memcachedHost, _memcachedPort,
                "sessionCache", 0);
        cacheList.add(dummy);
        //Adding cache list to cache manager
        ssmCacheManager.setCaches(cacheList);

        return ssmCacheManager;
    }

    private SSMCache createNewCache(String memcachedServer, int port,
            String cacheName, int expiryTimeInSeconds) {
        //Basic client factory to be used. This is SpyMemcached for now.
        MemcacheClientFactoryImpl cacheClientFactory = new MemcacheClientFactoryImpl();

        //Memcached server address parameters
        //"127.0.0.1:11211"
        String serverAddressStr = memcachedServer + ":" + String.valueOf(port);
        AddressProvider addressProvider = new DefaultAddressProvider(serverAddressStr);

        //Basic configuration object
        CacheConfiguration cacheConfigToUse = getNewCacheConfiguration();

        //Create cache factory
        CacheFactory cacheFactory = new CacheFactory();
        cacheFactory.setCacheName(cacheName);
        cacheFactory.setCacheClientFactory(cacheClientFactory);
        cacheFactory.setAddressProvider(addressProvider);
        cacheFactory.setConfiguration(cacheConfigToUse);

        //Get Cache object
        Cache object = null;
        try {
            object = cacheFactory.getObject();
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        //allow/disallow remove all entries from this cache!!
        boolean allowClearFlag = false;
        SSMCache ssmCache = new SSMCache(object, expiryTimeInSeconds, allowClearFlag);

        return ssmCache;

    }

    private CacheConfiguration getNewCacheConfiguration() {
        CacheConfiguration ssmCacheConfiguration = new CacheConfiguration();
        ssmCacheConfiguration.setConsistentHashing(true);
        //ssmCacheConfiguration.setUseBinaryProtocol(true);
        return ssmCacheConfiguration;
    }
}
