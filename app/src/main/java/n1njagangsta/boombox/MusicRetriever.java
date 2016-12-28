package n1njagangsta.boombox;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
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
    private ArrayList<Song> allSongs, currentPlaylist;
    private Stack<Object> artistsStack, albumsStack;
    private Album currentSelectedAlbum;
    private Song currentSong;

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

        int artistColumn = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST),
                titleColumn = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE),
                albumColumn = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM),
                songIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);

        do{
            allSongs.add(new Song(
                    cursor.getLong(songIdColumn),
                    cursor.getString(artistColumn),
                    cursor.getString(titleColumn),
                    cursor.getString(albumColumn),
                    null
            )); // song Album key is set to null so that they keys may be set within the initialization of the albums
        } while (cursor.moveToNext());

        // Album Query
        uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;

        projection = new String[]{ MediaStore.Audio.AlbumColumns.ALBUM,
                                    MediaStore.Audio.AlbumColumns.ARTIST,
                                     MediaStore.Audio.AlbumColumns.ALBUM_KEY,
                                      MediaStore.Audio.AlbumColumns.ALBUM_ART};

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

        artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ARTIST);
        albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ALBUM);
        int albumKey = cursor.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ALBUM_KEY),
                albumArtColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ALBUM_ART);

        ArrayList<Album> albumList = new ArrayList<>();
        ArrayList<Song> songs = allSongs;
        do{
            ArrayList<Song> songsInAlbum = new ArrayList<>();
            for(int i = 0; i < songs.size(); i++){
                if(songs.get(i).getAlbum().contentEquals(cursor.getString(albumColumn))){
                    songs.get(i).setAlbumKey(cursor.getString(albumKey));
                    songsInAlbum.add(songs.get(i));
                }
            }

            Bitmap albumArt = BitmapFactory.decodeFile(cursor.getString(albumArtColumn));

            albumList.add(new Album(
                    cursor.getString(artistColumn),
                    cursor.getString(albumColumn),
                    cursor.getString(albumKey),
                    songsInAlbum,
                    albumArt));
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

    public Album getCurrentSelectedAlbum() {
        return currentSelectedAlbum;
    }

    public void setCurrentSelectedAlbum(Album currentSelectedAlbum) {
        this.currentSelectedAlbum = currentSelectedAlbum;
    }

    public Album getAlbumByKey(String songAlbumKey){
        for(Album album : getAlbums()){
            if(album.getAlbumKey().contentEquals(songAlbumKey)){
                return album;
            }
        }
        return null;
    }

    public void setPlaylist(ArrayList<Song> newPlaylist){
        this.currentPlaylist = newPlaylist;
    }

    public ArrayList<Song> getPlaylist(){
        return this.currentPlaylist;
    }

    public int getPlaylistSize(){
        return this.currentPlaylist.size();
    }

    public int getIndexOfSongInPlaylist(Song song){
        for(int i = 0; i < currentPlaylist.size(); i++){
            if(currentPlaylist.get(i).getURI().compareTo(song.getURI()) == 0){
                return i;
            }
        }
        throw new IndexOutOfBoundsException("Song Ain't Here :P");
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public void setCurrentSong(Song currentSong) {
        this.currentSong = currentSong;
    }
}
