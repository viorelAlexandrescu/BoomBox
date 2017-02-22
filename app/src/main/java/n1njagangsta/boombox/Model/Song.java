package n1njagangsta.boombox.Model;

import android.net.Uri;

/**
 * Created by viorel on 16.08.2016.
 *
 * This class describes a Song :)
 */
public class Song {
    private String artist, title, album;
    private Uri uri;
    private long duration;

    public Song(String newTitle, String newArtist, String newAlbum, long newDuration){
        this.title = newTitle;
        this.artist = newArtist;
        this.album = newAlbum;
        this.duration = newDuration;
    }

    public String getSongTitle() {
        return this.title;
    }

    public String getArtistName() {
        return this.artist;
    }

    public String getAlbumName() {
        return this.album;
    }

    public long getDuration() {
        return this.duration;
    }

    public Uri getURI() {
        return this.uri;
    }

    public void setURI(Uri newUri){
        this.uri = newUri;
    }

    public static String getTimeInMinutesAndSeconds(int songDuration){
        if(songDuration == 0) return "0:00";
        else {
            int minutes = (songDuration/1000)/60,
                 seconds = (songDuration/1000)%60;
            if(seconds >= 10){
                return minutes + ":" + seconds;
            } else {
                String doubleDigitSeconds = "0" + seconds;

                return minutes + ":" + doubleDigitSeconds;
            }
        }
    }
}
