package me.dkflab.coins.objects;

import java.util.UUID;



public class MongoPlayer {

    private String name;
    private int coins;
    public MongoPlayer(String name, int coins) {
        this.name = name;
        this.coins = coins;
    }

    public int getCoins() {
        return this.coins;
    }

    public String getName() {
        return this.name;
    }
}
