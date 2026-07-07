package de.ullmann.fooddelivery.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    void values_containsAllRoles() {
        assertThat(Role.values()).containsExactlyInAnyOrder(
                Role.SUPER_ADMIN,
                Role.RESTAURANT_ADMIN,
                Role.RESTAURANT_EMPLOYEE,
                Role.DELIVERY_ADMIN,
                Role.DELIVERY_DRIVER,
                Role.CUSTOMER);
    }

    @Test
    void valueOf_returnsCorrectRole() {
        assertThat(Role.valueOf("CUSTOMER")).isEqualTo(Role.CUSTOMER);
        assertThat(Role.valueOf("SUPER_ADMIN")).isEqualTo(Role.SUPER_ADMIN);
        assertThat(Role.valueOf("RESTAURANT_ADMIN")).isEqualTo(Role.RESTAURANT_ADMIN);
        assertThat(Role.valueOf("RESTAURANT_EMPLOYEE")).isEqualTo(Role.RESTAURANT_EMPLOYEE);
        assertThat(Role.valueOf("DELIVERY_ADMIN")).isEqualTo(Role.DELIVERY_ADMIN);
        assertThat(Role.valueOf("DELIVERY_DRIVER")).isEqualTo(Role.DELIVERY_DRIVER);
    }
}
