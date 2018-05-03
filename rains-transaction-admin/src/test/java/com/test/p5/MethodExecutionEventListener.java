package com.test.p5;

import java.util.EventListener;

/**
 * @author hugosz
 * @version [2018年03月27日  9:19]
 * @since V1.00
 */
public interface MethodExecutionEventListener extends EventListener {

    /**
     * 处理方法开始执行的时候发布的MethodExecutionEvent事件 3
     */
    void onMethodBegin(MethodExecutionEvent evt);
    /** * 处理方法执行将结束时候发布的MethodExecutionEvent事件 4
     */
    void onMethodEnd(MethodExecutionEvent evt);
}
