/*
 * Copyright 2019 Bunjlabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.overstreamapp.ympd.state;

import java.util.Objects;

public class PlayerSong {
    private int position;
    private String title;
    private String artist;
    private String album;

    public PlayerSong() {
    }

    public PlayerSong(int position, String title, String artist, String album) {
        this.position = position;
        this.title = title;
        this.artist = artist;
        this.album = album;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerSong playerSong = (PlayerSong) o;
        return position == playerSong.position &&
                Objects.equals(title, playerSong.title) &&
                Objects.equals(artist, playerSong.artist) &&
                Objects.equals(album, playerSong.album);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, title, artist, album);
    }
}
