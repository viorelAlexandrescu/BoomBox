package n1njagangsta.boombox.Activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import n1njagangsta.boombox.Model.Song;
import n1njagangsta.boombox.Fragments.MusicListFragment;
import n1njagangsta.boombox.Fragments.MusicPlayerFragment;
import n1njagangsta.boombox.MusicRetriever;
import n1njagangsta.boombox.R;

public class MainActivity extends AppCompatActivity
        implements MusicListFragment.OnItemSelectedListener,
        MusicPlayerFragment.OnPlayerInteractionListener,
                    AudioManager.OnAudioFocusChangeListener{

    private android.app.FragmentManager fragmentManager;

    private TabLayout tabLayout;

    private FloatingActionButton quickFAB;

    private Toolbar myToolbar;

    private MusicListFragment musicListFragment;

    private MusicPlayerFragment musicPlayerFragment;

    private MusicRetriever musicRetriever;

    private AudioManager audioManager;

    private static MediaPlayer mediaPlayer;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode){
            case PackageManager.PERMISSION_GRANTED:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    initApp();
                }
                break;
            }
            // do not start app if you cannot access local data
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        }

        initApp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_between_screens:
                if(musicListFragment.isVisible()){
                    item.setIcon(R.drawable.ic_list_white_48dp);
                    changeScreenToPlayer();
                    item.setTitle(R.string.action_return_to_music_list);

                } else {
                    item.setIcon(R.drawable.ic_radio_white_48dp);
                    changeScreenToList();
                    item.setTitle(R.string.action_open_music_player);
                }
                break;
        }
        return true;
    }

    @Override
    public void OnListItemPicked(int index) {
        switch (tabLayout.getSelectedTabPosition()){
            case 0:
                //  artists tab
                onArtistSelect(index);
                break;
            case 1:
                //  albums tab
                onAlbumsSelect(index);
                break;
            case 2:
                // songs tab
                musicRetriever.setPlaylist(musicRetriever.getSongs());
                startPlaylist(index);
        }
    }

    @Override
    public boolean isSongSelected() {
        if(musicRetriever.getCurrentSong() != null){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onPlaybackClick() {
        boolean isMediaPlaying = mediaPlayer.isPlaying();

        if(isMediaPlaying){
            pausePlayback();
        } else {
            startPlayback();
        }

        musicPlayerFragment.changePlaybackButtonImage(!isMediaPlaying);
    }

    @Override
    public void onSkipToPreviousClick() {
        skipToPreviousSong();
    }

    @Override
    public void onSkipToNextClick() {
        skipToNextSong();
    }

    @Override
    public void onSeek(int seekValue) {
        pausePlayback();
        mediaPlayer.seekTo(seekValue);
    }

    @Override
    public int getSongPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public int getSongDuration() {
        return mediaPlayer.getDuration();
    }

    @Override
    public boolean isMusicPlaying() {
        return mediaPlayer.isPlaying();
    }

    private void startPlayback(){
        mediaPlayer.start();
        quickFAB.setImageResource(R.drawable.ic_pause_white_48dp);
    }

    private void pausePlayback(){
        mediaPlayer.pause();
        quickFAB.setImageResource(R.drawable.ic_play_arrow_white_48dp);
    }

    private void skipToPreviousSong(){
        if(musicRetriever.getPlaylist() != null){
            int currentSongIndex = musicRetriever.getIndexOfSongInPlaylist(
                    musicRetriever.getCurrentSong());
            if(currentSongIndex > 0){
                onSongSelect(musicRetriever.getPlaylist().get(currentSongIndex - 1));
            }
        } else {
            Toast.makeText(getApplicationContext(), "Null Playlist", Toast.LENGTH_SHORT).show();
        }
    }

    private void skipToNextSong(){
        if(musicRetriever.getPlaylist() != null){
            int currentSongIndex = musicRetriever.getIndexOfSongInPlaylist(
                    musicRetriever.getCurrentSong());
            if(currentSongIndex < musicRetriever.getPlaylistSize() - 1){
                onSongSelect(musicRetriever.getPlaylist().get(currentSongIndex + 1));
            }
        } else {
            Toast.makeText(getApplicationContext(), "Null Playlist", Toast.LENGTH_SHORT).show();
        }
    }

    private void initApp(){
        musicRetriever = new MusicRetriever(getApplicationContext().getContentResolver());
        musicRetriever.prepare();

        mediaPlayer = new MediaPlayer();

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        fragmentManager = getFragmentManager();

        musicListFragment = new MusicListFragment();
        musicPlayerFragment = new MusicPlayerFragment();

        prepareUI();
        changeScreenToList();
    }

    private void prepareUI() {
        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        tabLayout = (TabLayout) findViewById(R.id.music_list_tab_layout);
        quickFAB = (FloatingActionButton) findViewById(R.id.quickPlayFAB);

        quickFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    pausePlayback();
                } else {
                    startPlayback();
                }
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0:
                        switch (musicRetriever.getArtistStackSize()){
                            case 1:
                                musicListFragment.changeContents(musicRetriever.getArtistsAsStringArray());
                                break;
                            case 2:
                                musicListFragment.changeContents(
                                        musicRetriever.getAlbumListAsStringArray(
                                                musicRetriever.getSelectedArtistAlbums()));
                                break;
                            case 3:
                                musicListFragment.changeContents(
                                        musicRetriever.getSongListFromAlbumAsStringArray(
                                                musicRetriever.getSongListOfSelectedAlbumOfSelectedArtist()));
                                break;
                        }
                        break;
                    case 1:
                        switch (musicRetriever.getAlbumStackSize()){
                            case 1:
                                musicListFragment.changeContents(
                                        musicRetriever.getAlbumsAsStringArray());
                                break;
                            case 2:
                                musicListFragment.changeContents(
                                        musicRetriever.getSongListFromAlbumAsStringArray(
                                                musicRetriever.getSongListOfSelectedAlbumOfAlbumList()));
                                break;
                        }
                        break;
                    case 2:
                        musicListFragment.changeContents(musicRetriever.getSongsAsStringArray());
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                skipToNextSong();
            }
        });
        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mediaPlayer) {
                if (musicPlayerFragment.isVisible()) {
                    musicPlayerFragment.changePlaybackButtonImage(true);
                }
                startPlayback();
            }
        });

        setSupportActionBar(myToolbar);
    }

    private void onArtistSelect(int artistItemIndex){
        switch (musicRetriever.getArtistStackSize()){
            case 1:
                musicRetriever.pushAlbumListToArtistStack(
                        musicRetriever.getArtists().get(artistItemIndex).getAlbumList());

                musicListFragment.changeContents(
                        musicRetriever.getAlbumListAsStringArray(
                                musicRetriever.getSelectedArtistAlbums()));
                break;
            case 2:
                try{
                    musicRetriever.pushSongListToArtistStack(
                            musicRetriever.getSelectedArtistAlbums().get(artistItemIndex).getAlbumSongList());

                    musicListFragment.changeContents(
                            musicRetriever.getSongListFromAlbumAsStringArray(
                                    musicRetriever.getSongListOfSelectedAlbumOfSelectedArtist()));
                } catch (Exception e){
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
            case 3:
                musicRetriever.setPlaylist(
                        musicRetriever.getSongListOfSelectedAlbumOfSelectedArtist());
                startPlaylist(artistItemIndex);
                break;
        }
    }

    private void onAlbumsSelect(int albumItemIndex){
        switch (musicRetriever.getAlbumStackSize()){
            case 1:
                try {
                    musicRetriever.pushSongListToAlbumStack(
                            musicRetriever.getAlbums().get(albumItemIndex).getAlbumSongList());

                    musicListFragment.changeContents(
                            musicRetriever.getSongListFromAlbumAsStringArray(
                                    musicRetriever.getSongListOfSelectedAlbumOfAlbumList()));
                } catch (Exception e){
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                musicRetriever.setPlaylist(
                        musicRetriever.getSongListOfSelectedAlbumOfAlbumList());
                startPlaylist(albumItemIndex);
                break;
        }
    }

    private void onSongSelect(Song newSong) {
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        mediaPlayer.reset();

        try {
            mediaPlayer.setDataSource(getApplicationContext(), newSong.getURI());
            mediaPlayer.prepare();
            startPlayback();

            myToolbar.setTitle(newSong.getTitle());
            myToolbar.setSubtitle(newSong.getArtist());

            musicRetriever.setCurrentSelectedAlbum(
                    musicRetriever.getAlbumByKey(
                            newSong.getSongAlbumKey()));

            musicRetriever.setCurrentSong(newSong);

            //this is implicit to when skipping songs as well
            if(musicPlayerFragment.isVisible()){
                musicPlayerFragment.setCurrentSongPositionText(0);
                musicPlayerFragment.setSongDurationTextView(mediaPlayer.getDuration());
                musicPlayerFragment.changePlaybackButtonImage(true);
            }
        } catch (IOException ioe){
            Toast.makeText(getApplicationContext(),
                    newSong.getTitle() + " does not exist.",
                    Toast.LENGTH_SHORT).show();

            // here add a method to remove this entry
        }
    }

    private void startPlaylist(int playlistItem){
        onSongSelect(musicRetriever.getPlaylist().get(playlistItem));
    }

    private void changeScreenToList() {
            tabLayout.setVisibility(View.VISIBLE);

            String artistsKey = "artistList",
                    albumsKey = "albumList",
                    songsKey = "songList";

            Bundle bundleData = new Bundle();
            bundleData.putStringArray(songsKey, musicRetriever.getSongsAsStringArray());

            switch (musicRetriever.getArtistStackSize()){
                case 1:
                    bundleData.putStringArray(artistsKey, musicRetriever.getArtistsAsStringArray());
                    break;
                case 2:
                    bundleData.putStringArray(artistsKey,
                            musicRetriever.getAlbumListAsStringArray(
                                    musicRetriever.getSelectedArtistAlbums()));
                    break;
                case 3:
                    bundleData.putStringArray(artistsKey,
                            musicRetriever.getSongListFromAlbumAsStringArray(
                                    musicRetriever.getSongListOfSelectedAlbumOfSelectedArtist()));
                    break;
            }

            switch (musicRetriever.getAlbumStackSize()){
                case 1:
                    bundleData.putStringArray(albumsKey, musicRetriever.getAlbumsAsStringArray());
                    break;
                case 2:
                    bundleData.putStringArray(albumsKey,
                            musicRetriever.getSongListFromAlbumAsStringArray(
                                    musicRetriever.getSongListOfSelectedAlbumOfAlbumList()));
                    break;
            }

            bundleData.putInt("currentTabPosition", tabLayout.getSelectedTabPosition());
            musicListFragment.setArguments(bundleData);

            fragmentManager.beginTransaction().
                    replace(R.id.fragment_container, musicListFragment).commit();

        quickFAB.show();
    }

    private void changeScreenToPlayer() {
        Bundle bundle = new Bundle();

        if (musicRetriever.getCurrentSelectedAlbum() != null) {
            musicPlayerFragment.setAlbumArtBitmap(
                    musicRetriever.getCurrentSelectedAlbum().getAlbumArt());
        }

        tabLayout.setVisibility(View.GONE);
        quickFAB.hide();

        musicPlayerFragment.setArguments(bundle);
        getFragmentManager().beginTransaction().
                replace(R.id.fragment_container, musicPlayerFragment).commit();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        // add more cases if you wish to approach more audio focus cases
        boolean isMusicPlaying = mediaPlayer.isPlaying();

        if(focusChange == AudioManager.AUDIOFOCUS_LOSS || focusChange ==  AudioManager.AUDIOFOCUS_LOSS_TRANSIENT){
            if(isMusicPlaying){
                pausePlayback();
                if(musicPlayerFragment.isVisible()){
                    musicPlayerFragment.changePlaybackButtonImage(false);
                }
            }
        } else {
            switch (focusChange){
                case AudioManager.AUDIOFOCUS_GAIN:
                    // TODO find a way to make audio start only when wanted, When player is paused, during phone call also make the app not start playback if the player is paused and later the app receives audio focus
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(musicListFragment.isVisible()){
            switch (tabLayout.getSelectedTabPosition()){
                case 0:
                    switch (musicRetriever.getArtistStackSize()){
                        case 1:
                            break;
                        case 2:
                            musicRetriever.popAlbumListFromArtistStack();
                            musicListFragment.changeContents(
                                    musicRetriever.getArtistsAsStringArray());
                            break;
                        case 3:
                            musicRetriever.popSongListFromArtistStack();
                            musicListFragment.changeContents(
                                    musicRetriever.getAlbumListAsStringArray(
                                            musicRetriever.getSelectedArtistAlbums()));
                            break;
                    }
                    break;
                case 1:
                    switch (musicRetriever.getAlbumStackSize()){
                        case 1:
                            break;
                        case 2:
                            musicRetriever.popSongListFromAlbumStack();
                            musicListFragment.changeContents(
                                    musicRetriever.getAlbumsAsStringArray());
                            break;
                    }
                    break;
            }
        }
    }
}