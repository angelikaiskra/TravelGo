package com.heroes.hack.travelgo.objects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class User {
    /*This class represent user.
     * It is used when fetching users data from API*/
    private String username;
    private int level;
    private int experience;
    private int leftExperience;
    private List<Relic> relics;

    public User(String username) {
        this.username = username;
        this.level = 0;
        this.experience = 0;
        this.leftExperience = 0;
        this.relics = new ArrayList<>();
    }

    public User(String username, int level, int experience, int leftExperience, List<Relic> relics) {
        this.username = username;
        this.level = level;
        this.experience = experience;
        this.leftExperience = leftExperience;
        this.relics = new ArrayList<>(relics);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getLeftExperience() {
        return leftExperience;
    }

    public void setLeftExperience(int leftExperience) {
        this.leftExperience = leftExperience;
    }

    public List<Relic> getRelics() {
        return relics;
    }

    public void setRelics(List<Relic> relics) {
        this.relics = relics;
    }

    public Set<String> getRelicsIdSet() {
        Set<String> relicsIdSet = new HashSet<>();

        for (Relic relic : relics) {
            relicsIdSet.add(String.valueOf(relic.getId()));
        }

        return relicsIdSet;
    }
}
