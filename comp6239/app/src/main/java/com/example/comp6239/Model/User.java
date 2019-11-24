package com.example.comp6239.Model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class User {
    public String id;
    private String username;
    private String name;
    private String surname;
    private String type;
    private String imageurl;
    private String approved;

    public User() {}
    public User(String id, String email, String name, String surname, String type,String approved,String imageurl) {
        this.id = id;
        this.username = email;
        this.name = name;
        this.surname = surname;
        this.type = type;
        this.imageurl = imageurl;
        this.approved=approved;
    }

    public String getId() { return id; }
    public String getUsername() {
        return username;
    }
    public String getName() {
        return name;
    }
    public String getSurname() {return surname;}
    public String getType() {return type;}
    public String getImageURL () {return imageurl;}
    public String getApproved() {return approved;}
    public void setId(String id) {this.id = id;}
    public void setUsername(String username) {this.username = username;}
    public void setName(String name) {this.name = name;}
    public void setSurname(String surname) {this.surname = surname;}
    public void setimageURL (String imageurl) {this.imageurl = imageurl;}
    public void setType(String type) {this.type = type;}
    public void setApproved(String approved){this.approved=approved;}

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("username", username);
        result.put("name",name);
        result.put("imageurl",imageurl);
        result.put("type",type);
        result.put("surname",surname);
        result.put("approved",approved);
        return result;
    }
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                "username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", imageurl='" + imageurl + '\'' +
                ", type='" + type + '\'' +
                ",approved="+approved+'}';
    }
}
