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

    private FloatingActionButton quickFAB;

    private Item[] songItems;


    private static MediaPlayer mediaPlayer;

    private boolean isPlayButtonClicked = false;





    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PackageManager.PERMISSION_GRANTED:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    prepareListFragment();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        MusicRetriever musicRetriever = new MusicRetriever(this.getContentResolver());
        musicRetriever.prepare();
        songItems = musicRetriever.getSongsAsItems();

        mediaPlayer = new MediaPlayer();

        container = (FrameLayout) findViewById(R.id.fragment_container);

        quickFAB = (FloatingActionButton) findViewById(R.id.quickPlayFAB);

        if(isPlayButtonClicked){
            quickFAB.setImageResource(R.drawable.ic_pause_white_24dp);
        } else {
            quickFAB.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        }

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


        if(ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        } else {
            prepareListFragment();
        }



    }

    private void prepareListFragment(){
        MusicRetriever musicRetriever = new MusicRetriever(this.getContentResolver());
        musicRetriever.prepare();

        Bundle songsData = new Bundle();
        songsData.putStringArray("songList", musicRetriever.getSongsAsStringArray());

        musicListFragment = new MusicListFragment();
        musicListFragment.setArguments(songsData);

        getFragmentManager().beginTransaction().
                add(R.id.fragment_container, musicListFragment).commit();
    }



    public void onSongSelect(int songItemIndex) {
        Uri songURI = songItems[songItemIndex].getURI();

        mediaPlayer.stop();
        mediaPlayer.reset();

        try {
            mediaPlayer.setDataSource(getApplicationContext(), songURI);
            mediaPlayer.prepare();
            startPlayback();
        } catch (IOException ioe){
            Toast.makeText(MainActivity.this, ioe.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void OnListItemPicked(int index) {
        onSongSelect(index);
        Toast.makeText(MainActivity.this, songItems[index].getTitle(), Toast.LENGTH_SHORT).show();
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
