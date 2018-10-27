package com.rains.transaction.admin.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author dourx
 * 2018年 10 月  27日  15:00
 * @version V1.0
 * TODO
 */
@ConfigurationProperties(prefix = "tx", ignoreUnknownFields = true)
public class AdminTxProperties {
    private Admin admin;
    private Recover recover;

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public Recover getRecover() {
        return recover;
    }

    public void setRecover(Recover recover) {
        this.recover = recover;
    }

    public static class Admin {
        private String username="admin";
        private String password="admin";

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

    public static class Recover {
       private String  serializer= "kryo";
        private int retry=  10;
        private List<String> application;

        public String getSerializer() {
            return serializer;
        }

        public void setSerializer(String serializer) {
            this.serializer = serializer;
        }

        public int getRetry() {
            return retry;
        }

        public void setRetry(int retry) {
            this.retry = retry;
        }

        public List<String> getApplication() {
            return application;
        }

        public void setApplication(List<String> application) {
            this.application = application;
        }
    }
}
