package dev.xfj.events.application;

import dev.xfj.events.Event;

import java.util.EnumSet;

public class WindowResizeEvent extends Event {
    private static final EventType eventType = EventType.WindowResize;
    private final int width;
    private final int height;


    public WindowResizeEvent(int width, int height) {
        super(EnumSet.of(Event.EventCategory.EventCategoryApplication));
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String toString() {
        return String.format("WindowResizeEvent: %d, %d", width, height);
    }

    public static Event.EventType getStaticType() {
        return eventType;
    }

    @Override
    public Event.EventType getEventType() {
        return getStaticType();
    }
}