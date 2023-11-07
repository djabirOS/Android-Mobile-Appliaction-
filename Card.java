package com.ostaxi.app.Adapters;

public class Card {


    String card,cvv,expire,name;


    public Card() {
    }

    public Card(String card, String cvv, String expire, String name) {
        this.card = card;
        this.cvv = cvv;
        this.expire = expire;
        this.name = name;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getExpire() {
        return expire;
    }

    public void setExpire(String expire) {
        this.expire = expire;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
