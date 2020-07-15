/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.parsers;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.esmo.sessionmng.pojo.DataStoreObject;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author nikos
 */
public class TestNewDataStoreObject {

    @Test
    public void testParsing() throws IOException {
        String input = "{\"id\":\"019293f04893b0298392a0484\",\"type\":\"dataSet\",\"data\":\" {\\\"this is\\\": \\\" a dataSet object\\\"}\"}";
        ObjectMapper mapper = new ObjectMapper();
        DataStoreObject res = mapper.readValue(input, DataStoreObject.class);
        assertEquals("019293f04893b0298392a0484", res.getId());
        assertEquals("dataSet", res.getType().toString());

//        DataStoreObject object = new DataStoreObject();
//        object.setId("019293f04893b0298392a0484");
//        object.setType("dataSet");
//        object.setData(" {\"this is\": \" a dataSet object\"}");
//        System.out.println(mapper.writeValueAsString(object));
//        assertEquals("true", "true");
    }

}
