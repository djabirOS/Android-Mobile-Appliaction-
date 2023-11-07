package com.ostaxi.app.Model;

public class CardModel {

    String brand, id, name;
    int expMonth, expYear, lastDigits;
    Boolean defaultCard = false;


    public CardModel(String brand, String id, String name, int expMonth, int expYear, int lastDigits, Boolean defaultCard) {
        this.brand = brand;
        this.id = id;
        this.name = name;
        this.expMonth = expMonth;
        this.expYear = expYear;
        this.lastDigits = lastDigits;
        this.defaultCard = defaultCard;
    }

    public CardModel(String id){
        this.id = id;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setExpMonth(int expMonth) {
        this.expMonth = expMonth;
    }

    public void setExpYear(int expYear) {
        this.expYear = expYear;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLastDigits(int lastDigits) {
        this.lastDigits = lastDigits;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDefaultCard(Boolean defaultCard) {
        this.defaultCard = defaultCard;
    }

    public int getExpMonth() {
        return expMonth;
    }

    public int getExpYear() {
        return expYear;
    }

    public int getLastDigits() {
        return lastDigits;
    }

    public String getBrand() {
        return brand;
    }

    public String getName() {
        return name;
    }

    public Boolean getDefaultCard() {
        return defaultCard;
    }

    public String getId() {
        return id;
    }
}
