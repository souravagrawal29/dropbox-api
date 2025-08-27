package com.cloudeagle;

import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

public class DropboxApp {

    private final DropboxClient dropboxClient;

    private DropboxApp() {
        Properties props = loadProperties();
        dropboxClient = new DropboxClient(props);
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream properties = DropboxApp.class.getResourceAsStream("/dropbox.properties")) {
            props.load(properties);
        } catch (Exception e) {
            System.out.println("Failed to load properties");
            e.printStackTrace();
            System.exit(-1);
        }
        return props;
    }

    private void initiateAuthentication() throws Exception {
        System.out.println("1) Open the following URL in a browser and authorize the app:\n\n" + dropboxClient.getAuthorizationUrl() + "\n");
        System.out.println("2) After consent, copy the 'code' query parameter value and paste it here:");
        String authorizationCode = new Scanner(System.in).nextLine().trim();
        dropboxClient.setAuthorizationCode(authorizationCode);
        dropboxClient.fetchAccessToken();
    }

    private String getTeamInfo() throws Exception {
        return dropboxClient.getTeamInfo();
    }

    public static void main(String[] args) {
        try {
            DropboxApp dropboxApp = new DropboxApp();
            dropboxApp.initiateAuthentication();
            System.out.println("\nThe team info for the organization is as follows:\n");
            System.out.println(dropboxApp.getTeamInfo());
        } catch (Exception e) {
            System.out.println("Failed to get team info");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}