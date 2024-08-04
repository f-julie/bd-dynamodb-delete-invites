package com.amazon.ata.dynamodbdeleteiterators.classroom.dao;

import com.amazon.ata.dynamodbdeleteiterators.classroom.dao.models.Event;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.KeyPair;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;


/**
 * Manages access to Event items.
 */
public class EventDao {
    private DynamoDBMapper mapper;

    /**
     * Creates an EventDao with the given DDB mapper.
     * @param mapper DynamoDBMapper
     */
    @Inject
    public EventDao(DynamoDBMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Gets an event by ID.
     * @param eventId The ID of the event to look up
     * @return the Event
     */
    public Event getEvent(String eventId) {
        return mapper.load(Event.class, eventId);
    }

    /**
     * Gets a collection of events for a Collection of event IDs.
     * No guarantee of the order/size of result is provided (e.g. in
     * case some of the IDs are not found).
     * @param eventIds The (Collection of) IDs to fetch events for
     * @return List of Events (those found from the IDs provided)
     */
    public List<Event> getEvents(Collection<String> eventIds) {
        Map<Class<?>, List<KeyPair>> classAndKeys = new HashMap<>();

        List<KeyPair> keysList = eventIds.stream()
            .distinct()
            .map(eventId -> new KeyPair().withHashKey(eventId))
            .collect(Collectors.toList());
        classAndKeys.put(Event.class, keysList);
        List<Object> results = mapper.batchLoad(classAndKeys).values().iterator().next();

        // DynamoDB guarantees that we can cast the Objects that come
        // back to the class mapped provided above
        return results.stream()
            .map(object -> (Event) object)
            .collect(Collectors.toList());
    }

    /**
     * Creates a new event.
     * @param event The event to create
     * @return the created event
     */
    public Event createEvent(Event event) {
        if (Objects.isNull(event.getId())) {
            event.setId(UUID.randomUUID().toString());
        }
        mapper.save(event);
        return event;
    }

    /**
     * Cancels an existing event by ID.
     * @param eventId The event ID of the event to cancel
     * @return the updated state of the event
     */
    public Event cancelEvent(String eventId) {
        // PARTICIPANTS: replace this implementation to perform a soft delete
        Event event = mapper.load(Event.class, eventId);
        event.setCanceled(true);
        mapper.save(event);
        return event;
    }
}
