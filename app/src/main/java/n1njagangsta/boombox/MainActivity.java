package n1njagangsta.boombox;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements MusicListFragment.OnItemSelectedListener{

    private FrameLayout container;

    private TabLayout tabLayout;

    private MusicListFragment musicListFragment;

    private MusicRetriever musicRetriever;

    private FloatingActionButton quickFAB;

    private Toolbar myToolbar;

    private Item[] songItems;

    private static MediaPlayer mediaPlayer;

    private boolean isPlayButtonClicked = false, isUserNotInSongList = true;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PackageManager.PERMISSION_GRANTED:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    prepareListFragment(musicRetriever);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        musicRetriever = new MusicRetriever(this.getContentResolver());
        musicRetriever.prepare();
        songItems = musicRetriever.getSongsAsItems();

        mediaPlayer = new MediaPlayer();

        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        container = (FrameLayout) findViewById(R.id.fragment_container);

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

        tabLayout = (TabLayout) findViewById(R.id.music_list_tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0:
                        musicListFragment.changeContents(new String[]{"Artists"});
                        break;
                    case 1:
                        musicListFragment.changeContents(new String[]{"Albums"});
                        break;
                    case 2:
                        musicListFragment.changeContents(musicRetriever.getSongsAsStringArray());

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

        if(ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        } else {
            prepareListFragment(musicRetriever);
        }
    }

    private void prepareListFragment(MusicRetriever musicRetriever){
        Bundle songsData = new Bundle();
        songsData.putStringArray("songList", musicRetriever.getSongsAsStringArray());

        musicListFragment = new MusicListFragment();
        musicListFragment.setArguments(songsData);

        getFragmentManager().beginTransaction().
                add(R.id.fragment_container, musicListFragment).commit();
    }

    public void onSongSelect(int songItemIndex) {
        mediaPlayer.stop();
        mediaPlayer.reset();

        try {
            mediaPlayer.setDataSource(getApplicationContext(), songItems[songItemIndex].getURI());
            mediaPlayer.prepare();
            startPlayback();
            myToolbar.setTitle(songItems[songItemIndex].getTitle());
            myToolbar.setSubtitle(songItems[songItemIndex].getArtist());
        } catch (IOException ioe){
            Toast.makeText(MainActivity.this, ioe.getMessage(), Toast.LENGTH_SHORT).show();
        }

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
                Toast.makeText(MainActivity.this, "Albums", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                onSongSelect(index);
        }

    }

    private void startPlayback(){
        mediaPlayer.start();
        quickFAB.setImageResource(R.drawable.ic_pause_white_24dp);
    }

    private void pausePlayback(){
        mediaPlayer.pause();
        quickFAB.setImageResource(R.drawable.ic_play_arrow_white_24dp);
    }
}
