package n1njagangsta.boombox;

import android.content.ContentUris;
import android.net.Uri;

/**
 * Created by viorel on 16.08.2016.
 */
public class Item {
    private long id, duration;
    private String artist, title, album;

    public Item(long newId, String newArtist, String newTitle, String newAlbum, long newDuration){
        this.id = newId;
        this.artist = newArtist;
        this.album = newAlbum;
        this.title = newTitle;
        this.duration = newDuration;
    }

    public long getId() {
        return id;
    }

    public long getDuration() {
        return duration;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public Uri getURI() {
        return ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
    }
}
