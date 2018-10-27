package com.test.p4;

/**
 * @author hugosz
 * @version [2018年03月26日  14:16]
 * @since V1.00
 */
public interface PasswordDecodable {
    String getEncodedPassword();
    void setDecodedPassword(String password);
}
