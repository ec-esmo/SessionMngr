/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.service.impl;

import eu.esmo.sessionmng.service.HttpSignatureService;
import eu.esmo.sessionmng.service.NetworkService;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.httpclient.NameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author nikos
 */
@Service
public class NetworkServiceImpl implements NetworkService {

    private HttpSignatureService sigServ;
    private final static Logger LOG = LoggerFactory.getLogger(NetworkServiceImpl.class);

    @Autowired
    public NetworkServiceImpl(HttpSignatureService sigServ) {
        this.sigServ = sigServ;
    }

    @Override
    public String sendPostForm(String hostUrl, String uri, List<NameValuePair> urlParameters) throws IOException, NoSuchAlgorithmException {

        Map<String, String> map = new HashMap();
        MultiValueMap<String, String> multiMap = new LinkedMultiValueMap<>();

        urlParameters.stream().forEach(nameVal -> {
            map.put(nameVal.getName(), nameVal.getValue());
            multiMap.add(nameVal.getName(), nameVal.getValue());
        });

        String requestId = UUID.randomUUID().toString();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        try {
            headers.add("authorization", sigServ.generateSignature(hostUrl, "POST", uri, map, "application/x-www-form-urlencoded", requestId));
            Date date = new Date();
            byte[] digestBytes;
            //only when the request is json encoded are the post params added to the body of the request
            // else they eventually become encoded to the url
            digestBytes = MessageDigest.getInstance("SHA-256").digest("".getBytes());
            addHeaders(headers, hostUrl, date, digestBytes, "POST", uri, requestId);

        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            LOG.error("could not generate signature!!");
            LOG.error(e.getMessage());
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(multiMap, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                hostUrl + uri, request, String.class);

        assert (response.getStatusCode().equals(HttpStatus.CREATED) || response.getStatusCode().equals(HttpStatus.OK));
        return response.getBody();
    }

    @Override
    public String sendGet(String hostUrl, String uri, List<NameValuePair> urlParameters) throws IOException, NoSuchAlgorithmException {

        String requestId = UUID.randomUUID().toString();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(hostUrl + uri);
        Map<String, String> map = new HashMap();
        urlParameters.stream().forEach(nameVal -> {
            map.put(nameVal.getName(), nameVal.getValue());
            builder.queryParam(nameVal.getName(), nameVal.getValue());
        });
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        try {
            headers.add("authorization", sigServ.generateSignature(hostUrl, "GET", uri, map, "application/x-www-form-urlencoded", requestId));
            Date date = new Date();
            byte[] digestBytes;
            //only when the request is json encoded are the post params added to the body of the request
            // else they eventually become encoded to the url
            digestBytes = MessageDigest.getInstance("SHA-256").digest("".getBytes());
            addHeaders(headers, hostUrl, date, digestBytes, "GET", uri, requestId);

        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            LOG.error("could not generate signature!!");
            LOG.error(e.getMessage());
        }

        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(), HttpMethod.GET, entity, String.class);
        return response.getBody();
    }

    private void addHeaders(HttpHeaders headers, String host, Date date, byte[] digestBytes, String method, String uri, String requestId) throws NoSuchAlgorithmException {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z");
        String nowDate = formatter.format(date);
        //only when the request is json encoded are the post params added to the body of the request
        // else they eventually become encoded to the url
        digestBytes = MessageDigest.getInstance("SHA-256").digest("".getBytes());
        String digest = "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digestBytes));
        String requestTarget = method + " " + uri;

        headers.add("host", host);
        headers.add("(request-target)", requestTarget);
        headers.add("original-date", nowDate);
        headers.add("digest", digest);
        headers.add("x-request-id", requestId);

    }

}
