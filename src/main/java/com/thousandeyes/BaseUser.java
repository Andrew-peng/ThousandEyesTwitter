package com.thousandeyes;

import java.util.List;

public class BaseUser {
    private int id;
    private String name;
    private List<String> followers;
    private List<String> following;
    private List<String> messages;
    
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public List<String> getFollowers() {
        return followers;
    }
    public List<String> getFollowing() {
        return following;
    }
    public List<String> getMessages() {
        return messages;
    }

    public void setId(int _id) {
        this.id = _id;
    }
    public void setName(String n) {
        this.name = n;
    }
    public void setFollowers(List<String> arr) {
        this.followers = arr;
    }
    public void setFollowing(List<String> arr) {
        this.following = arr;
    }
    public void setMessages(List<String> arr) {
        this.messages = arr;
    }
   
}
