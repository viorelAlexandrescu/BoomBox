package n1njagangsta.boombox.Model;

import android.content.ContentUris;
import android.net.Uri;

/**
 * Created by viorel on 16.08.2016.
 *
 * This class describes a Song :)
 */
public class Song {
    private long songId;
    private String artist, title, album, albumKey;

    public Song(long newId, String newArtist, String newTitle, String newAlbum, String newAlbumKey){
        this.songId = newId;
        this.albumKey = newAlbumKey;
        this.artist = newArtist;
        this.album = newAlbum;
        this.title = newTitle;
    }

    public String getSongAlbumKey(){
        return albumKey;
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
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId);
    }

    public void setAlbumKey(String newAlbumKey){
        albumKey = newAlbumKey;
    }

    public static String getTimeInMinutesAndSeconds(int songDuration){
        if(songDuration == 0){
            return "0:00";
        } else {
            int minutes = (songDuration/1000)/60,
                 seconds = (songDuration/1000)%60;
            if(seconds >= 10){
                return minutes + ":" + seconds;
            }else {
                String doubleDigitSeconds = "0" + seconds;

                return minutes + ":" + doubleDigitSeconds;
            }


        }
    }
}
