package br.edu.ifba.saj.fwads.bus;

import java.util.HashSet;
import java.util.Set;

public class EventBus {
    private static EventBus instance;
    private Set<EventListener> listeners;
    
    private EventBus() {
        listeners = new HashSet<>();
    }
    
    public static EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }
    
    public void subscribe(EventListener listener) {
        listeners.add(listener);
    }
    
    public void unsubscribe(EventListener listener) {
        listeners.remove(listener);
    }
    
    public void publish(Event event) {
        for (EventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
    
    public interface EventListener {
        void onEvent(Event event);
    }
    
    public static class Event {
        private final String type;
        private final Object data;
        
        public Event(String type, Object data) {
            this.type = type;
            this.data = data;
        }
        
        public String getType() {
            return type;
        }
        
        public Object getData() {
            return data;
        }
    }
}