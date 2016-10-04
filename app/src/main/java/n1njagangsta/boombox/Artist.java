package n1njagangsta.boombox;

import java.util.List;

/**
 * Created by viorel on 28.09.2016.
 * This class shall describe an artist object and all it's contents
 */

public class Artist {
    private String artistName;
    private List<Album> albumList;

    public Artist(String newArtistName, List<Album> newAlbums) {
        this.artistName = newArtistName;
        this.albumList = newAlbums;
    }

    public List<Album> getAlbumList() {
        return albumList;
    }

    public String getArtistName() {
        return artistName;
    }
}
