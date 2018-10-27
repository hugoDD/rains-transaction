package com.test.p4;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @author hugosz
 * @version [2018年03月26日  14:17]
 * @since V1.00
 */
public class PasswordDecodePostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof  PasswordDecodable){
            String encodedPassword = ((PasswordDecodable)bean).getEncodedPassword();
            String decodedPassword = decodePassword(encodedPassword);
            ((PasswordDecodable)bean).setDecodedPassword(decodedPassword);
        }
        return bean;
    }

    private String decodePassword(String encodedPassword) { // 实现解码逻辑 3
        return encodedPassword;
    }


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
