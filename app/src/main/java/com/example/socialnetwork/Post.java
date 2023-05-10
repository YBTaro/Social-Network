package com.example.socialnetwork;

public class Post {
    private String date, uid, time, description, fullname, profile_image, post_image;

    public Post(){

    }

    public Post(String date, String uid, String time, String description, String fullname, String profile_image, String post_image) {
        this.date = date;
        this.uid = uid;
        this.time = time;
        this.description = description;
        this.fullname = fullname;
        this.profile_image = profile_image;
        this.post_image = post_image;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getPost_image() {
        return post_image;
    }

    public void setPost_image(String post_image) {
        this.post_image = post_image;
    }
}
