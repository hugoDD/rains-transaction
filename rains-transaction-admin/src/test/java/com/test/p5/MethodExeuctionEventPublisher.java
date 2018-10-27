package com.test.p5;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hugosz
 * @version [2018年03月27日  9:21]
 * @since V1.00
 */
public class MethodExeuctionEventPublisher {

    private List<MethodExecutionEventListener> listeners = new ArrayList<MethodExecutionEventListener>();

    public void methodToMonitor()
    {
        MethodExecutionEvent event2Publish = new MethodExecutionEvent(this,"methodToMonitor");
        publishEvent(MethodExecutionStatus.BEGIN,event2Publish);

    }

    protected void publishEvent(MethodExecutionStatus status, MethodExecutionEvent methodExecutionEvent) {
        List<MethodExecutionEventListener> copyListeners = new ArrayList<>(listeners);
        for(MethodExecutionEventListener listener:copyListeners)
        {
            if(MethodExecutionStatus.BEGIN.equals(status))
                listener.onMethodBegin(methodExecutionEvent);
            else
                listener.onMethodEnd(methodExecutionEvent);
        }
    }

    public void addMethodExecutionEventListener(MethodExecutionEventListener listener)
    {
        this.listeners.add(listener);
    }
    public void removeListener(MethodExecutionEventListener listener)
    {
        if(this.listeners.contains(listener))
            this.listeners.remove(listener);
    }
    public void removeAllListeners()
    {
        this.listeners.clear();
    }
    public static void main(String[] args) {
        MethodExeuctionEventPublisher eventPublisher =  new MethodExeuctionEventPublisher();
        eventPublisher.addMethodExecutionEventListener(new SimpleMethodExecutionEventListener());
        eventPublisher.methodToMonitor();
    }

}
