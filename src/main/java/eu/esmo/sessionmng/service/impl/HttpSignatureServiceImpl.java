/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.service.impl;

import eu.esmo.sessionmng.enums.HttpResponseEnum;
import eu.esmo.sessionmng.service.HttpSignatureService;
import eu.esmo.sessionmng.service.KeyStoreService;
import eu.esmo.sessionmng.service.MSConfigurationService;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tomitribe.auth.signatures.Algorithm;
import org.tomitribe.auth.signatures.Signature;
import org.tomitribe.auth.signatures.Signer;
import org.tomitribe.auth.signatures.Verifier;

/**
 *
 * @author nikos
 */
@Service
public class HttpSignatureServiceImpl implements HttpSignatureService {

    private Algorithm algorithm = Algorithm.RSA_SHA256;
    private Signer signer;
    private final static Logger log = LoggerFactory.getLogger(HttpSignatureServiceImpl.class);

    public static String[] requiredHeaders = {"(request-target)", "host", "original-date", "digest", "x-request-id"};
    private static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
    private static final int DATE_DIFF_ALLOWED = 5;

    private KeyStoreService keyServ;
    private MSConfigurationService msConfigServ;

    @Autowired
    public HttpSignatureServiceImpl(KeyStoreService keyServ, MSConfigurationService msConfigServ)
            throws InvalidKeySpecException, IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        try {
            this.msConfigServ = msConfigServ;
            this.keyServ = keyServ;
            String keyId = DigestUtils.sha256Hex(getX509PubKeytoRSABinaryFormat((PublicKey) this.keyServ.getHttpSigPublicKey()));
            this.signer = new Signer(keyServ.getSigningKey(), new Signature(keyId, algorithm, null, "(request-target)", "host", "original-date", "digest", "x-request-id"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getFakeSignature() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, UnsupportedEncodingException, IOException {

//         String[] requiredHeaders = {"(request-target)", "host", "original-date", "digest", "x-request-id"};
        final String method = "GET";
        final String uri = "/foo/Bar";
        final Map<String, String> headers = new HashMap<String, String>();
        headers.put("host", "example.org");
        headers.put("original-date", "Tue, 07 Jun 2014 20:51:35 GMT");
        headers.put("Content-Type", "application/json");
        headers.put("digest", "SHA-256=X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=");
        headers.put("Accept", "*/*");
        headers.put("Content-Length", "18");
        headers.put("x-request-id", UUID.randomUUID().toString());

        // Here it is!
        final Signature signed = getSigner().sign(method, uri, headers);
        return signed.toString();
    }

    public String getX509PubKeytoRSABinaryFormat(PublicKey key) throws IOException, KeyStoreException {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public String generateSignature(String hostUrl, String method, String uri, Map<String, String> postParams, String contentType, String requestId)
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, UnsupportedEncodingException, IOException {

        final String[] requiredHeaders = {"(request-target)", "host", "original-date", "digest", "x-request-id"};
        final Map<String, String> signatureHeaders = new HashMap<String, String>();
        signatureHeaders.put("host", hostUrl);
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z");
        String nowDate = formatter.format(date);
        signatureHeaders.put("original-date", nowDate);

        //multipart/form-data
        //application/x-www-form-urlencoded
        //application/json
        signatureHeaders.put("Content-Type", contentType);

        byte[] digest;
        //only when the request is json encoded are the post params added to the body of the request
        // else they eventually become encoded to the url
        if (postParams != null && contentType.equals("application/json")) {
            digest = MessageDigest.getInstance("SHA-256").digest(getParamsString(postParams).getBytes());
        } else {
            digest = MessageDigest.getInstance("SHA-256").digest("".getBytes());
        }

        signatureHeaders.put("digest", "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digest)));

        signatureHeaders.put("Accept", "*/*");
        signatureHeaders.put("Content-Length", Integer.toString(digest.length));
        signatureHeaders.put("x-request-id", requestId);
        signatureHeaders.put("(request-target)", method + " " + uri);

        Algorithm algorithm = Algorithm.RSA_SHA256;
//        String keyId = "06f336b68ba82890576f92b7d564c709cea0c0f318a09b4fbc5a502a7c93f926";
        // Here it is!
//        Signer signer = new Signer(keyServ.getSigningKey(), new Signature(keyId, algorithm, null, "(request-target)", "host", "original-date", "digest", "x-request-id"));
        Signature signed = getSigner().sign(method, uri, signatureHeaders);

        return signed.toString();

    }

    @Override
    public HttpResponseEnum verifySignature(HttpServletRequest httpRequest) {
        String authorization = httpRequest.getHeader("authorization");
        if (authorization != null) {

            Signature sigToVerify = Signature.fromString(authorization);
            log.info("HTTP Signature received: " + sigToVerify);

            /* check request contains all mandatory headers
             */
            boolean emptyRequiredHeader
                    = sigToVerify.getHeaders()
                            .stream()
                            .anyMatch(headerName -> {
                                return StringUtils.isEmpty(httpRequest.getHeader(headerName));
                            });

            /* Verify that all the required headers are signed (i.e. are part of the http signature)
                and that all the signed headers are present in the request
             */
            if (!sigToVerify.getHeaders().containsAll(Arrays.asList(requiredHeaders))
                    || emptyRequiredHeader) {
                return HttpResponseEnum.HEADER_MISSING;
            }

            Map<String, String> headers = new HashMap<String, String>();
            Collections.list(httpRequest.getHeaderNames())
                    .stream().forEach(hName -> {
                        headers.put(hName, httpRequest.getHeader(hName));
                    });

            String clientTime = StringUtils.isEmpty(httpRequest.getHeader("date"))
                    ? httpRequest.getHeader("original-date") : httpRequest.getHeader("date");

            try {
                //TODO blacklist requestIds to remove replay attacks?

//                String requestId = UUID.fromString(httpRequest.getHeader("x-request-id")).toString();
                if (!hasValidRequestTime(clientTime)) {
                    return HttpResponseEnum.BAD_REQUEST;
                }
                byte[] requestBodyRaw = IOUtils.toByteArray(httpRequest.getInputStream());
                byte[] digest = MessageDigest.getInstance("SHA-256").digest(requestBodyRaw);
                String digestCalculated = new String(Base64.getEncoder().encodeToString(digest));

                if (!areDigestsEqual(httpRequest.getHeader("digest"), digestCalculated)) {
                    log.info("Digest missmatch");
                    return HttpResponseEnum.UN_AUTHORIZED;
                }

                String method = httpRequest.getMethod().toLowerCase();
                String uri = httpRequest.getRequestURI();
                if (!StringUtils.isEmpty(httpRequest.getQueryString())) {
                    uri += "?" + httpRequest.getQueryString();
                }
                log.debug("Veryfing signature for " + uri + " and verb " + method);

                if (isSignatureValid(sigToVerify, msConfigServ, method, uri, headers)) {
                    return HttpResponseEnum.AUTHORIZED;
                }

                return HttpResponseEnum.UN_AUTHORIZED;

            } catch (IllegalArgumentException e) {
                log.error("Wrong request ID");
                log.error(e.getMessage());
                return HttpResponseEnum.BAD_REQUEST;
            } catch (ParseException ex) {
                log.error("Error parsing date");
                log.error(ex.getMessage());
                return HttpResponseEnum.BAD_REQUEST;
            } catch (IOException ex) {
                log.error("ERROR getting request content");
                log.error(ex.getMessage());
                return HttpResponseEnum.BAD_REQUEST;
            } catch (NoSuchAlgorithmException ex) {
                log.error(ex.getMessage());
                return HttpResponseEnum.UN_AUTHORIZED;
            } catch (InvalidKeyException | InvalidKeySpecException | SignatureException ex) {
                log.error("Error verifying Signature");
                log.error(ex.getMessage());
                return HttpResponseEnum.UN_AUTHORIZED;
            }
        }
        return HttpResponseEnum.UN_AUTHORIZED;
    }

    public boolean hasValidRequestTime(String receivedTime) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        final Date timeServer = new Date();
        final Date timeClient = format.parse(receivedTime);
        long diff = Math.abs(timeClient.getTime() - timeServer.getTime());
        long diffMinutes = diff / (60 * 1000) % 60;
        return diffMinutes < DATE_DIFF_ALLOWED;
    }

    public boolean areDigestsEqual(String requestDigest, String calculatedDigest) {
        String reqDigestSha256;
        Pattern p = Pattern.compile("SHA-256=([^,$]+)");
        Matcher m = p.matcher(requestDigest);
        if (m.find()) {
            reqDigestSha256 = m.group(1);
            log.info("Extracted SHA-256 digest: " + reqDigestSha256);
        } else {
            return false;
        }
        return calculatedDigest.equals(reqDigestSha256);
    }

    public boolean isSignatureValid(Signature sigToVerify,
            MSConfigurationService msConfigServ, String method, String uri, Map<String, String> headers) throws InvalidKeyException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException {
        String fingerprint = sigToVerify.getKeyId();
        Optional<PublicKey> pubKey = msConfigServ.getPublicKeyFromFingerPrint(fingerprint);
        if (pubKey.isPresent()) {
            Verifier verifier = new Verifier(pubKey.get(), sigToVerify);
            return verifier.verify(method, uri, headers);
        }
        return false;
    }

    public static String getParamsString(Map<String, String> params)
            throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            result.append("&");
        }
        String resultString = result.toString();
        return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }

    private Signer getSigner() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, UnsupportedEncodingException, IOException {
        if (this.signer == null) {
            String keyId = DigestUtils.sha256Hex(getX509PubKeytoRSABinaryFormat((PublicKey) this.keyServ.getHttpSigPublicKey()));
            signer = new Signer(keyServ.getSigningKey(), new Signature(keyId, algorithm, null, "(request-target)", "host", "original-date", "digest", "x-request-id"));
        }
        return signer;
    }

}
