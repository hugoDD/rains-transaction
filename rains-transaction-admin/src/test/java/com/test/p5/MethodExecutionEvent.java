package com.test.p5;

import java.util.EventObject;

/**
 * @author hugosz
 * @version [2018年03月27日  9:17]
 * @since V1.00
 */
public class MethodExecutionEvent extends EventObject {
    private static final long serialVersionUID = -71960369269303337L;
    private String methodName;

    public MethodExecutionEvent(Object source) {
        super(source);
    }

    public MethodExecutionEvent(Object source, String methodName) {
        super(source);
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
