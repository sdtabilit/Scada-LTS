package org.scada_lts.web.mvc.api.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;

public class JsonSettingsEmail implements Serializable {

    private boolean auth;
    private boolean tls;
    private int contentType;
    private int port;
    private String from;
    private String host;
    private String name;
    private String username;
    private String password;

    public JsonSettingsEmail() {}

    public JsonSettingsEmail(boolean auth, boolean tls, int contentType, int port, String from, String host, String name, String username, String password) {
        this.auth = auth;
        this.tls = tls;
        this.contentType = contentType;
        this.port = port;
        this.from = from;
        this.host = host;
        this.name = name;
        this.username = username;
        this.password = password;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public boolean isTls() {
        return tls;
    }

    public void setTls(boolean tls) {
        this.tls = tls;
    }

    public int getContentType() {
        return contentType;
    }

    public void setContentType(int contentType) {
        this.contentType = contentType;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
