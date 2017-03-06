package n1njagangsta.boombox.Activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.app.FragmentManager;

import java.util.ArrayList;

import n1njagangsta.boombox.Model.MediaPlayerInteraction;
import n1njagangsta.boombox.Model.Song;
import n1njagangsta.boombox.Fragments.MusicListFragment;
import n1njagangsta.boombox.Fragments.MusicPlayerFragment;
import n1njagangsta.boombox.MusicRetriever;
import n1njagangsta.boombox.PrepareMusicRetrieverTask;
import n1njagangsta.boombox.R;
import n1njagangsta.boombox.Services.PlaybackService;

public class MainActivity extends AppCompatActivity
        implements MusicListFragment.OnItemSelectedListener,
        MusicPlayerFragment.OnPlayerViewInteractionListener,
        PrepareMusicRetrieverTask.MusicRetrieverPreparedListener{

    FragmentManager fragmentManager;
    TabLayout tabLayout;
    Toolbar myToolbar;
    MusicListFragment musicListFragment;
    MusicPlayerFragment musicPlayerFragment;
    MusicRetriever mRetriever;
    BroadcastReceiver mBroadcastReceiver;

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
    public void onBackPressed() {
        if (musicListFragment.isVisible()) {
            switch (tabLayout.getSelectedTabPosition()) {
                case 0:
                    switch (mRetriever.getArtistStackSize()) {
                        case 2:
                            // 2 pops makes the stack have only artists
                            mRetriever.popArtistStack();
                            musicListFragment.changeContents(
                                    mRetriever.getArtistListAsStringArray());
                            break;
                        case 3:
                            // 1 pop make the stack have only artists and selected artist album
                            mRetriever.popArtistStack();
                            musicListFragment.changeContents(
                                    mRetriever.getAlbumListAsStringArray(
                                            mRetriever.getSelectedArtistAlbums()));
                            break;
                    }
                    break;
                case 1:
                    switch (mRetriever.getAlbumStackSize()) {
                        case 2:
                            mRetriever.popSongListFromAlbumStack();
                            musicListFragment.changeContents(
                                    mRetriever.getAlbumListAsStringArray(
                                            mRetriever.getAlbums()));
                            break;
                    }
                    break;
            }
        }
    }

    /**
     * Sets a new playlist and start a new song from the playlist with respect at the given index.
     * Selecting a song from an album already set as a playlist shall only pass the new song's index
     * <p>
     * <p>
     * By default this method shall update the list with a new array of strings
     * representing either an album list or a song list.
     *
     * @param listItemIndex is the selected list item index
     */
    @Override
    public void onListItemPicked(int listItemIndex) {
        // newSongList is the selected playlist
        ArrayList<Song> newSongList = null,
                currentPlaylist = mRetriever.getPlaylist();
        switch (tabLayout.getSelectedTabPosition()) {
            //  artists tab
            case 0:
                switch (mRetriever.getArtistStackSize()) {
                    // user selected an artist
                    case 1:
                        // push to the artist stack the selected artist album
                        mRetriever.pushAlbumListToArtistStack(
                                mRetriever.getArtists().get(listItemIndex).getAlbumList());
                        // change the list contents with the new album
                        musicListFragment.changeContents(
                                mRetriever.getAlbumListAsStringArray(
                                        mRetriever.getSelectedArtistAlbums()));
                        break;
                    case 2:
                        // user selected an album of the selected artist
                        mRetriever.pushSongListToArtistStack(
                                mRetriever.getSelectedArtistAlbums().
                                        get(listItemIndex).getAlbumSongList());

                        musicListFragment.changeContents(
                                mRetriever.getSongListAsStringArray(
                                        mRetriever.getSongListOfSelectedAlbumOfSelectedArtist()));
                        break;
                    case 3:
                        // user selected a song from previously selected album
                        newSongList = mRetriever.getSongListOfSelectedAlbumOfSelectedArtist();
                        break;
                }
                break;

            //  albums tab
            case 1:
                switch (mRetriever.getAlbumStackSize()) {
                    case 1:
                        mRetriever.pushSongListToAlbumStack(
                                mRetriever.getAlbums().get(listItemIndex).getAlbumSongList());

                        musicListFragment.changeContents(
                                mRetriever.getSongListAsStringArray(
                                        mRetriever.getSongListOfSelectedAlbumOfAlbumList()));
                        break;
                    case 2:
                        newSongList = mRetriever.getSongListOfSelectedAlbumOfAlbumList();
                        break;
                }
                break;

            // songs tab
            default:
                newSongList = mRetriever.getSongs();
                break;
        }

        mRetriever.setCurrentSongIndex(listItemIndex);
        if (currentPlaylist == null || !currentPlaylist.equals(newSongList)){
            mRetriever.setPlaylist(newSongList);
        } else newSongList = null;

        onSongSelect(newSongList, listItemIndex);
    }

    @Override
    public void onSkipToPreviousClick() {
        Intent skipToPreviousIntent = new Intent(PlaybackService.ACTION_PLAY_PREVIOUS_SONG);
        startService(skipToPreviousIntent);
    }

    @Override
    public void onSkipToNextClick() {
        Intent skipToNextIntent = new Intent(PlaybackService.ACTION_PLAY_NEXT_SONG);
        startService(skipToNextIntent);
    }

    @Override
    public void onSeek(int seekValue) {
        Intent seekIntent = new Intent(PlaybackService.ACTION_SEEK_TO);
        seekIntent.putExtra(String.valueOf(R.string.Intent_Seek_To_Integer_Value),seekValue);
        startService(seekIntent);
    }

    @Override
    public int getSongDuration() {
        return mRetriever.getPlaylist().get(mRetriever.getCurrentSongIndex()).getDuration();
    }

    /**
     * Start playback of selected song.
     * <p>
     * By default, we send current song data and the album of which it belongs to, but the
     * method checks if the album from which the song has been previously selected,
     * if so, just send the index at which new song is at.
     */
    private void onSongSelect(ArrayList<Song> newSongList, int newSongIndex) {

        Intent playbackIntent = new Intent(PlaybackService.ACTION_START_PLAYBACK);

        if (newSongList != null) {
            playbackIntent.putParcelableArrayListExtra(String.valueOf(R.string.Intent_New_Playlist_Key), newSongList);
        }
        playbackIntent.putExtra(String.valueOf(R.string.Intent_New_Song_Index_Key), newSongIndex);

        startService(playbackIntent);
        myToolbar.setTitle(mRetriever.getPlaylist().get(newSongIndex).getSongTitle());
        myToolbar.setSubtitle(mRetriever.getPlaylist().get(newSongIndex).getArtistName());

        if (musicListFragment.isVisible()) {
            musicListFragment.changeFABImage(true);
        } else {
            //this is implicit to when skipping songs as well
            musicPlayerFragment.changePlaybackButtonImage(true);
        }
    }

    /**
     * This method passes the media player status so it may change FAB image
     * And the current list of items
     */
    private void changeScreenToList() {
        Bundle bundleData = new Bundle();

        String currentListKey = "currentList", itemListValues[];

        switch (tabLayout.getSelectedTabPosition()) {
            case 0:
                // user may have selected only an artist or not
                if (mRetriever.getArtistStackSize() < 3) {
                    if (mRetriever.getArtistStackSize() == 2) {
                        // user has only selected an artist and is viewing it's album list
                        itemListValues = mRetriever.getAlbumListAsStringArray(
                                mRetriever.getSelectedArtistAlbums());
                    } else {
                        // user has not selected an artist yet
                        itemListValues = mRetriever.getArtistListAsStringArray();
                    }
                } else {
                    // user has selected an album and is viewing it's song list
                    itemListValues = mRetriever.getSongListAsStringArray(
                            mRetriever.getSongListOfSelectedAlbumOfSelectedArtist());
                }
                break;
            case 1:
                if (mRetriever.getAlbumStackSize() > 1) {
                    // user may have selected an album list
                    itemListValues = mRetriever.getSongListAsStringArray(
                            mRetriever.getSongListOfSelectedAlbumOfAlbumList());
                } else {
                    // no album has been selected from the album list
                    itemListValues = mRetriever.getAlbumListAsStringArray(mRetriever.getAlbums());
                }
                break;
            default:
                itemListValues = mRetriever.getSongListAsStringArray(mRetriever.getSongs());
                break;
        }

        bundleData.putStringArray(currentListKey, itemListValues);
        // todo here pass media player status

        musicListFragment.setArguments(bundleData);

        tabLayout.setVisibility(View.VISIBLE);

        fragmentManager.beginTransaction().
                replace(R.id.fragment_container, musicListFragment).commit();
    }

    /**
     * This method passes the media player status, current playback position
     * and the song duration
     */
    private void changeScreenToPlayer() {
        musicListFragment.changeFABVisibility(false);
        tabLayout.setVisibility(View.GONE);

        getFragmentManager().beginTransaction().
                replace(R.id.fragment_container, musicPlayerFragment).commit();
    }

    /**
     * This main initializes objects
     */
    private void initApp() {
        prepareUI();

        mRetriever = new MusicRetriever(getContentResolver());
        PrepareMusicRetrieverTask prepRetrieverTask =
                new PrepareMusicRetrieverTask(mRetriever, this);
        prepRetrieverTask.execute();

        fragmentManager = getFragmentManager();

        musicListFragment = new MusicListFragment();
        musicPlayerFragment = new MusicPlayerFragment();

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(getApplicationContext(), "Received " + intent.getAction() + " Intent",
                        Toast.LENGTH_SHORT).show();
            }
        };
        changeScreenToList();
    }

    /**
     * This method prepares the UI elements
     */
    private void prepareUI() {
        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        tabLayout = (TabLayout) findViewById(R.id.music_list_tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String[] newItems;
                switch (tab.getPosition()) {
                    case 0:
                        switch (mRetriever.getArtistStackSize()) {
                            case 2:
                                newItems = mRetriever.getAlbumListAsStringArray(
                                        mRetriever.getSelectedArtistAlbums());
                                break;
                            case 3:
                                newItems = mRetriever.getSongListAsStringArray(
                                        mRetriever.getSongListOfSelectedAlbumOfSelectedArtist());
                                break;
                            default:
                                newItems = mRetriever.getArtistListAsStringArray();
                                break;
                        }
                        break;
                    case 1:
                        switch (mRetriever.getAlbumStackSize()) {
                            case 2:
                                newItems = mRetriever.getSongListAsStringArray(
                                        mRetriever.getSongListOfSelectedAlbumOfAlbumList());
                                break;
                            default:
                                newItems = mRetriever.getAlbumListAsStringArray(
                                        mRetriever.getAlbums());
                                break;
                        }
                        break;
                    default:
                        newItems = mRetriever.getSongListAsStringArray(mRetriever.getSongs());
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

    /**
     * Callback Method for when the music retriever has been prepared.
     *
     * Here we could initialize the list and populate it with values
     */
    @Override
    public void onMusicRetrieverPrepared() {
    }
}