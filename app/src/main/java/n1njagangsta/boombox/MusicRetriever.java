package n1njagangsta.boombox;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import n1njagangsta.boombox.Model.Album;
import n1njagangsta.boombox.Model.Artist;
import n1njagangsta.boombox.Model.Song;

/**
 * Created by viorel on 16.08.2016.
 * Found this example online, i think it was via android examples
 * idk lol
 */
public class MusicRetriever {
    private ContentResolver contentResolver;
    private ArrayList<Song> allSongs;
    private Stack<Object> artistsStack, albumsStack;
    private ArrayList<Song> currentPlaylist;
    private int currentSongIndex;

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
            System.err.println("Failed to get first item. No songs on device");
            return;
        }

        int artistColumn = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST),
                titleColumn = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE),
                albumColumn = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM),
                durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION),
                idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);

        long songId = cursor.getLong(idColumn);

        //here we instantiate all local songs
        do{
            String songTitle = cursor.getString(titleColumn),
                    artistName = cursor.getString(artistColumn),
                    album = cursor.getString(albumColumn);

            int duration = cursor.getInt(durationColumn);
            //this works for local songs, for URL use setSongUri which (hopefully) parses the url
            Uri songUri = ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    songId);

            Song newSong = new Song(songTitle, artistName, album, duration, songUri);

            allSongs.add(newSong);
        } while (cursor.moveToNext());

        // Album Query
        uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;

        projection = new String[]{ MediaStore.Audio.AlbumColumns.ALBUM,
                MediaStore.Audio.AlbumColumns.ARTIST,
                MediaStore.Audio.AlbumColumns.ALBUM_KEY,
                MediaStore.Audio.AlbumColumns.ALBUM_ART};

        // retrieves the albums sorted by album title
        selection = MediaStore.Audio.Albums.ALBUM +
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
//        cursor.moveToFirst();

        artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ARTIST);
        albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ALBUM);
        int albumArtColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ALBUM_ART);

        ArrayList<Album> albumList = new ArrayList<>();
        do{
            ArrayList<Song> songsInAlbum = new ArrayList<>();
            for(Song currentSong : allSongs){
                String currentAlbum = cursor.getString(albumColumn);
                if(currentSong.getAlbumName().contentEquals(currentAlbum)){
                    songsInAlbum.add(currentSong);
                }
            }
            Bitmap newAlbumArt = BitmapFactory.decodeFile(cursor.getString(albumArtColumn));

            Album newAlbum = new Album(
                    cursor.getString(albumColumn),
                    cursor.getString(artistColumn),
                    songsInAlbum,
                    newAlbumArt);

            albumList.add(newAlbum);

        } while (cursor.moveToNext());
        albumsStack.push(albumList);

        // Artists Query
        uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;

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

        List<Artist> listOfArtists = new ArrayList<>();
        do {
            String currentArtist = cursor.getString(artistColumn);
            ArrayList<Album> listOfAlbumsPerArtist = new ArrayList<>();

            for(int i = 0; i < getAlbums().size(); i++){
                if(getAlbums().get(i).getArtistName().contentEquals(currentArtist)){
                    listOfAlbumsPerArtist.add(getAlbums().get(i));
                }
            }
            listOfArtists.add(new Artist(currentArtist, listOfAlbumsPerArtist));
        } while(cursor.moveToNext());
        artistsStack.push(listOfArtists);
        //todo sort artist list by artist name

        cursor.close();
    }

    public ArrayList<Song> getSongs(){
        return allSongs;
    }

    public List<Album> getAlbums(){
        return (ArrayList<Album>) albumsStack.get(0);
    }

    public List<Artist> getArtists(){
        return (ArrayList<Artist>) artistsStack.get(0);
    }

    public List<Album> getSelectedArtistAlbums(){
        return (ArrayList<Album>) artistsStack.get(1);
    }

    public ArrayList<Song> getSongListOfSelectedAlbumOfSelectedArtist() { return (ArrayList<Song>) artistsStack.get(2);}

    public ArrayList<Song> getSongListOfSelectedAlbumOfAlbumList(){
        return (ArrayList<Song>) albumsStack.get(1);
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

    public void popArtistStack(){
        artistsStack.pop();
    }

    public void pushSongListToAlbumStack(List<Song> newSongList){
        albumsStack.push(newSongList);
    }

    public void popSongListFromAlbumStack(){
        albumsStack.pop();
    }

    public String[] getSongListAsStringArray(List<Song> songList){
        String[] songs = new String[songList.size()];

        for(int i = 0; i < songs.length; i++){
            songs[i] = songList.get(i).getArtistName() + " - " + songList.get(i).getSongTitle();
        }

        return songs;
    }

    public String[] getAlbumListAsStringArray(List<Album> albumList){
        String[] albums = new String[albumList.size()];

        for(int i = 0; i < albums.length; i++){
            albums[i] = albumList.get(i).getAlbumTitle();
        }

        return albums;
    }

    public String[] getArtistListAsStringArray(){
        List<Artist> artistList = getArtists();
        String[] artists = new String[artistList.size()];

        for(int i = 0; i < artists.length; ++i){
            artists[i] = artistList.get(i).getArtistName();
        }

        return artists;
    }

    /**
     * Performs a binary search on the whole album list.
     * BEWARE, the list may not be sorted by default.
     *
     * Returns the Album searched for or throws a NullPointerException due to the fact
     * that there is no album found
     */
    public Album getAlbumByName(String albumTitle) throws NullPointerException{
        int foundAlbumIndex = Arrays.binarySearch(
                getAlbumListAsStringArray(getAlbums()), albumTitle);
        if(foundAlbumIndex >= 0) {
            return getAlbums().get(foundAlbumIndex);
        } else throw new NullPointerException("No album found by title:" + albumTitle);
    }

    public void setPlaylist(ArrayList<Song> newPlaylist){
        this.currentPlaylist = newPlaylist;
    }

    public ArrayList<Song> getPlaylist(){
        return this.currentPlaylist;
    }

    public int getCurrentSongIndex() {
        return currentSongIndex;
    }

    public void setCurrentSongIndex(int newCurrentSongIndex){
        this.currentSongIndex = newCurrentSongIndex;
    }
}