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
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);

                musicListFragment.changeContents(musicRetriever.getAlbumsAsStringArray());
                // here change Title + Subtitle Font size back to normal
                break;

            case R.id.return_to_list_menu_btn:
                if(musicListFragment.isVisible()){
                    break;
                }
                quickFAB.show();
                tabLayout.setVisibility(View.VISIBLE);

                Bundle bundleData = new Bundle();
                bundleData.putStringArray("songList", musicRetriever.getSongsAsStringArray());
                bundleData.putStringArray("artistList", new String[]{"Artists"});
                if(musicRetriever.isViewingTempList()){
                    bundleData.putStringArray("albumList", musicRetriever.getTempSongListAsArray());
                } else {
                    bundleData.putStringArray("albumList", musicRetriever.getAlbumsAsStringArray());
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
                //artists tab
                Toast.makeText(MainActivity.this, "Artists", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                //albums tab
                onAlbumsSelect(index);
                break;
            case 2:
                onSongSelect(index);
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
        songsData.putStringArray("artistList", new String[]{"Artists"});
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
                        musicListFragment.changeContents(new String[]{"Artists"});
                        break;
                    case 1:
                        if(musicRetriever.isViewingTempList()){
                            musicListFragment.changeContents(musicRetriever.getTempSongListAsArray());
                            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                        }else {
                            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                            musicListFragment.changeContents(musicRetriever.getAlbumsAsStringArray());
                        }

                        break;
                    case 2:
                        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                        musicListFragment.changeContents(musicRetriever.getSongsAsStringArray());
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                /* when any of the Artists or Albums tab is unselected,
                    they add to their own stack what the user was viewing last time

                    REMEMBER WHEN SCROLLING THE ARTISTS OR ALBUMS TABS DO ADD A BACK BUTTON IN THE TOOLBAR
                 */
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                /* for the Artists and Albums tabs, we'll make a stack for each of them
                    and when the user scrolls through the data, when he comes back, he'll the
                    last thing he viewed
                */
            }
        });

        setSupportActionBar(myToolbar);
    }

    private void onAlbumsSelect(int albumItemIndex){
        if(musicRetriever.isViewingTempList()){
            onSongFromAlbumSelect(albumItemIndex);
        } else {
            String songs[] = musicRetriever.getSongsByAlbumId(
                    musicRetriever.getAlbums().get(albumItemIndex).getAlbumId());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            // here change Title + Subtitle Font size back a smaller size if needed
            musicListFragment.changeContents(songs);
        }
    }

    private void onSongSelect(int songItemIndex) {
        mediaPlayer.stop();
        mediaPlayer.reset();

        try {
            mediaPlayer.setDataSource(getApplicationContext(),
                    musicRetriever.getSongs().get(songItemIndex).getURI());
            mediaPlayer.prepare();
            startPlayback();
            myToolbar.setTitle(musicRetriever.getSongs().get(songItemIndex).getTitle());
            myToolbar.setSubtitle(musicRetriever.getSongs().get(songItemIndex).getArtist());
        } catch (IOException ioe){
            Toast.makeText(MainActivity.this, ioe.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void onSongFromAlbumSelect(int songItemIndex){
        mediaPlayer.stop();
        mediaPlayer.reset();

        try {
            mediaPlayer.setDataSource(getApplicationContext(),
                    musicRetriever.getTempSongList().get(songItemIndex).getURI());
            mediaPlayer.prepare();
            startPlayback();
            myToolbar.setTitle(musicRetriever.getTempSongList().get(songItemIndex).getTitle());
            myToolbar.setSubtitle(musicRetriever.getTempSongList().get(songItemIndex).getArtist());
        } catch (IOException ioe){
            Toast.makeText(MainActivity.this, ioe.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
