package com.test.p4;

import org.assertj.core.util.introspection.PropertySupport;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.format.annotation.DateTimeFormat;


import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

/**
 * @author hugosz
 * @version [2018年03月26日  10:45]
 * @since V1.00
 */
public class  DatePropertyEditor extends PropertyEditorSupport{

    private String datePattern="yyyy-MM-dd";

    /**
     * Sets the property value by parsing a given String.  May raise
     * java.lang.IllegalArgumentException if either the String is
     * badly formatted or if this kind of property can't be expressed
     * as text.
     *
     * @param text The string to be parsed.
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        AbstractXmlApplicationContext container = new ClassPathXmlApplicationContext("...");
        container.registerShutdownHook();
        DateFormat dateTimeFormatter = new SimpleDateFormat(getDatePattern());
        try {
         Date date=   dateTimeFormatter.parse(text);
         setValue(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public String getDatePattern() {

        return datePattern;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }


}