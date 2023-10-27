package dev.xfj.events.mouse;

import dev.xfj.events.Event;

public class MouseButtonPressedEvent extends MouseButtonEvent {
    private static final Event.EventType eventType = EventType.MouseButtonPressed;

    public MouseButtonPressedEvent(int button) {
        super(button);

    }
    public String toString() {
        return String.format("MouseButtonPressedEvent: %1$d", getMouseButton());
    }

    public static Event.EventType getStaticType() {
        return eventType;
    }

    @Override
    public Event.EventType getEventType() {
        return getStaticType();
    }
}
