package de.ullmann.fooddelivery.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @NotBlank
    private String street;

    @NotBlank
    private String houseNumber;

    @NotBlank
    private String city;

    @NotBlank
    @Size(max = 10)
    @Pattern(regexp = "\\d+", message = "zip must contain digits only")
    private String zip;

    @NotBlank
    private String country;

    @JsonCreator
    public static Address of(
            @JsonProperty("street") String street,
            @JsonProperty("houseNumber") String houseNumber,
            @JsonProperty("city") String city,
            @JsonProperty("zip") String zip,
            @JsonProperty("country") String country) {
        var a = new Address();
        a.street = street;
        a.houseNumber = houseNumber;
        a.city = city;
        a.zip = zip;
        a.country = country;
        return a;
    }
}