/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.service.impl;

import eu.esmo.sessionmng.MemCacheConfig;
import eu.esmo.sessionmng.service.BlackListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 *
 * @author nikos
 */
@Service
public class BlackListServiceImpl implements BlackListService {

    private CacheManager cacheManager;

    @Autowired
    public BlackListServiceImpl(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public final static String BLACK_LIST_CACHE = MemCacheConfig.BLACKLIST;

    @Override
    public boolean isBlacklisted(String jti) throws NullPointerException {
        if (this.cacheManager.getCache(BLACK_LIST_CACHE) != null) {
            return this.cacheManager.getCache(BLACK_LIST_CACHE).get(jti) != null;
        } else {
            throw new NullPointerException("Cache " + BLACK_LIST_CACHE + " was  not found");

        }
    }

    @Override
    public void addToBlacklist(String jti) throws NullPointerException {
        if (this.cacheManager.getCache(BLACK_LIST_CACHE) != null) {
            this.cacheManager.getCache(BLACK_LIST_CACHE).put(jti, jti);
        } else {
            throw new NullPointerException("Cache " + BLACK_LIST_CACHE + " was  not found");
        }
    }

}
