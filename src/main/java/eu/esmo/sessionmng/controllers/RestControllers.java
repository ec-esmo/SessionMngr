/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.controllers;

import io.swagger.annotations.ApiOperation;
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

    @RequestMapping(value = "/startSession", method = RequestMethod.POST)
    @ApiOperation(value = "Sets up an internal session temporary storage and returns its identifier", response = String.class)
    public @ResponseBody
    String startSession() {
        return "sessionId";
    }

    @RequestMapping(value = "/endSession", method = RequestMethod.DELETE)
    @ApiOperation(value = "Terminates a session and deletes all the stored data")
    public void endSession(@RequestParam String sessionId) {

    }

    @RequestMapping(value = "/updateSessionData", method = RequestMethod.POST)
    @ApiOperation(value = "Passed data is stored in a session variable overwriting the previous value")
    public void updateSessionData(@RequestParam String sessionId, @RequestParam String variableName, @RequestParam String dataObject) {

    }

    @RequestMapping(value = "/getSessionData", method = RequestMethod.GET)
    @ApiOperation(value = "A variable Or the whole session object  is retrieved")
    public @ResponseBody
    String getSessionData(@RequestParam String sessionId, @RequestParam(required = false) String variableName) {
        return "dataObject";
    }

    @RequestMapping(value = "/generateToken", method = RequestMethod.GET)
    @ApiOperation(value = "Generates a signed token, containing the session ID and the data on the payload.")
    public String generateToken(@RequestParam String sessionId, @RequestParam(required=false) String data) {

        return "jwt token";
    }

    @RequestMapping(value = "/validateToken", method = RequestMethod.GET)
    @ApiOperation(value="The passed security token’s signature will be validated, as well as the validity as well as other validation measures")
    public String validateToken(@RequestParam String token) {

        return "jwt payload";
    }

}
