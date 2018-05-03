package com.test.p5;

/**
 * @author hugosz
 * @version [2018年03月27日  9:20]
 * @since V1.00
 */
public class SimpleMethodExecutionEventListener implements  MethodExecutionEventListener {
    /**
     * 处理方法开始执行的时候发布的MethodExecutionEvent事件 3
     *
     * @param evt
     */
    @Override
    public void onMethodBegin(MethodExecutionEvent evt) {
        String methodName = evt.getMethodName();
        System.out.println("start to execute the method["+methodName+"].");

    }

    /**
     * 处理方法执行将结束时候发布的MethodExecutionEvent事件 4
     *
     * @param evt
     */
    @Override
    public void onMethodEnd(MethodExecutionEvent evt) {
        String methodName = evt.getMethodName();
        System.out.println("finished to execute the method["+methodName+"].");
    }
}
