package org.scada_lts.web.mvc.api.json;

import java.io.Serializable;

public class JsonSettingsHttp implements Serializable {

    private boolean useProxy;
    private int port;
    private String host;
    private String username;
    private String password;

    public JsonSettingsHttp() {}

    public JsonSettingsHttp(boolean useProxy, int port, String host, String username, String password) {
        this.useProxy = useProxy;
        this.port = port;
        this.host = host;
        this.username = username;
        this.password = password;
    }

    public boolean isUseProxy() {
        return useProxy;
    }

    public void setUseProxy(boolean useProxy) {
        this.useProxy = useProxy;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
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
