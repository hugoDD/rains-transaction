package com.test.p4;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.validation.DataBinder;

import java.beans.PropertyEditor;
import java.util.Date;

/**
 * @author hugosz
 * @version [2018年03月26日  10:57]
 * @since V1.00
 */
public class DatePropertyEditorRegistrar implements PropertyEditorRegistrar {
    public PropertyEditor getPropertyEditor() {
        return propertyEditor;
    }

    public void setPropertyEditor(PropertyEditor propertyEditor) {
        this.propertyEditor = propertyEditor;
    }

    private PropertyEditor propertyEditor;


    @Override
    public void registerCustomEditors(PropertyEditorRegistry registry) {
        registry.registerCustomEditor(Date.class,getPropertyEditor());

    }
}
