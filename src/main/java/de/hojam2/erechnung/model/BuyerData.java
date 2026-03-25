package de.hojam2.erechnung.model;

import jakarta.validation.constraints.NotBlank;

public class BuyerData {

    @NotBlank(message = "Empfängername ist erforderlich.")
    private String name;

    @NotBlank(message = "Straße und Hausnummer des Empfängers sind erforderlich.")
    private String street;

    @NotBlank(message = "PLZ des Empfängers ist erforderlich.")
    private String zip;

    @NotBlank(message = "Ort des Empfängers ist erforderlich.")
    private String city;

    private String vatId;

    private String buyerReference;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getVatId() {
        return vatId;
    }

    public void setVatId(String vatId) {
        this.vatId = vatId;
    }

    public String getBuyerReference() {
        return buyerReference;
    }

    public void setBuyerReference(String buyerReference) {
        this.buyerReference = buyerReference;
    }
}
