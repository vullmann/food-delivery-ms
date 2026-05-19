package de.ullmann.fooddelivery.notificationservice.projection;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.ullmann.fooddelivery.notificationservice.entity.CustomerPhoneProjection;
import de.ullmann.fooddelivery.notificationservice.repository.CustomerPhoneProjectionRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerPhoneStoreTest {

    @Mock
    private CustomerPhoneProjectionRepository repository;

    @InjectMocks
    private CustomerPhoneStore store;

    @Test
    void upsert_whenExists_shouldUpdatePhone() {
        UUID customerId = UUID.randomUUID();
        CustomerPhoneProjection existing = CustomerPhoneProjection.of(customerId, "+49111111111");
        when(repository.findById(customerId)).thenReturn(Optional.of(existing));

        store.upsert(customerId, "+49999999999");

        assertThat(existing.getPhone()).isEqualTo("+49999999999");
        verify(repository, never()).save(any());
    }

    @Test
    void upsert_whenNotExists_shouldSaveNew() {
        UUID customerId = UUID.randomUUID();
        when(repository.findById(customerId)).thenReturn(Optional.empty());

        store.upsert(customerId, "+49123456789");

        ArgumentCaptor<CustomerPhoneProjection> captor = ArgumentCaptor.forClass(CustomerPhoneProjection.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getPhone()).isEqualTo("+49123456789");
    }

    @Test
    void find_whenExists_shouldReturnPhone() {
        UUID customerId = UUID.randomUUID();
        when(repository.findById(customerId))
                .thenReturn(Optional.of(CustomerPhoneProjection.of(customerId, "+49123")));

        Optional<String> result = store.find(customerId);

        assertThat(result).contains("+49123");
    }

    @Test
    void find_whenNotExists_shouldReturnEmpty() {
        UUID customerId = UUID.randomUUID();
        when(repository.findById(customerId)).thenReturn(Optional.empty());

        Optional<String> result = store.find(customerId);

        assertThat(result).isEmpty();
    }

    @Test
    void getOrWarn_whenExists_shouldReturnPhone() {
        UUID customerId = UUID.randomUUID();
        when(repository.findById(customerId))
                .thenReturn(Optional.of(CustomerPhoneProjection.of(customerId, "+49123")));

        String result = store.getOrWarn(customerId);

        assertThat(result).isEqualTo("+49123");
    }

    @Test
    void getOrWarn_whenNotExists_shouldReturnNull() {
        UUID customerId = UUID.randomUUID();
        when(repository.findById(customerId)).thenReturn(Optional.empty());

        String result = store.getOrWarn(customerId);

        assertThat(result).isNull();
    }
}
