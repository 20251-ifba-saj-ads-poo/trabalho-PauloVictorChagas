package br.edu.ifba.saj.fwads.bus;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class EventBus {
    private static final EventBus instance = new EventBus(); 
    private final Set<EventListener> listeners;

    private EventBus() {
        listeners = new CopyOnWriteArraySet<>();
    }

    public static EventBus getInstance() {
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
            try {
                listener.onEvent(event);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
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
        public String getType() { return type; }
        public Object getData() { return data; }
    }
}
