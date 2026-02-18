package com.example.sound;

public class Song {
    public String title;
    public String audioUrl;      // URL or android.resource://
    public String thumbnail;     // URL or local drawable name
    public boolean isOnline;

    public Song(String title, String audioUrl, String thumbnail, boolean isOnline) {
        this.title = title;
        this.audioUrl = audioUrl;
        this.thumbnail = thumbnail;
        this.isOnline = isOnline;
    }
}
