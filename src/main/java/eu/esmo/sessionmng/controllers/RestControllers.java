/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.controllers;

import eu.esmo.sessionmng.builders.MngrSessionFactory;
import eu.esmo.sessionmng.model.TO.MngrSessionTO;
import eu.esmo.sessionmng.model.service.JwtService;
import eu.esmo.sessionmng.model.service.ParameterService;
import eu.esmo.sessionmng.model.service.SessionService;
import eu.esmo.sessionmng.pojo.ResponseCode;
import eu.esmo.sessionmng.pojo.SessionMngrResponse;
import io.swagger.annotations.ApiOperation;
import java.util.HashMap;
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

    @Autowired
    private JwtService jwtServ;

    @Autowired
    private ParameterService paramServ;

    @RequestMapping(value = "/startSession", method = RequestMethod.POST)
    @ApiOperation(value = "Sets up an internal session temporary storage and returns its identifier", response = String.class)
    public @ResponseBody
    SessionMngrResponse startSession() {
        UUID sessionId = UUID.randomUUID();
        sessionServ.makeNewSession(sessionId.toString());
        LOG.debug("created new session with Id:: " + sessionId.toString());
        return new SessionMngrResponse(ResponseCode.NEW, new MngrSessionTO(sessionId.toString(), new HashMap()), null, null);
    }

    @RequestMapping(value = "/endSession", method = RequestMethod.DELETE)
    @ApiOperation(value = "Terminates a session and deletes all the stored data")
    public SessionMngrResponse endSession(@RequestParam String sessionId) {
        LOG.debug("Asked to delete session:: " + sessionId);
        sessionServ.delete(sessionId);
        return new SessionMngrResponse(ResponseCode.OK, null, null, null);
    }

    @RequestMapping(value = "/updateSessionData", method = RequestMethod.POST)
    @ApiOperation(value = "Passed data is stored in a session variable overwriting the previous value")
    public SessionMngrResponse updateSessionData(@RequestParam String sessionId, @RequestParam String variableName, @RequestParam String dataObject) {
        try {
            LOG.debug("Attempting to update variable " + variableName + " of session  " + sessionId + " with value  " + dataObject);
            sessionServ.updateSessionVariable(sessionId, variableName, dataObject);
            return new SessionMngrResponse(ResponseCode.OK, null, null, null);
        } catch (ChangeSetPersister.NotFoundException ex) {
            LOG.error("failed to update variable " + variableName + " NOT Found", ex.getMessage());
            return new SessionMngrResponse(ResponseCode.ERROR, null, null, "failed to update variable " + variableName + " NOT Found");
        }
    }

    @RequestMapping(value = "/getSessionData", method = RequestMethod.GET)
    @ApiOperation(value = "A variable Or the whole session object  is retrieved")
    public @ResponseBody
    SessionMngrResponse getSessionData(@RequestParam String sessionId, @RequestParam(required = false) String variableName) {

        LOG.debug("requested from sessionId:: " + sessionId + " and for variable  " + variableName);
        if (StringUtils.isEmpty(variableName)) {
            return new SessionMngrResponse(ResponseCode.OK, sessionServ.findBySessionId(sessionId), null, null);
        } else {
            return new SessionMngrResponse(ResponseCode.OK, MngrSessionFactory.makeMngrSessionTOFromVariableAndSessionId(sessionId, variableName, sessionServ.getValueByVariableAndId(sessionId, variableName)),
                    null, null);
        }
    }

    @RequestMapping(value = "/generateToken", method = RequestMethod.GET)
    @ApiOperation(value = "Generates a signed token, only the sessionId as the payload, additionaly parameters include:"
            + " The id of the requesting microservice (msA) and The id of the destination microservice (msB), may also include additional data")
    public SessionMngrResponse generateToken(@RequestParam String sessionId, @RequestParam(required = true) String sender,
            @RequestParam(required = true) String receiver, @RequestParam(required = false) String data) {
//        MngrSessionTO payload = sessionServ.findBySessionId(sessionId);
        try {
            if (sessionServ.findBySessionId(sessionId) == null) {
                throw new ChangeSetPersister.NotFoundException();
            }
            String jwt = jwtServ.makeJwt(sessionId, data, paramServ.getProperty("ISSUER"), sender, receiver, Long.valueOf(paramServ.getProperty("EXPIRES")));
            return new SessionMngrResponse(ResponseCode.NEW, null, jwt, null);
        } catch (ChangeSetPersister.NotFoundException e) {
            LOG.error(e.getMessage());
            return new SessionMngrResponse(ResponseCode.ERROR, null, null, "sessionId not found");
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return new SessionMngrResponse(ResponseCode.ERROR, null, null, "error making jwt");
        }
    }

    @RequestMapping(value = "/validateToken", method = RequestMethod.GET)
    @ApiOperation(value = "The passed security token’s signature will be validated, as well as the validity as well as other validation measures")
    public SessionMngrResponse validateToken(@RequestParam String token) {
        //TODO check sender from config manager mciroservice
        return jwtServ.validateJwt(token);
    }

}
