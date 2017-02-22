package n1njagangsta.boombox.Activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import n1njagangsta.boombox.Model.MediaPlayerInteraction;
import n1njagangsta.boombox.Model.Song;
import n1njagangsta.boombox.Fragments.MusicListFragment;
import n1njagangsta.boombox.Fragments.MusicPlayerFragment;
import n1njagangsta.boombox.MusicRetriever;
import n1njagangsta.boombox.R;

public class MainActivity extends AppCompatActivity
        implements MusicListFragment.OnItemSelectedListener,
        MusicPlayerFragment.OnPlayerViewInteractionListener,
        MediaPlayerInteraction {

    private android.app.FragmentManager fragmentManager;

    private TabLayout tabLayout;

    private Toolbar myToolbar;

    private MusicListFragment musicListFragment;

    private MusicPlayerFragment musicPlayerFragment;

    private MusicRetriever musicRetriever;

    private AudioManager audioManager;

    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;

    private MediaPlayer mediaPlayer;

    //todo when headphones come out, pause playback

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PackageManager.PERMISSION_GRANTED: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_between_screens:
                if (musicListFragment.isVisible()) {
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
    public void onListItemPicked(int listItemIndex) {
        ArrayList<Song> newSongList = null;
        switch (tabLayout.getSelectedTabPosition()) {
            //  artists tab
            case 0:
                switch (musicRetriever.getArtistStackSize()) {
                    // user selected an artist
                    case 1:
                        musicRetriever.pushAlbumListToArtistStack(
                                musicRetriever.getArtists().get(listItemIndex).getAlbumList());

                        musicListFragment.changeContents(
                                musicRetriever.getAlbumListAsStringArray(
                                        musicRetriever.getSelectedArtistAlbums()));
                        break;
                    case 2:
                        // user selected an album from the selected artist
                        musicRetriever.pushSongListToArtistStack(
                                musicRetriever.getSelectedArtistAlbums().
                                        get(listItemIndex).getAlbumSongList());

                        musicListFragment.changeContents(
                                musicRetriever.getSongListAsStringArray(
                                        musicRetriever.getSongListOfSelectedAlbumOfSelectedArtist()));
                        break;
                    case 3:
                        // user selected a song from previously selected album
                        newSongList = musicRetriever.getSongListOfSelectedAlbumOfSelectedArtist();
                        break;
                }
                break;

            //  albums tab
            case 1:
                switch (musicRetriever.getAlbumStackSize()) {
                    case 1:
                        musicRetriever.pushSongListToAlbumStack(
                                musicRetriever.getAlbums().get(listItemIndex).getAlbumSongList());

                        musicListFragment.changeContents(
                                musicRetriever.getSongListAsStringArray(
                                        musicRetriever.getSongListOfSelectedAlbumOfAlbumList()));
                        break;
                    case 2:
                        newSongList = musicRetriever.getSongListOfSelectedAlbumOfAlbumList();
                        break;
                }
                break;

            // songs tab
            case 2:
                newSongList = musicRetriever.getSongs();
                break;
        }
        if(newSongList != null){
            setPlaylistAndStartPlaybackFromIndex(newSongList ,listItemIndex);
        }
    }

    private void setPlaylistAndStartPlaybackFromIndex(ArrayList<Song> newPlaylist, int index) {
        musicRetriever.setPlaylist(newPlaylist);
        musicRetriever.setCurrentSongIndex(index);
        onSongSelect(newPlaylist.get(index));
    }

    @Override
    public void onPlaybackClick() {
        if (mediaPlayer.isPlaying())
            mediaPlayer.pause();
        else
            mediaPlayer.start();

    }

    @Override
    public void onSkipToPreviousClick() {
        if (musicRetriever.getPlaylist() != null) {
            int currentSongIndex = musicRetriever.getCurrentSongIndex();
            if (currentSongIndex > 0) {
                musicRetriever.setCurrentSongIndex(--currentSongIndex);
                onSongSelect(musicRetriever.getPlaylist().get(currentSongIndex));
                musicPlayerFragment.resetPlayerInfo();
            } else {
                Toast.makeText(getApplicationContext(), "It's song #1", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Null Playlist", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSkipToNextClick() {
        if (musicRetriever.getPlaylist() != null) {
            int currentSongIndex = musicRetriever.getCurrentSongIndex();
            if (currentSongIndex < musicRetriever.getPlaylistSize() - 1) {
                musicRetriever.setCurrentSongIndex(++currentSongIndex);
                onSongSelect(musicRetriever.getPlaylist().get(currentSongIndex));
                musicPlayerFragment.resetPlayerInfo();
            } else
                Toast.makeText(getApplicationContext(), "End of playlist", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getApplicationContext(), "Null Playlist", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onSeek(int seekValue) {
        mediaPlayer.pause();
        mediaPlayer.seekTo(seekValue);
    }

    @Override
    public int getSongPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public int getSongDuration() {
        return musicRetriever.getCurrentSong() != null ?
                (int) musicRetriever.getCurrentSong().getDuration() : 0;
    }

    @Override
    public boolean isMusicPlaying() {
        return mediaPlayer.isPlaying();
    }

    private void onSongSelect(Song newSong) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.reset();

        try {
            mediaPlayer.setDataSource(getApplicationContext(), newSong.getURI());
            mediaPlayer.prepare();
            mediaPlayer.start();

            myToolbar.setTitle(newSong.getSongTitle());
            myToolbar.setSubtitle(newSong.getArtistName());


            if (musicListFragment.isVisible()) {
                musicListFragment.changeFABImage(true);
            } else {
                //this is implicit to when skipping songs as well
                musicPlayerFragment.changePlaybackButtonImage(true);
            }


        } catch (IOException ioe) {
            Toast.makeText(getApplicationContext(),
                    newSong.getSongTitle() + " does not exist.",
                    Toast.LENGTH_SHORT).show();
            // here add a method to remove this entry
        }
    }

    private void changeScreenToList() {
        Bundle bundleData = new Bundle();

        String currentListKey = "currentList",
                playerStatusKey = "isMusicPlaying";
        String[] itemListValues;

        switch (tabLayout.getSelectedTabPosition()) {
            case 0:
                // user may have selected only an artist or not
                if (musicRetriever.getArtistStackSize() < 3) {
                    if (musicRetriever.getArtistStackSize() == 2) {
                        // user has only selected an artist and is viewing it's album list
                        itemListValues = musicRetriever.getAlbumListAsStringArray(
                                musicRetriever.getSelectedArtistAlbums());
                    } else {
                        // user has not selected an artist yet
                        itemListValues = musicRetriever.getArtistListAsStringArray();
                    }
                } else {
                    // user has selected an album and is viewing it's song list
                    itemListValues = musicRetriever.getSongListAsStringArray(
                            musicRetriever.getSongListOfSelectedAlbumOfSelectedArtist());
                }
                break;
            case 1:

                if (musicRetriever.getAlbumStackSize() > 1) {
                    // user may have selected an album list
                    itemListValues = musicRetriever.getSongListAsStringArray(
                            musicRetriever.getSongListOfSelectedAlbumOfAlbumList());
                } else {
                    // no album has been selected from the album list
                    itemListValues = musicRetriever.getAlbumListAsStringArray(musicRetriever.getAlbums());
                }
                break;
            default:
                itemListValues = musicRetriever.getSongListAsStringArray(musicRetriever.getSongs());
                break;
        }

        bundleData.putStringArray(currentListKey, itemListValues);
        bundleData.putBoolean(playerStatusKey, mediaPlayer.isPlaying());

        musicListFragment.setArguments(bundleData);

        tabLayout.setVisibility(View.VISIBLE);

        fragmentManager.beginTransaction().
                replace(R.id.fragment_container, musicListFragment).commit();
    }

    private void changeScreenToPlayer() {
        Bundle bundleData = new Bundle();

        String playerStatusKey = "isMusicPlaying",
                currentSongPositionKey = "currentSongPosition",
                songDurationKey = "currentSongDuration";

        bundleData.putBoolean(playerStatusKey, mediaPlayer.isPlaying());
        bundleData.putInt(currentSongPositionKey, mediaPlayer.getCurrentPosition());
        bundleData.putInt(songDurationKey, musicRetriever.getCurrentSong() != null ?
                (int) musicRetriever.getCurrentSong().getDuration() : -1);

        musicPlayerFragment.setArguments(bundleData);

        musicListFragment.changeFABVisibility(false);
        tabLayout.setVisibility(View.GONE);

        getFragmentManager().beginTransaction().
                replace(R.id.fragment_container, musicPlayerFragment).commit();
    }

    private void initApp() {
        fragmentManager = getFragmentManager();

        musicListFragment = new MusicListFragment();
        musicPlayerFragment = new MusicPlayerFragment();

        prepareUI();
        changeScreenToList();
    }

    private void prepareUI() {
        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        tabLayout = (TabLayout) findViewById(R.id.music_list_tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String[] newItems;
                switch (tab.getPosition()) {
                    case 0:
                        switch (musicRetriever.getArtistStackSize()) {
                            case 2:
                                newItems = musicRetriever.getAlbumListAsStringArray(
                                        musicRetriever.getSelectedArtistAlbums());
                                break;
                            case 3:
                                newItems = musicRetriever.getSongListAsStringArray(
                                        musicRetriever.getSongListOfSelectedAlbumOfSelectedArtist());
                                break;
                            default:
                                newItems = musicRetriever.getArtistListAsStringArray();
                                break;
                        }
                        break;
                    case 1:
                        switch (musicRetriever.getAlbumStackSize()) {
                            case 2:
                                newItems = musicRetriever.getSongListAsStringArray(
                                        musicRetriever.getSongListOfSelectedAlbumOfAlbumList());
                                break;
                            default:
                                newItems = musicRetriever.getAlbumListAsStringArray(
                                        musicRetriever.getAlbums());
                                break;
                        }
                        break;
                    default:
                        newItems = musicRetriever.getSongListAsStringArray(
                                musicRetriever.getSongs());
                        break;
                }
                musicListFragment.changeContents(newItems);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        setSupportActionBar(myToolbar);
    }

    @Override
    public void onBackPressed() {
        if (musicListFragment.isVisible()) {
            switch (tabLayout.getSelectedTabPosition()) {
                case 0:
                    switch (musicRetriever.getArtistStackSize()) {
                        case 2:
                            // 2 pops makes the stack have only artists
                            musicRetriever.popArtistStack();
                            musicListFragment.changeContents(
                                    musicRetriever.getArtistListAsStringArray());
                            break;
                        case 3:
                            // 1 pop make the stack have only artists and selected artist album
                            musicRetriever.popArtistStack();
                            musicListFragment.changeContents(
                                    musicRetriever.getAlbumListAsStringArray(
                                            musicRetriever.getSelectedArtistAlbums()));
                            break;
                    }
                    break;
                case 1:
                    switch (musicRetriever.getAlbumStackSize()) {
                        case 2:
                            musicRetriever.popSongListFromAlbumStack();
                            musicListFragment.changeContents(
                                    musicRetriever.getAlbumListAsStringArray(
                                            musicRetriever.getAlbums()));
                            break;
                    }
                    break;
            }
        }
    }
}