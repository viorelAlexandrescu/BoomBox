package n1njagangsta.boombox;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by viorel on 16.08.2016.
 */
public class MusicRetriever {
    private ContentResolver contentResolver;
    private List<Item> allSongs;

    public MusicRetriever(ContentResolver newContentResolver) {
        contentResolver = newContentResolver;
        allSongs = new ArrayList<>();
    }

    public void prepare(){
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = contentResolver.query(uri, null,
                MediaStore.Audio.Media.IS_MUSIC + " = 1", null, null);


        if(cursor == null){
            System.err.println("Failed to retrieve music: cursor is null");
            return;
        }

        if(!cursor.moveToFirst()){
            System.err.println("Failed to get first item. Empty Cursor");
            return;
        }

        int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST),
                titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE),
                albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM),
                durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION),
                idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);

        do{
            allSongs.add(new Item(
                    cursor.getLong(idColumn),
                    cursor.getString(artistColumn),
                    cursor.getString(titleColumn),
                    cursor.getString(albumColumn),
                    cursor.getLong(durationColumn)
            ));
        } while (cursor.moveToNext());

        cursor.close();

        System.out.println("Music Retriever Ready!");
    }

    public ContentResolver getContentResolver(){
        return contentResolver;
    }

    public Item[] getSongsAsItems(){
       return allSongs.toArray(new Item[allSongs.size()]);
    }

    public String[] getSongsAsStringArray(){
        String[] songs = new String[allSongs.size()];
        for(int i = 0; i < songs.length; i++){
            songs[i] = allSongs.get(i).getArtist() + " - " + allSongs.get(i).getTitle();
        }
        return songs;
    }

}
