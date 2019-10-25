/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.service;

/**
 *
 * @author nikos
 */
public interface BlackListService {
    

    public boolean isBlacklisted(String jti) throws NullPointerException;
    public void addToBlacklist(String jti) throws NullPointerException;


}
