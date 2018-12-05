/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.service.impl;

import eu.esmo.sessionmng.service.ParameterService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 *
 * @author nikos
 */
@Service
public class ParameterServiceImpl implements ParameterService {

    @Override
    public String getProperty(String propertyName) {
        String res = System.getenv(propertyName);
        if(!StringUtils.isEmpty(res)){
            return res;
        }
        return "errorValue";
    }
    
}
