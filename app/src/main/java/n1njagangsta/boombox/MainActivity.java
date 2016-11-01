package n1njagangsta.boombox;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
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
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements MusicListFragment.OnItemSelectedListener,
                    MusicPlayerFragment.OnPlayerInteractionListener,
                    AudioManager.OnAudioFocusChangeListener{

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
                    musicRetriever = new MusicRetriever(this.getContentResolver());
                    musicRetriever.prepare();

                    mediaPlayer = new MediaPlayer();

                    prepareUI();
                    prepareListFragment();
                }
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

        musicRetriever = new MusicRetriever(this.getContentResolver());
        musicRetriever.prepare();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                quickFAB.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                if (musicPlayerFragment.isVisible()) {
                    musicPlayerFragment.changePlaybackButtonImage(false);
                }
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

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                                            AudioManager.AUDIOFOCUS_GAIN);

        prepareUI();
        prepareListFragment();

        musicPlayerFragment = new MusicPlayerFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                // here change Title + Subtitle Font size back to normal
                switch (tabLayout.getSelectedTabPosition()){
                    case 0:
                        switch (musicRetriever.getArtistStackSize()){
                            case 2:
                                musicRetriever.popAlbumListFromArtistStack();
                                musicListFragment.changeContents(
                                        musicRetriever.getArtistsAsStringArray());
                                showBackButton(false);
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
                        musicRetriever.popSongListFromAlbumStack();
                        musicListFragment.changeContents(
                                musicRetriever.getAlbumsAsStringArray());
                        showBackButton(false );
                        break;
                }
                break;

            case R.id.return_to_list_menu_btn:
                if(musicListFragment.isVisible()){
                    break;
                }
                quickFAB.show();
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
                        showBackButton(true);
                        break;
                    case 3:
                        bundleData.putStringArray(artistsKey,
                                musicRetriever.getSongListFromAlbumAsStringArray(
                                        musicRetriever.getSongListOfSelectedAlbumOfSelectedArtist()));
                        showBackButton(true);
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
                        showBackButton(true);
                        break;
                }


                bundleData.putInt("currentTabPosition", tabLayout.getSelectedTabPosition());
                musicListFragment.setArguments(bundleData);

                getFragmentManager().beginTransaction().
                        replace(R.id.fragment_container, musicListFragment).commit();
                break;

            case R.id.open_music_player_btn:
                if(musicPlayerFragment.isVisible()){
                    break;
                }
                Bundle bundle = new Bundle();
                bundle.putBoolean("mediaPlayerStatus", mediaPlayer.isPlaying());

                int songDuration = mediaPlayer.getDuration(),
                        currentPosition = mediaPlayer.getCurrentPosition();

                if(songDuration == -1){
                    bundle.putInt("songDuration", 0);
                }else {
                    bundle.putInt("songCurrentPosition", currentPosition);
                    bundle.putInt("songDuration", songDuration);
                }
                if(musicRetriever.getCurrentSelectedAlbum() != null){
                    musicPlayerFragment.setAlbumArtBitmap(
                            musicRetriever.getCurrentSelectedAlbum().getAlbumArt());
                }

                quickFAB.hide();
                tabLayout.setVisibility(View.GONE);

                showBackButton(false);

                musicPlayerFragment.setArguments(bundle);
                getFragmentManager().beginTransaction().
                        replace(R.id.fragment_container, musicPlayerFragment).commit();
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
                onSongSelect(musicRetriever.getSongs(),index);
        }
    }

    @Override
    public void onPlaybackClick() {
        if(mediaPlayer.isPlaying()){
            pausePlayback();
        } else {
            startPlayback();
        }
        musicPlayerFragment.changePlaybackButtonImage(mediaPlayer.isPlaying());
    }

    @Override
    public void onSeek(int seekValue) {
        pausePlayback();
        mediaPlayer.seekTo(seekValue);
    }

    private void startPlayback(){
        mediaPlayer.start();
        quickFAB.setImageResource(R.drawable.ic_pause_white_48dp);
    }

    private void pausePlayback(){
        mediaPlayer.pause();
        quickFAB.setImageResource(R.drawable.ic_play_arrow_white_48dp);
    }

    private void prepareListFragment(){
        Bundle songsData = new Bundle();
        songsData.putStringArray("songList", musicRetriever.getSongsAsStringArray());
        songsData.putStringArray("artistList", musicRetriever.getArtistsAsStringArray());
        songsData.putStringArray("albumList", musicRetriever.getAlbumsAsStringArray());
        songsData.putInt("currentTabPosition", tabLayout.getSelectedTabPosition());

        musicListFragment = new MusicListFragment();
        musicListFragment.setArguments(songsData);

        getFragmentManager().beginTransaction().
                add(R.id.fragment_container, musicListFragment).commit();
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
                                showBackButton(false);
                                break;
                            case 2:
                                musicListFragment.changeContents(
                                        musicRetriever.getAlbumListAsStringArray(
                                                musicRetriever.getSelectedArtistAlbums()));
                                showBackButton(true);
                                break;
                            case 3:
                                musicListFragment.changeContents(
                                        musicRetriever.getSongListFromAlbumAsStringArray(
                                                musicRetriever.getSongListOfSelectedAlbumOfSelectedArtist()));
                                showBackButton(true);
                                break;
                        }
                        break;
                    case 1:
                        switch (musicRetriever.getAlbumStackSize()){
                            case 1:
                                musicListFragment.changeContents(
                                        musicRetriever.getAlbumsAsStringArray());
                                showBackButton(false);
                                break;
                            case 2:
                                musicListFragment.changeContents(
                                        musicRetriever.getSongListFromAlbumAsStringArray(
                                                musicRetriever.getSongListOfSelectedAlbumOfAlbumList()));
                                showBackButton(true);
                                break;
                        }
                        break;
                    case 2:
                        musicListFragment.changeContents(musicRetriever.getSongsAsStringArray());
                        showBackButton(false);
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
                showBackButton(true);
                break;
            case 2:
                try{
                    musicRetriever.pushSongListToArtistStack(
                            musicRetriever.getSelectedArtistAlbums().get(artistItemIndex).getAlbumSongList());

                    musicRetriever.setCurrentSelectedAlbum(
                            musicRetriever.getSelectedArtistAlbums().get(artistItemIndex));

                    musicListFragment.changeContents(
                            musicRetriever.getSongListFromAlbumAsStringArray(
                                    musicRetriever.getSongListOfSelectedAlbumOfSelectedArtist()));
                } catch (Exception e){
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
            case 3:
                onSongSelect(musicRetriever.getSongListOfSelectedAlbumOfSelectedArtist(),
                        artistItemIndex);
                break;
        }
    }

    private void onAlbumsSelect(int albumItemIndex){
        switch (musicRetriever.getAlbumStackSize()){
            case 1:
                try {
                    musicRetriever.pushSongListToAlbumStack(
                            musicRetriever.getAlbums().get(albumItemIndex).getAlbumSongList());

                    musicRetriever.setCurrentSelectedAlbum(
                            musicRetriever.getAlbums().get(albumItemIndex));

                    musicListFragment.changeContents(
                            musicRetriever.getSongListFromAlbumAsStringArray(
                                    musicRetriever.getSongListOfSelectedAlbumOfAlbumList()));
                    showBackButton(true);
                } catch (Exception e){
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                onSongSelect(musicRetriever.getSongListOfSelectedAlbumOfAlbumList(),albumItemIndex);
                break;
        }
    }

    private void onSongSelect(List<Song> songs, int songItemIndex) {
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        mediaPlayer.reset();

        try {
            mediaPlayer.setDataSource(getApplicationContext(),
                    songs.get(songItemIndex).getURI());
            mediaPlayer.prepare();
            myToolbar.setTitle(songs.get(songItemIndex).getTitle());
            myToolbar.setSubtitle(songs.get(songItemIndex).getArtist());
            startPlayback();
        } catch (IOException ioe){
            Toast.makeText(MainActivity.this, ioe.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showBackButton(boolean showOrNot){
        if(getSupportActionBar() != null){
            if(showOrNot){
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            } else {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange){
            case AudioManager.AUDIOFOCUS_LOSS:
                if(mediaPlayer.isPlaying()){
                    pausePlayback();
                    if(musicPlayerFragment.isVisible()){
                        musicPlayerFragment.changePlaybackButtonImage(mediaPlayer.isPlaying());
                    }
                }
                break;
            // add more cases if you wish to approach more audio focus cases
        }
    }
}