package n1njagangsta.boombox;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import java.security.Permission;

public class MainActivity extends AppCompatActivity {

    private FrameLayout container;

    private static MediaPlayer mediaPlayer;

    private FloatingActionButton quickFAB;

    private boolean isPlayButtonClicked = false;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PackageManager.PERMISSION_GRANTED:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    MusicListFragment musicListFragment = new MusicListFragment();
                    musicListFragment.setActivity(this);

                    getFragmentManager().beginTransaction().
                            add(R.id.fragment_container, musicListFragment).commit();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
//        myToolbar.setElevation(0);
        setSupportActionBar(myToolbar);

        container = (FrameLayout) findViewById(R.id.fragment_container);

        mediaPlayer = new MediaPlayer();

        quickFAB = (FloatingActionButton) findViewById(R.id.quickPlayFAB);
        quickFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPlayButtonClicked){
                    mediaPlayer.pause();
                    quickFAB.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                } else {
                    mediaPlayer.start();
                    quickFAB.setImageResource(R.drawable.ic_pause_white_24dp);
                }
                isPlayButtonClicked = !isPlayButtonClicked;
            }
        });



        if(ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        } else {
            MusicListFragment musicListFragment = new MusicListFragment();
            musicListFragment.setActivity(this);

            getFragmentManager().beginTransaction().
                    add(R.id.fragment_container, musicListFragment).commit();
        }



    }

    public static MediaPlayer getMediaPlayer(){
        return mediaPlayer;
    }

}
