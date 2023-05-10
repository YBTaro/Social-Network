package com.example.socialnetwork;

public class FindFriends {
    private String profile_img, fullname, status;
    boolean isExpanded = false; // 不是存在 FirebaseDatabase;

    public FindFriends() {
    }

    public FindFriends(String profile_img, String fullname, String status) {
        this.profile_img = profile_img;
        this.fullname = fullname;
        this.status = status;
    }

    public String getProfile_img() {
        return profile_img;
    }

    public void setProfile_img(String profile_img) {
        this.profile_img = profile_img;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getStatus() {
        return status;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
