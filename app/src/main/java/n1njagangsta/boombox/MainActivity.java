package n1njagangsta.boombox;

import android.Manifest;
import android.content.pm.PackageManager;
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
        implements MusicListFragment.OnItemSelectedListener, MusicPlayerFragment.OnPlayerInteractionListener{

    private TabLayout tabLayout;

    private MusicListFragment musicListFragment;

    private MusicPlayerFragment musicPlayerFragment;

    private MusicRetriever musicRetriever;

    private FloatingActionButton quickFAB;

    private Toolbar myToolbar;

    private static MediaPlayer mediaPlayer;

    private boolean isPlayButtonClicked = false;

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
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        } else {

            musicRetriever = new MusicRetriever(this.getContentResolver());
            musicRetriever.prepare();

            mediaPlayer = new MediaPlayer();

            prepareUI();
            prepareListFragment();

            musicPlayerFragment = new MusicPlayerFragment();
        }
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
                                                musicRetriever.getSelectedArtistAlbum()));
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
                                        musicRetriever.getSelectedArtistAlbum()));
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
                bundle.putBoolean("mediaPlayerStatus",mediaPlayer.isPlaying());

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
            musicPlayerFragment.changePlaybackButtonImage(mediaPlayer.isPlaying());
        } else {
            startPlayback();
            musicPlayerFragment.changePlaybackButtonImage(mediaPlayer.isPlaying());
        }
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

    private void prepareUI(){
        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        tabLayout = (TabLayout) findViewById(R.id.music_list_tab_layout);
        quickFAB = (FloatingActionButton) findViewById(R.id.quickPlayFAB);

        quickFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPlayButtonClicked){
                    startPlayback();

                } else {
                    pausePlayback();
                }
                isPlayButtonClicked = !isPlayButtonClicked;
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
                                                musicRetriever.getSelectedArtistAlbum()));
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
                                musicRetriever.getSelectedArtistAlbum()));
                showBackButton(true);
                break;
            case 2:
                try{
                    musicRetriever.pushSongListToArtistStack(
                            musicRetriever.getSongsByAlbumId(
                                    musicRetriever.getSelectedArtistAlbum().get(artistItemIndex).getAlbumId()));
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
                            musicRetriever.getSongsByAlbumId(
                                    musicRetriever.getAlbums().get(albumItemIndex).getAlbumId()));
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
        mediaPlayer.stop();
        mediaPlayer.reset();

        try {
            mediaPlayer.setDataSource(getApplicationContext(),
                    songs.get(songItemIndex).getURI());
            mediaPlayer.prepare();
            startPlayback();
            myToolbar.setTitle(songs.get(songItemIndex).getTitle());
            myToolbar.setSubtitle(songs.get(songItemIndex).getArtist());
        } catch (IOException ioe){
            Toast.makeText(MainActivity.this, ioe.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showBackButton(boolean showOrNot){
        if(getSupportActionBar() != null){
            if(showOrNot == true){
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            } else {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
    }

}
