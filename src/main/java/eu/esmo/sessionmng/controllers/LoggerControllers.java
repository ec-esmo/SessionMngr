/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.controllers;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author nikos
 */
@RestController
public class LoggerControllers {

    private final static Logger log = LoggerFactory.getLogger(LoggerControllers.class);

    @RequestMapping(value = "/loglevel/{loglevel}", method = RequestMethod.POST)
    @ApiOperation(value = "Changes the log level of the logger for the given package", response = String.class)
    public String loglevel(@PathVariable("loglevel") String logLevel, @RequestParam(value = "package", required=false) String packageName) throws Exception {
        if(StringUtils.isEmpty(packageName)){
            packageName ="eu.esmo.sessionmng";
        }
        log.info("Log level: " + logLevel);
        log.info("Package name: " + packageName);
        String retVal = setLogLevel(logLevel, packageName);
        return retVal;
    }

    public String setLogLevel(String logLevel, String packageName) {
        String retVal;
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        if (logLevel.equalsIgnoreCase("DEBUG")) {
            loggerContext.getLogger(packageName).setLevel(Level.DEBUG);
            retVal = "ok";
        } else if (logLevel.equalsIgnoreCase("INFO")) {
            loggerContext.getLogger(packageName).setLevel(Level.INFO);
            retVal = "ok";
        } else if (logLevel.equalsIgnoreCase("TRACE")) {
            loggerContext.getLogger(packageName).setLevel(Level.TRACE);
            retVal = "ok";
        } else {
            log.error("Not a known loglevel: " + logLevel);
            retVal = "Error, not a known loglevel: " + logLevel;
        }
        return retVal;
    }

}
