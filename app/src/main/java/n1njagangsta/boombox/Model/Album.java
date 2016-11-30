package n1njagangsta.boombox.Model;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by viorel on 19.09.2016.
 * This class represents an album
 */

public class Album {
    private String title, artist;
    private long albumId;
    private List<Song> albumSongList;
    private Bitmap albumArt;

    public Album(String newArtist, String newTitle, long newAlbumId,
                 List<Song> newAlbumSongList, Bitmap newAlbumArt){
        this.title = newTitle;
        this.artist = newArtist;
        this.albumId = newAlbumId;
        this.albumSongList = newAlbumSongList;
        this.albumArt = newAlbumArt;
    }

    public String getTitle(){
        return title;
    }

    public String getArtist(){
        return artist;
    }

    public long getAlbumId(){
        return albumId;
    }

    public List<Song> getAlbumSongList(){
        return albumSongList;
    }

    @Nullable
    public Bitmap getAlbumArt(){
        return albumArt;
    }
}
