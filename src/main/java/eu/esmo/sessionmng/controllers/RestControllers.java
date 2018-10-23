/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.esmo.sessionmng.builders.MngrSessionBuilder;
import eu.esmo.sessionmng.model.TO.MngrSessionTO;
import eu.esmo.sessionmng.model.service.SessionService;
import io.swagger.annotations.ApiOperation;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author nikos
 */
@RestController
public class RestControllers {

    private final static Logger LOG = LoggerFactory.getLogger(RestControllers.class);
    
    
    @Autowired
    private SessionService sessionServ;

    @RequestMapping(value = "/startSession", method = RequestMethod.POST)
    @ApiOperation(value = "Sets up an internal session temporary storage and returns its identifier", response = String.class)
    public @ResponseBody
    String startSession() {
        UUID sessionId = UUID.randomUUID();
        sessionServ.makeNewSession(sessionId.toString());
        LOG.debug("created new session with Id:: " + sessionId.toString());
        return sessionId.toString();
    }

    @RequestMapping(value = "/endSession", method = RequestMethod.DELETE)
    @ApiOperation(value = "Terminates a session and deletes all the stored data")
    public void endSession(@RequestParam String sessionId) {
        LOG.debug("Asked to delete session:: " + sessionId);
        sessionServ.delete(sessionId);
    }

    @RequestMapping(value = "/updateSessionData", method = RequestMethod.POST)
    @ApiOperation(value = "Passed data is stored in a session variable overwriting the previous value")
    public void updateSessionData(@RequestParam String sessionId, @RequestParam String variableName, @RequestParam String dataObject) {

        try {
            LOG.debug("Attempting to update variable " + variableName + " of session  " + sessionId + " with value  " + dataObject);
            sessionServ.updateSessionVariable(sessionId, variableName, dataObject);
        } catch (ChangeSetPersister.NotFoundException ex) {
            LOG.error("failed to update variable " + variableName + " NOT Found", ex.getMessage());
        }

    }

    @RequestMapping(value = "/getSessionData", method = RequestMethod.GET)
    @ApiOperation(value = "A variable Or the whole session object  is retrieved")
    public @ResponseBody
    MngrSessionTO getSessionData(@RequestParam String sessionId, @RequestParam(required = false) String variableName) {

        LOG.debug("requested from sessionId:: " + sessionId.toString() + " and for variable  " + variableName);

        if (StringUtils.isEmpty(variableName)) {
            return sessionServ.findBySessionId(sessionId);
        } else {
            return MngrSessionBuilder.buildMngrSessionFromVariable(sessionId, variableName, sessionServ.getValueByVariableAndId(sessionId, variableName));
        }
    }

    @RequestMapping(value = "/generateToken", method = RequestMethod.GET)
    @ApiOperation(value = "Generates a signed token, containing the session ID and the data on the payload.")
    public String generateToken(@RequestParam String sessionId, @RequestParam(required = false) String data) {

        return "jwt token";
    }

    @RequestMapping(value = "/validateToken", method = RequestMethod.GET)
    @ApiOperation(value = "The passed security token’s signature will be validated, as well as the validity as well as other validation measures")
    public String validateToken(@RequestParam String token) {

        return "jwt payload";
    }

}
