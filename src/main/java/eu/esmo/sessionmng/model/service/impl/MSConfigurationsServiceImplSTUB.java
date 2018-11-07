/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.model.service.impl;

import eu.esmo.sessionmng.builders.MSConfigurationResponseFactory;
import eu.esmo.sessionmng.model.service.MSConfigurationService;
import eu.esmo.sessionmng.pojo.MSConfigurationResponse;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author nikos
 */
@Service
public class MSConfigurationsServiceImplSTUB implements MSConfigurationService {

    private final static Logger log = LoggerFactory.getLogger(MSConfigurationsServiceImplSTUB.class);

    @Override
    public MSConfigurationResponse getConfigurationJSON() {

        try {
            return MSConfigurationResponseFactory.makeMSConfigResponseFromJSON(getFile("configurationResponse.json"));
        } catch (IOException e) {
            log.error("file not found ", e);
            return null;
        }

    }

    private String getFile(String fileName) {
        StringBuilder result = new StringBuilder("");
        //Get file from resources folder
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();

    }

}
