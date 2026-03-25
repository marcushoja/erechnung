package de.hojam2.erechnung.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public class SellerData {

    @NotBlank(message = "Absendername ist erforderlich.")
    private String name;

    @NotBlank(message = "Straße und Hausnummer des Absenders sind erforderlich.")
    private String street;

    @NotBlank(message = "PLZ des Absenders ist erforderlich.")
    private String zip;

    @NotBlank(message = "Ort des Absenders ist erforderlich.")
    private String city;

    @NotBlank(message = "Steuernummer des Absenders ist erforderlich.")
    private String taxNumber;

    private String vatId;

    @NotBlank(message = "IBAN ist erforderlich.")
    private String iban;

    private String bic;

    @Email(message = "E-Mail-Adresse ist ungültig.")
    private String email;

    private String phone;

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

    public String getTaxNumber() {
        return taxNumber;
    }

    public void setTaxNumber(String taxNumber) {
        this.taxNumber = taxNumber;
    }

    public String getVatId() {
        return vatId;
    }

    public void setVatId(String vatId) {
        this.vatId = vatId;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
