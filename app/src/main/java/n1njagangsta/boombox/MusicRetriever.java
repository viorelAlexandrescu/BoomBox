package n1njagangsta.boombox;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by viorel on 16.08.2016.
 * Found this example online, i think it was via android examples
 * idk lol
 */
public class MusicRetriever {
    private ContentResolver contentResolver;
    private List<Song> allSongs;
    private List<Song> tempSongList; //can be used for both album and artists selection
    private List<Album> albums;
    private boolean isViewingTempList = false;

    public MusicRetriever(ContentResolver newContentResolver) {
        contentResolver = newContentResolver;
        allSongs = new ArrayList<>();
        albums   = new ArrayList<>();
        tempSongList = new ArrayList<>();
    }

    public void prepare(){
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String selection = MediaStore.Audio.Media.IS_MUSIC + " = 1",
                projection[] = null;

        Cursor cursor = contentResolver.query(uri, projection, selection, null, null);

        if(cursor == null){
            System.err.println("Failed to retrieve songs: cursor is null");
            return;
        }

        if(!cursor.moveToFirst()){
            System.err.println("Failed to get first item. Empty Songs Cursor");
            return;
        }

        int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST),
                titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE),
                albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM),
                durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION),
                idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);

        do{
            allSongs.add(new Song(
                    cursor.getLong(idColumn),
                    cursor.getString(artistColumn),
                    cursor.getString(titleColumn),
                    cursor.getString(albumColumn),
                    cursor.getLong(durationColumn)
            ));
        } while (cursor.moveToNext());


        projection = new String[] {MediaStore.Audio.AlbumColumns.ALBUM,
                                            MediaStore.Audio.AlbumColumns.ARTIST,
                                            MediaStore.Audio.AlbumColumns.ALBUM_ID};
        selection = MediaStore.Audio.AlbumColumns.ALBUM +
                " IS NOT NULL) GROUP BY (" + MediaStore.Audio.AlbumColumns.ALBUM;

        cursor = contentResolver.query(uri, projection, selection, null, null);

        if(cursor == null){
            System.err.println("Failed to retrieve albums: cursor is null");
            return;
        }

        if(!cursor.moveToFirst()){
            System.err.println("Failed to get first item. Empty Album Cursor");
            return;
        }

        artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ARTIST);
        albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ALBUM);
        int albumId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ID);

        do{
            albums.add(new Album(
                    cursor.getString(artistColumn),
                    cursor.getString(albumColumn),
                    cursor.getLong(albumId)));
        } while (cursor.moveToNext());


        cursor.close();
        System.out.println("Music Retriever Ready!");
    }

    public List<Song> getSongs(){
       return allSongs;
    }

    public List<Album> getAlbums() {
        return albums;
    }

    public String[] getSongsAsStringArray(){
        String[] songs = new String[allSongs.size()];
        for(int i = 0; i < songs.length; i++){
            songs[i] = allSongs.get(i).getArtist() + " - " + allSongs.get(i).getTitle();
        }
        return songs;
    }

    public String[] getAlbumsAsStringArray(){
        isViewingTempList = false;

        String[] allAlbums = new String[albums.size()];
        for(int i = 0; i < allAlbums.length; i++){
            allAlbums[i] = albums.get(i).getTitle()+ "\n" + albums.get(i).getArtist();
        }

        return allAlbums;
    }

    @Nullable
    public String[] getSongsByAlbumId(long albumId){
        if(!tempSongList.isEmpty()){
            tempSongList.clear();
        }

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String projection[] = {
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media._ID  },

                selectionArgs[] = null,
                selection = MediaStore.Audio.Media.ALBUM_ID + " = " + albumId,

                sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;

        Cursor cursor = contentResolver.query(
                uri,
                projection,
                selection, selectionArgs,
                sortOrder);

        if(cursor == null){
            System.err.println("Failed to retrieve music: cursor is null");
            return null;
        }

        if(!cursor.moveToFirst()){
            System.err.println("Failed to get first item. Empty Cursor");
            return null;
        }

        int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST),
                titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE),
                albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM),
                durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION),
                idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);


        do{
            tempSongList.add(new Song(
                    cursor.getLong(idColumn),
                    cursor.getString(artistColumn),
                    cursor.getString(titleColumn),
                    cursor.getString(albumColumn),
                    cursor.getLong(durationColumn)
            ));
        } while (cursor.moveToNext());

        cursor.close();

        isViewingTempList = true;

        String songs[] = new String[tempSongList.size()],
                artist, title;

        for(int i = 0; i < songs.length; i++){
            artist =  tempSongList.get(i).getArtist();
            title = tempSongList.get(i).getTitle();
            songs[i] = artist + " - " + title;
        }

        return songs;
    }

    public List<Song> getTempSongList() {
        return tempSongList;
    }

    public boolean isViewingTempList(){
        return isViewingTempList;
    }

    public String[] getTempSongListAsArray(){
        String songs[] = new String[tempSongList.size()],
                songTitle, artist;
        for(int i = 0; i < songs.length; i++){
            artist = tempSongList.get(i).getArtist();
            songTitle = tempSongList.get(i).getTitle();
            songs[i] = artist + " - " + songTitle;
        }
        return songs;
    }


}
