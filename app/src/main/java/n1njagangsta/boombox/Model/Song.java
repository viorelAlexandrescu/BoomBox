package n1njagangsta.boombox.Model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by viorel on 16.08.2016.
 *
 * This class describes a Song :)
 */
public class Song implements Parcelable{
    private String artist, title, album;
    private int duration;
    private Uri songUri;

    public Song(String newTitle, String newArtist, String newAlbum, int newDuration){
        this.title = newTitle;
        this.artist = newArtist;
        this.album = newAlbum;
        this.duration = newDuration;
    }

    public Song(String newTitle, String newArtist, String newAlbum, int newDuration, Uri newSongUri){
        this.title = newTitle;
        this.artist = newArtist;
        this.album = newAlbum;
        this.duration = newDuration;
        this.songUri = newSongUri;
    }

    public static final Parcelable.Creator<Song> CREATOR
            = new Parcelable.Creator<Song>() {
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    private Song(Parcel sourceParcel) {
        // restoring song data when reading from parcel in this order
        this.title = sourceParcel.readString();
        this.artist = sourceParcel.readString();
        this.album = sourceParcel.readString();
        this.duration = sourceParcel.readInt();
        this.songUri = Uri.parse(sourceParcel.readString());
    }

    @Override
    public int describeContents() {
        /* there is no predefined integer value for
            describing the contents of this object if
            it does not contain a file descriptor
        */
        return 0;
    }

    @Override
    public void writeToParcel(Parcel destParcel, int flags) {
        // when writing data to a parcel, we place the data in this order
        destParcel.writeString(this.title);
        destParcel.writeString(this.artist);
        destParcel.writeString(this.album);
        destParcel.writeInt(this.duration);
        destParcel.writeString(this.songUri.toString());
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

    public int getDuration() {
        return this.duration;
    }

    public Uri getUri(){
        return this.songUri;
    }

    public void setUri(String url){
        this.songUri = Uri.parse(url);
    }

    public void setUri(Uri newUri){
        this.songUri = newUri;
    }

    public static String getTimeInMinutesAndSeconds(int duration){
        if(duration == 0) return "0:00";
        else {
            int minutes = (duration/1000)/60,
                    seconds = (duration/1000)%60;
            if(seconds >= 10){
                return minutes + ":" + seconds;
            } else {
                //double digit seconds
                return minutes + ":" + "0" + seconds;
            }
        }
    }
}
