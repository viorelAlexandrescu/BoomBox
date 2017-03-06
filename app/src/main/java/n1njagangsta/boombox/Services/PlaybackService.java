package n1njagangsta.boombox.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import n1njagangsta.boombox.Model.MediaPlayerInteraction;
import n1njagangsta.boombox.Model.Song;
import n1njagangsta.boombox.R;

/**
 * Official BoomBox Android Playback Service
 */

public class PlaybackService extends Service
        implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnInfoListener,
        MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener {
    //todo rearrange interface method implementations according to the above list
    AudioManager mAudioManager;
    MediaPlayer mPlayer;
    AudioManager.OnAudioFocusChangeListener mAfChangeListener;
    int currentSongIndex;
    ArrayList<Song> currentPlaylist;
//    boolean isShuffling = false;

    private static final String DEFAULT_ACTION = "n1njagangsta.boombox.action.";
    public static final String ACTION_START_PLAYBACK = DEFAULT_ACTION + "START_PLAYBACK";
    public static final String ACTION_PLAY = DEFAULT_ACTION + "PLAY";
    public static final String ACTION_PAUSE = DEFAULT_ACTION + "PAUSE";
    public static final String ACTION_PLAY_PREVIOUS_SONG = DEFAULT_ACTION + "SKIP_PREV";
    public static final String ACTION_PLAY_NEXT_SONG = DEFAULT_ACTION + "SKIP_NEXT";
    public static final String ACTION_SEEK_TO = DEFAULT_ACTION + "SEEK_TO_POSITION";
    static final String TAG = "BoomBox Music Service";
    static final int NOTIFY_ID = 2014;

    @Override
    public void onCreate() {
        super.onCreate();

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mPlayer = new MediaPlayer();
        mAfChangeListener = this;

        initializeMediaPlayer();
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        //Invoked when the audio focus of the system is updated.
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mPlayer == null) initializeMediaPlayer();
                else changePlaybackStatus();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mPlayer.isPlaying()) mPlayer.stop();
                mPlayer.release();
                mPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                changePlaybackStatus();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mPlayer.isPlaying()) mPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case ACTION_START_PLAYBACK:
                if(intent.getParcelableExtra(String.valueOf(R.string.Intent_New_Playlist_Key)) != null){
                    currentPlaylist = intent.getParcelableExtra(String.valueOf(R.string.Intent_New_Playlist_Key));
                }
                playNewSong(currentPlaylist.get(intent.getIntExtra(String.valueOf(R.string.Intent_New_Song_Index_Key),0)));
                break;
            case ACTION_PLAY:
                changePlaybackStatus();
                break;
            case ACTION_PAUSE:
                changePlaybackStatus();
                break;
            case ACTION_PLAY_PREVIOUS_SONG:
                playPreviousSong();
                break;
            case ACTION_PLAY_NEXT_SONG:
                playNextSong();
                break;
            case ACTION_SEEK_TO:
                mPlayer.pause();
                mPlayer.seekTo(intent.getIntExtra(String.valueOf(R.string.Intent_Seek_To_Integer_Value),0));
                break;
        }
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        currentPlaylist = null;
        mAudioManager = null;
        mAfChangeListener = null;

        if (mPlayer.isPlaying()) {
            mPlayer.stop();
        }
        mPlayer.release();
        abandonAudioFocus();

        stopForeground(true);
        Toast.makeText(getApplicationContext(), "Service Destroyed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mPlayer, int i) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
    }

    @Override
    public boolean onInfo(MediaPlayer mPlayer, int i, int i1) {
        //Invoked to communicate some info.
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mPlayer) {
        changePlaybackStatus();
    }

    @Override
    public void onCompletion(MediaPlayer mPlayer) {
//        if(isShuffling){
//            Random randomValue = new Random();
//            currentSongIndex = randomValue.nextInt(currentPlaylist.size());
//        }
//        else{
        currentSongIndex++;
        if (currentSongIndex >= currentPlaylist.size())
            currentSongIndex = 0;
        playNewSong(currentPlaylist.get(currentSongIndex));
    }

    @Override
    public boolean onError(MediaPlayer mPlayer, int errorCode, int extra) {
        //Invoked when there has been an error during an asynchronous operation.
        boolean isErrorHandled = false; // change value to true if the error has been handled
        switch (errorCode) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return isErrorHandled;
    }

    @Override
    public void onPrepared(MediaPlayer mPlayer) {
        changePlaybackStatus();
    }

    private boolean requestAudioFocus() {
        boolean isFocusGranted = false;

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            isFocusGranted = true;
            Toast.makeText(this, "Audio Focus Gained", Toast.LENGTH_SHORT).show();
        }
        //Could not gain focus
        return isFocusGranted;
    }

    private boolean abandonAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager.abandonAudioFocus(this);
    }

    void playPreviousSong() {
        currentSongIndex--;
        if (currentSongIndex < 0)
            currentSongIndex = currentPlaylist.size() - 1;
        playNewSong(currentPlaylist.get(currentSongIndex));
    }

    void playNewSong(Song newSong) {
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
        }
        mPlayer.reset();

//        Song currentSong = currentPlaylist.get(currentSongIndex);
        try {
            mPlayer.setDataSource(getApplicationContext(), newSong.getUri());
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        mPlayer.prepareAsync();
    }

    void playNextSong(){
        currentSongIndex++;
        if (currentSongIndex > currentPlaylist.size() - 1)
            currentSongIndex = 0;
        playNewSong(currentPlaylist.get(currentSongIndex));
    }

    void changePlaybackStatus(){
        if(mPlayer.isPlaying()){
            mPlayer.pause();
        } else {
            mPlayer.start();
        }
    }

    void initializeMediaPlayer() {
        mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);

        mPlayer.setOnBufferingUpdateListener(this);
        mPlayer.setOnSeekCompleteListener(this);
        mPlayer.setOnInfoListener(this);
    }

    public void setPlaylist(ArrayList<Song> newPlaylist) {
        this.currentPlaylist = newPlaylist;
    }

    public ArrayList<Song> getPlaylist() {
        return this.currentPlaylist;
    }

    public int getPlaylistSize() {
        return this.currentPlaylist.size();
    }

    public int getCurrentSongIndex() {
        return currentSongIndex;
    }

    public void setCurrentSongIndex(int newCurrentSongIndex) {
        this.currentSongIndex = newCurrentSongIndex;
    }
}
