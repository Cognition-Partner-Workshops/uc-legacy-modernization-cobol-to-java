package com.suitecrm.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(DomainEvent event) {
        log.info("Publishing domain event: type={}, entity={}, entityId={}",
                event.getEventType(), event.getEntityType(), event.getEntityId());
        applicationEventPublisher.publishEvent(event);
    }
}
