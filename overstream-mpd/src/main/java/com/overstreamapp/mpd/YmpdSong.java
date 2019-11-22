package com.overstreamapp.mpd;

public class YmpdSong {

    private final int pos;
    private final String title;
    private final String artist;
    private final String album;

    public YmpdSong(int pos, String title, String artist, String album) {
        this.pos = pos;
        this.title = title;
        this.artist = artist;
        this.album = album;
    }

    public int getPosition() {
        return pos;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }
}
