package de.ullmann.fooddelivery.notificationservice.projection;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import de.ullmann.fooddelivery.notificationservice.entity.CustomerPhoneProjection;
import de.ullmann.fooddelivery.notificationservice.repository.CustomerPhoneProjectionRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomerPhoneStore {

    private static final Logger log = LoggerFactory.getLogger(CustomerPhoneStore.class);

    private final CustomerPhoneProjectionRepository repository;

    @Transactional
    public void upsert(UUID customerId, String phone) {
        repository.findById(customerId).ifPresentOrElse(
                projection -> projection.updatePhone(phone),
                () -> repository.save(CustomerPhoneProjection.of(customerId, phone))
        );
    }

    @Transactional(readOnly = true)
    public Optional<String> find(UUID customerId) {
        return repository.findById(customerId).map(CustomerPhoneProjection::getPhone);
    }

    public String getOrWarn(UUID customerId) {
        return find(customerId).orElseGet(() -> {
            log.warn("No phone found in projection for customerId={}", customerId);
            return null;
        });
    }
}
