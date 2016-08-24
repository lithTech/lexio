package lt.ru.lexio.ui;

import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by lithTech on 21.08.2016.
 */

public class EventListenerManager {

    public static final String EVENT_TYPE_RESUME = "RESUME";

    private Map<String, Map<String, GeneralCallback>> listenerMap = new HashMap<>();

    public boolean registered(String type) {
        return listenerMap.get(type) != null && !listenerMap.get(type).isEmpty();
    }

    public void register(String type, String id, GeneralCallback listener) {
        Map<String, GeneralCallback> events = listenerMap.get(type);
        if (events == null) {
            events = new HashMap<String, GeneralCallback>();
            listenerMap.put(type, events);
        }
        events.put(id, listener);
    }

    public void unregister(String type, String id) {
        Map<String, GeneralCallback> events = listenerMap.get(type);
        if (events != null) {
            events.remove(id);
        }
    }

    public void raise(String type, Object data) {
        Map<String, GeneralCallback> events = listenerMap.get(type);
        if (events != null) {
            for (GeneralCallback eventListener : events.values()) {
                eventListener.done(data);
            }
        }
    }
}
