/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.controllers;

import eu.esmo.sessionmng.factory.MngrSessionFactory;
import eu.esmo.sessionmng.factory.SessionMngrResponseFactory;
import eu.esmo.sessionmng.model.TO.MngrSessionTO;
import eu.esmo.sessionmng.service.BlackListService;
import eu.esmo.sessionmng.service.JwtService;
import eu.esmo.sessionmng.service.ParameterService;
import eu.esmo.sessionmng.service.SessionService;
import eu.esmo.sessionmng.pojo.JwtValidationResponse;
import eu.esmo.sessionmng.enums.ResponseCode;
import eu.esmo.sessionmng.pojo.SessionMngrResponse;
import eu.esmo.sessionmng.service.MSConfigurationService;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.tomitribe.auth.signatures.Signature;

/**
 *
 * @author nikos
 */
@RestController
@RequestMapping("sm")
public class RestControllers {

    private final static Logger LOG = LoggerFactory.getLogger(RestControllers.class);

    @Autowired
    private SessionService sessionServ;

    @Autowired
    private JwtService jwtServ;

    @Autowired
    private ParameterService paramServ;

    @Autowired
    private BlackListService blacklistServ;

    @Autowired
    private MSConfigurationService configServ;

    @RequestMapping(value = "/startSession", method = RequestMethod.POST, produces = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    @ApiOperation(value = "Sets up an internal session temporary storage and returns its identifier", response = SessionMngrResponse.class, code = 200)
    public @ResponseBody
    SessionMngrResponse startSession() {
        UUID sessionId = UUID.randomUUID();
        sessionServ.makeNewSession(sessionId.toString());
        LOG.debug("created new session with Id:: " + sessionId.toString());
        return new SessionMngrResponse(ResponseCode.NEW, new MngrSessionTO(sessionId.toString(), new HashMap()), null, null);
    }

    @RequestMapping(value = "/endSession", method = RequestMethod.DELETE, produces = "application/json")
    @ApiOperation(value = "Terminates a session and deletes all the stored data")
    public SessionMngrResponse endSession(@RequestParam String sessionId) {
        LOG.debug("Asked to delete session:: " + sessionId);
        sessionServ.delete(sessionId);
        return new SessionMngrResponse(ResponseCode.OK, null, null, null);
    }

    @RequestMapping(value = "/updateSessionData", method = RequestMethod.POST, produces = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    @ApiOperation(value = "Passed data is stored in a session variable overwriting the previous value. If no session variable is given, "
            + "then the whole data stored in this session will be replaced with the passed dataObject, under the default variable name data", response = SessionMngrResponse.class)
    public SessionMngrResponse updateSessionData(@RequestParam String sessionId, @RequestParam(required = false) String variableName, @RequestParam String dataObject) {
        try {
            if (StringUtils.isEmpty(variableName)) {
                LOG.debug("Attempting to update the whole session  " + sessionId + " with value  " + dataObject);
                sessionServ.replaceSession(sessionId, "data", dataObject);
            } else {
                LOG.debug("Attempting to update variable " + variableName + " of session  " + sessionId + " with value  " + dataObject);
                sessionServ.updateSessionVariable(sessionId, variableName, dataObject);
            }

            return new SessionMngrResponse(ResponseCode.OK, null, null, null);
        } catch (ChangeSetPersister.NotFoundException ex) {
            LOG.error("failed to update variable " + variableName + " NOT Found", ex.getMessage());
            return new SessionMngrResponse(ResponseCode.ERROR, null, null, "failed to update variable " + variableName + " NOT Found");
        }
    }

    @RequestMapping(value = "/getSessionData", method = RequestMethod.GET, produces = "application/json")
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

    @RequestMapping(value = "/generateToken", method = RequestMethod.GET, produces = "application/json")
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

    @RequestMapping(value = "/validateToken", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "The passed security token’s signature will be validated, as well as the validity as well as other validation measures")
    public SessionMngrResponse validateToken(@RequestParam String token, HttpServletRequest req) {
        SessionMngrResponse response = new SessionMngrResponse();
        response.setCode(ResponseCode.ERROR);
        try {
            String authorization = req.getHeader("authorization");
            if (authorization != null) {
                Signature sigToVerify = Signature.fromString(authorization);
                String fingerprint = sigToVerify.getKeyId();
                Optional<String> requestSenderId = configServ.getMsIDfromRSAFingerprint(fingerprint);
                if (requestSenderId.isPresent()) {
                    JwtValidationResponse valResp = jwtServ.validateJwt(token);
                    if (valResp.getCode().equals(ResponseCode.OK)) {
                        blacklistServ.addToBlacklist(valResp.getJti());
                    }
                    if (!valResp.getReceiver().equals(requestSenderId.get())) {
                        valResp.setError("sender id token missmatch!");
                    }
                    return SessionMngrResponseFactory.makeSessionMngrResponseFromValidationResponse(valResp);
                }
                response.setError("sender id is missing!");
            }
            response.setError("authorization header is missing!");
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
            response.setError("error getting sender public key!");
            return response;
        }
        return response;

    }

    @RequestMapping(value = "/getSession", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "Returns the internal session identifier by querying using the UUID of an exteranal "
            + "the session request. E.g. eIDAS request identifier, The identifier must be previously stored in the session")
    public SessionMngrResponse getSessionFromIdPUUUID(String varName, String varValue) {
        SessionMngrResponse response = new SessionMngrResponse();
        try {
            Optional<String> sessionId = sessionServ.getSessionIdByVariableAndValue(varName, varValue);

            if (sessionId.isPresent() && !StringUtils.isEmpty(sessionId.get())) {
                response.setCode(ResponseCode.OK);
                response.setSessionData(MngrSessionFactory.makeMngrSessionTOFromSessionId(sessionId.get()));
            } else {
                response.setCode(ResponseCode.ERROR);
                response.setError("No sessios found");
            }
        } catch (ArithmeticException e) {
            response.setCode(ResponseCode.ERROR);
            response.setError("More than one sessions match criteria!");
            LOG.debug(e.getMessage());
        }
        return response;
    }

}
