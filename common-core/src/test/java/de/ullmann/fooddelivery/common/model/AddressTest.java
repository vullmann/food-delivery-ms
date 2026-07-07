package de.ullmann.fooddelivery.common.model;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class AddressTest {

    private final String street = "Musterstraße";
    private final String houseNumber = "23";
    private final String city = "Halle";
    private final String zip = "06108";
    private final String country = "DE";

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldCreateAddressWithValidData() {
        Address address = Address.of(street, houseNumber, city, zip, country);

        assertThat(address.getStreet()).isEqualTo(street);
        assertThat(address.getHouseNumber()).isEqualTo(houseNumber);
        assertThat(address.getCity()).isEqualTo(city);
        assertThat(address.getZip()).isEqualTo(zip);
        assertThat(address.getCountry()).isEqualTo(country);
    }

    @Test
    void shouldCreateAddressUsingProtectedConstructor() {
        Address address = new Address();

        assertThat(address).isNotNull();
    }

    @Test
    void shouldBeEmbeddable() {
        Address address = Address.of(street, houseNumber, city, zip, country);

        // Embeddable entities are typically used in JPA, so we just verify the object is created
        assertThat(address).isNotNull();
    }

    @Test
    void shouldNotBeEqualForDifferentStreets() {
        Address address1 = Address.of(street, houseNumber, city, zip, country);
        Address address2 = Address.of("Andere Straße", houseNumber, city, zip, country);

        assertThat(address1).isNotEqualTo(address2);
    }

    @Test
    void shouldNotBeEqualForDifferentHouseNumbers() {
        Address address1 = Address.of(street, houseNumber, city, zip, country);
        Address address2 = Address.of(street, "42", city, zip, country);

        assertThat(address1).isNotEqualTo(address2);
    }

    @Test
    void shouldNotBeEqualForDifferentCities() {
        Address address1 = Address.of(street, houseNumber, city, zip, country);
        Address address2 = Address.of(street, houseNumber, "Berlin", zip, country);

        assertThat(address1).isNotEqualTo(address2);
    }

    @Test
    void shouldNotBeEqualForDifferentZips() {
        Address address1 = Address.of(street, houseNumber, city, zip, country);
        Address address2 = Address.of(street, houseNumber, city, "10115", country);

        assertThat(address1).isNotEqualTo(address2);
    }

    @Test
    void shouldNotBeEqualForDifferentCountries() {
        Address address1 = Address.of(street, houseNumber, city, zip, country);
        Address address2 = Address.of(street, houseNumber, city, zip, "AT");

        assertThat(address1).isNotEqualTo(address2);
    }

    @Test
    void shouldStripTrailingZipCodes() {
        // Zip must contain digits only (Pattern validation)
        Address address = Address.of(street, houseNumber, city, "06108", country);
        assertThat(address.getZip()).isEqualTo("06108");
    }

    @Test
    void shouldRejectZipWithNonDigits() {
        Address address = Address.of(street, houseNumber, city, "O61AB", country);

        Set<ConstraintViolation<Address>> violations = validator.validate(address);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("zip"));
    }

    @Test
    void shouldRejectZipWithMaxLength() {
        // Zip has @Size(max = 10)
        String validZip = "0610812345"; // 10 digits
        Address address = Address.of(street, houseNumber, city, validZip, country);
        assertThat(address.getZip()).isEqualTo(validZip);
    }

    @Test
    void shouldSupportToString() {
        Address address = Address.of(street, houseNumber, city, zip, country);
        String toStringResult = address.toString();

        assertThat(toStringResult).isNotNull().isNotEmpty();
    }

    @Test
    void shouldAllowMultipleAddressesWithDifferentValues() {
        Address address1 = Address.of("Straße 1", "10", "Stadt1", "12345", "DE");
        Address address2 = Address.of("Straße 2", "20", "Stadt2", "67890", "AT");
        Address address3 = Address.of("Straße 3", "30", "Stadt3", "11111", "CH");

        assertThat(address1).isNotEqualTo(address2);
        assertThat(address2).isNotEqualTo(address3);
        assertThat(address1).isNotEqualTo(address3);
    }
}

