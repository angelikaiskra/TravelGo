package com.heroes.hack.travelgo.objects;

import android.content.SharedPreferences;

public class User {
    /*This class represent user.
    * It is used when fetching users data from API*/
    private String username;
    private int level;
    private int experience;
    private int leftExperience;

    public User(String username, int level, int experience, int leftExperience) {
        this.username = username;
        this.level = level;
        this.experience = experience;
        this.leftExperience = leftExperience;
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

    public void saveUsersData(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("level", level);
        editor.putInt("experience", experience);
        editor.putInt("leftExperience", leftExperience);
        editor.apply();
    }
}
