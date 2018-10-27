package com.test.p4;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.beans.factory.support.MethodReplacer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.reflect.Method;

/**
 * @author hugosz
 * @version [2018年03月26日  9:30]
 * @since V1.00
 */
public class FXNewsProviderMethodReplacer implements MethodReplacer {
    private static final Logger LOGGER = LoggerFactory.getLogger(FXNewsProviderMethodReplacer.class);

    /**
     * Reimplement the given method.
     *
     * @param target    the instance we're reimplementing the method for
     * @param method the method to reimplement
     * @param args   arguments to the method
     * @return return value for the method
     */
    @Override
    public Object reimplement(Object target, Method method, Object[] args) throws Throwable {
        LOGGER.info("before executing method["+method.getName()+"] on Object["+target.getClass().getName()+"].");
        System.out.println("sorry,We will do nothing this time.");
        LOGGER.info("end of executing method["+method.getName()+ "] on Object["+target.getClass().getName()+"].");
        return null;
    }
}
