package n1njagangsta.boombox;

/**
 * Created by viorel on 19.09.2016.
 */
public class Album {
    private String title, artist;
    private long albumId;

    public Album(String newArtist, String newTitle, long newAlbumId){
        this.title = newTitle;
        this.artist = newArtist;
        this.albumId = newAlbumId;
    }

    public String getTitle(){
        return title;
    }

    public String getArtist(){
        return artist;
    }

    public long getAlbumId(){ return albumId;}
}
