package n1njagangsta.boombox.Model;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.util.ArrayList;

/**
 * Created by viorel on 19.09.2016.
 * This class represents an album
 */

public class Album {
    private String title, artist;
    private ArrayList<Song> albumSongList;
    private Bitmap albumArt;

    public Album(String newTitle, String newArtist, ArrayList<Song> newAlbumSongList){
        this.title = newTitle;
        this.artist = newArtist;;
        this.albumSongList = newAlbumSongList;
    }

    public Album(String newTitle, String newArtist, ArrayList<Song> newAlbumSongList, Bitmap newAlbumArt){
        this.title = newTitle;
        this.artist = newArtist;;
        this.albumSongList = newAlbumSongList;
        this.albumArt = newAlbumArt;
    }

    public String getAlbumTitle(){
        return this.title;
    }

    public String getArtistName(){
        return this.artist;
    }

    public ArrayList<Song> getAlbumSongList(){
        return this.albumSongList;
    }

    public void setAlbumArt(Bitmap newAlbumArt){
        this.albumArt = newAlbumArt;
    }

    public Bitmap getAlbumArt(){
        return albumArt;
    }
}
