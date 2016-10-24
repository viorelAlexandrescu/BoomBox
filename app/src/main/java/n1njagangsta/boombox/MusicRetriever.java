package n1njagangsta.boombox;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

/**
 * Created by viorel on 16.08.2016.
 * Found this example online, i think it was via android examples
 * idk lol
 */
public class MusicRetriever {
    private ContentResolver contentResolver;
    private List<Song> allSongs;
    private Stack<Object> artistsStack, albumsStack;

    public MusicRetriever(ContentResolver newContentResolver) {
        contentResolver = newContentResolver;
        allSongs = new ArrayList<>();
        albumsStack = new Stack<>();
        artistsStack = new Stack<>();
    }

    // TODO Reimplement this method to not exit when a cursor is either null or empty
    public void prepare(){
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        // Songs Query
        String selection = MediaStore.Audio.Media.IS_MUSIC + " = 1",
                projection[] = null;

        Cursor cursor = contentResolver.query(
                uri,
                projection,
                selection, null, /*selection args*/
                null); /* sort order */

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

        // Album Query
        projection = new String[]{ MediaStore.Audio.AlbumColumns.ALBUM,
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

        List<Album> albumList = new ArrayList<>();
        do{
            ArrayList<Song> songsInAlbum = new ArrayList<>();
            for(int i = 0; i < getSongs().size(); i++){
                if(getSongs().get(i).getAlbum().contentEquals(cursor.getString(albumColumn))){
                    songsInAlbum.add(getSongs().get(i));
                }
            }

            albumList.add(new Album(
                    cursor.getString(artistColumn),
                    cursor.getString(albumColumn),
                    cursor.getLong(albumId),
                    songsInAlbum));
        } while (cursor.moveToNext());
        albumsStack.push(albumList);

        // Artists Query
        projection = new String[]{MediaStore.Audio.ArtistColumns.ARTIST};
        selection = MediaStore.Audio.ArtistColumns.ARTIST +
                " IS NOT NULL) GROUP BY (" + MediaStore.Audio.ArtistColumns.ARTIST;

        cursor = contentResolver.query(uri,projection,selection,null,null);

        if(cursor == null){
            System.err.println("Failed to retrieve artists: cursor is null");
            return;
        }

        if(!cursor.moveToFirst()){
            System.err.println("Failed to get first item. Empty Artist Cursor");
            return;
        }

        artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.ArtistColumns.ARTIST);

        String[] foundArtists = new String[cursor.getCount()];
        int artistIterator = 0;
        do{
            foundArtists[artistIterator] = cursor.getString(artistColumn);
            ++artistIterator;
        }while(cursor.moveToNext());

        List<Album> listOfAlbumsPerArtist;
        List<Artist> listOfArtists = new ArrayList<>();

        for(String currentArtist : foundArtists){
            listOfAlbumsPerArtist = new ArrayList<>();

            for(int i = 0; i < getAlbums().size(); i++){
                if(getAlbums().get(i).getArtist().contentEquals(currentArtist)){
                    listOfAlbumsPerArtist.add(getAlbums().get(i));
                }
            }

            listOfArtists.add(new Artist(currentArtist,listOfAlbumsPerArtist));
        }
        artistsStack.push(listOfArtists);

        cursor.close();
        System.out.println("Music Retriever Ready!");
    }

    public List<Song> getSongs(){
       return allSongs;
    }

    public List<Album> getAlbums(){
        return (List<Album>) albumsStack.get(0);
    }

    public List<Artist> getArtists(){
        return (List<Artist>) artistsStack.get(0);
    }

    public List<Album> getSelectedArtistAlbums(){
        return (List<Album>) artistsStack.get(1);
    }

    public List<Song> getSongListOfSelectedAlbumOfSelectedArtist() { return (List<Song>) artistsStack.get(2);}

    public List<Song> getSongListOfSelectedAlbumOfAlbumList(){
        return (List<Song>) albumsStack.get(1);
    }

    public int getArtistStackSize(){
        return artistsStack.size();
    }

    public int getAlbumStackSize(){
        return albumsStack.size();
    }

    public void pushAlbumListToArtistStack(List<Album> newAlbumList){
        artistsStack.push(newAlbumList);
    }

    public void pushSongListToArtistStack(List<Song> newSongList){
        artistsStack.push(newSongList);
    }

    public void popAlbumListFromArtistStack(){
        artistsStack.pop();
    }

    public void popSongListFromArtistStack(){
        artistsStack.pop();
    }

    public void pushSongListToAlbumStack(List<Song> newSongList){
        albumsStack.push(newSongList);
    }

    public void popSongListFromAlbumStack(){
        albumsStack.pop();
    }

    public String[] getSongsAsStringArray(){
        String[] songs = new String[allSongs.size()];
        for(int i = 0; i < songs.length; i++){
            songs[i] = allSongs.get(i).getArtist() + " - " + allSongs.get(i).getTitle();
        }
        return songs;
    }

    public String[] getAlbumsAsStringArray(){

        List<Album> albumList = (List<Album>) albumsStack.get(0);
        String[] allAlbums = new String[albumList.size()];

        for(int i = 0; i < allAlbums.length; i++){
            allAlbums[i] = albumList.get(i).getTitle()+ "\n" + albumList.get(i).getArtist();
        }

        return allAlbums;
    }

    public String[] getArtistsAsStringArray(){

        List<Artist> artistList = (List<Artist>) artistsStack.get(0);
        String allArtists[] = new String[artistList.size()];

        for(int i = 0; i < allArtists.length; i++){
            allArtists[i] = artistList.get(i).getArtistName();
        }

        return allArtists;
    }

    public String[] getAlbumListAsStringArray(List<Album> albumList){
        String[] albums = new String[albumList.size()];

        for(int i = 0; i < albums.length; i++){
            albums[i] = albumList.get(i).getTitle();
        }

        return albums;
    }

    public String[] getSongListFromAlbumAsStringArray(List<Song> songList){
        String[] songs = new String[songList.size()];

        for(int i = 0; i < songs.length; i++){
            songs[i] = songList.get(i).getArtist() + " - " + songList.get(i).getTitle();
        }

        return songs;
    }
}
