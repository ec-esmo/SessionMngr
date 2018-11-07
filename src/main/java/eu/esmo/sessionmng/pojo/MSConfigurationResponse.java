/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esmo.sessionmng.pojo;

/**
 *
 * @author nikos
 */
public class MSConfigurationResponse {

    private MicroService[] ms;

    public MSConfigurationResponse() {
    }

    public MSConfigurationResponse(MicroService[] ms) {
        this.ms = ms;
    }

    public MicroService[] getMs() {
        return ms;
    }

    public void setMs(MicroService[] ms) {
        this.ms = ms;
    }

    //static is needed for jackson
    public static class MicroService {

        private String msID;
        private String msType;
        private String rsaPublicKeyBinary;
        private MsAPI[] publishedAPI;

        public MicroService(String msID, String msType, String rsaPublicKeyBinary, MsAPI[] publishedAPI) {
            this.msID = msID;
            this.msType = msType;
            this.rsaPublicKeyBinary = rsaPublicKeyBinary;
            this.publishedAPI = publishedAPI;
        }

        public MicroService() {
        }

        public String getMsID() {
            return msID;
        }

        public void setMsID(String msID) {
            this.msID = msID;
        }

        public String getMsType() {
            return msType;
        }

        public void setMsType(String msType) {
            this.msType = msType;
        }

        public String getRsaPublicKeyBinary() {
            return rsaPublicKeyBinary;
        }

        public void setRsaPublicKeyBinary(String rsaPublicKeyBinary) {
            this.rsaPublicKeyBinary = rsaPublicKeyBinary;
        }

        public MsAPI[] getPublishedAPI() {
            return publishedAPI;
        }

        public void setPublishedAPI(MsAPI[] publishedAPI) {
            this.publishedAPI = publishedAPI;
        }

    }

    //static is needed for jackson
    public static class MsAPI {

        private String apiClass;
        private String apiCall;
        private String apiConnectionType;
        private String url;

        public MsAPI() {
        }

        public MsAPI(String apiClass, String apiCall, String apiConnectionType, String url) {
            this.apiClass = apiClass;
            this.apiCall = apiCall;
            this.apiConnectionType = apiConnectionType;
            this.url = url;
        }

        public String getApiClass() {
            return apiClass;
        }

        public void setApiClass(String apiClass) {
            this.apiClass = apiClass;
        }

        public String getApiCall() {
            return apiCall;
        }

        public void setApiCall(String apiCall) {
            this.apiCall = apiCall;
        }

        public String getApiConnectionType() {
            return apiConnectionType;
        }

        public void setApiConnectionType(String apiConnectionType) {
            this.apiConnectionType = apiConnectionType;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

    }

}
