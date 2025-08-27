package com.cloudeagle;

import com.cloudeagle.schema.response.AccessTokenResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DropboxClient {

    private final String CLIENT_ID;
    private final String CLIENT_SECRET;
    private final String redirectUrl;
    private final String scope;
    private String authorizationCode;
    private String authToken;
    private String refreshToken;

    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DropboxClient(Properties props) {
        this.CLIENT_ID = props.getProperty("dropbox.clientId");
        this.CLIENT_SECRET = props.getProperty("dropbox.clientSecret");
        this.redirectUrl = props.getProperty("dropbox.redirectUrl");
        this.scope = props.getProperty("dropbox.scope");
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = getObjectMapper();
    }

    public String getAuthorizationUrl() throws Exception {
        StringBuilder authorizationUrl = new StringBuilder();
        authorizationUrl.append("https://www.dropbox.com/oauth2/authorize")
                        .append("?client_id=").append(CLIENT_ID)
                        .append("&redirect_uri=").append(encodedString(redirectUrl))
                        .append("&scope=").append(encodedString(scope))
                        .append("&response_type=code")
                        .append("&token_access_type=offline");
        return authorizationUrl.toString();
    }

    public void fetchAccessToken() throws Exception {
        try {
            HttpPost httpPostRequest = new HttpPost("https://api.dropboxapi.com/oauth2/token");
            httpPostRequest.setEntity(new UrlEncodedFormEntity(getAccessTokenParams()));
            HttpResponse response = executeRequest(httpPostRequest, /*includeAuthHeader*/ false);
            AccessTokenResponse accessTokenResponse = objectMapper.readValue(EntityUtils.toString(response.getEntity()), AccessTokenResponse.class);
            setAuthToken(accessTokenResponse.accessToken);
            setRefreshToken(accessTokenResponse.refreshToken);
        } catch (Exception e) {
            System.out.println("Failed to fetch access token");
            e.printStackTrace();
            throw e;
        }
    }

    public String getTeamInfo() throws Exception {
        try {
            HttpPost httpPostRequest = new HttpPost("https://api.dropboxapi.com/2/team/get_info");
            HttpResponse response = executeRequest(httpPostRequest, /*includeAuthHeader*/ true);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(EntityUtils.toString(response.getEntity())));
        } catch (Exception e) {
            System.out.println("Failed to get team info");
            e.printStackTrace();
            throw e;
        }
    }

    private HttpResponse executeRequest(HttpPost request, boolean includeAuthHeader) throws IOException {
        if (includeAuthHeader) {
            addAuthHeader(request);
        }
        HttpResponse response = httpClient.execute(request);
        if (!isRequestSuccessful(response.getStatusLine().getStatusCode())) {
            throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
        }
        return response;
    }

    private boolean isRequestSuccessful(int statusCode) {
        return statusCode / 100 == 2;
    }

    private void addAuthHeader(HttpPost request) {
        request.addHeader("Authorization", "Bearer " + authToken);
    }

    private List<NameValuePair> getAccessTokenParams() {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("code", authorizationCode));
        params.add(new BasicNameValuePair("grant_type", "authorization_code"));
        params.add(new BasicNameValuePair("client_id", CLIENT_ID));
        params.add(new BasicNameValuePair("client_secret", CLIENT_SECRET));
        params.add(new BasicNameValuePair("redirect_uri", redirectUrl));
        return params;
    }

    private ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    private void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    private void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    private String encodedString(String text) throws UnsupportedEncodingException {
        return URLEncoder.encode(text, "UTF-8");
    }

}
