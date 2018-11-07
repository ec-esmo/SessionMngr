/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.service;

import eu.esmo.sessionmng.model.service.MSConfigurationService;
import eu.esmo.sessionmng.model.service.impl.MSConfigurationsServiceImplSTUB;
import eu.esmo.sessionmng.pojo.MSConfigurationResponse;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author nikos
 */
public class TestMSConfigurationService {

    private MSConfigurationService stubServ;

    @Before
    public void before() {
        this.stubServ = new MSConfigurationsServiceImplSTUB();
    }

    @Test
    public void testReadConfnigJSON() {
        MSConfigurationResponse resp = stubServ.getConfigurationJSON();
        assertEquals(resp.getMs()[0].getMsID(),"SAMLms001");
        assertEquals(resp.getMs()[0].getMsType(),"SP_AP_IDP");
        assertEquals(resp.getMs()[0].getPublishedAPI()[0].getApiClass(),"AP");

    }

}
