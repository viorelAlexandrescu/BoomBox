package n1njagangsta.boombox.Model;

import java.util.ArrayList;

/**
 * Created by viorel on 28.09.2016.
 * This class shall describe an artist object and all it's contents
 */

public class Artist {
    private String artistName;
    private ArrayList<Album> albumList;

    public Artist(String newArtistName, ArrayList<Album> newAlbums) {
        this.artistName = newArtistName;
        this.albumList = newAlbums;
    }

    public String getArtistName() {
        return this.artistName;
    }

    public ArrayList<Album> getAlbumList() {
        return this.albumList;
    }
}
