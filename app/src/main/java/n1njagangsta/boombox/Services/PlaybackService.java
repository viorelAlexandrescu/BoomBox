package n1njagangsta.boombox.Services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

import n1njagangsta.boombox.Activities.MainActivity;
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
    boolean isShuffling = false;
    //
//    private static final String DEFAULT_ACTION = "n1njagangsta.boombox.action.";
//    public static final String ACTION_START_PLAYBACK = DEFAULT_ACTION + "START_PLAYBACK";
//    public static final String ACTION_PLAY = DEFAULT_ACTION + "PLAY";
//    public static final String ACTION_PAUSE = DEFAULT_ACTION + "PAUSE";
//    public static final String ACTION_PLAY_PREVIOUS_SONG = DEFAULT_ACTION + "SKIP_PREV";
//    public static final String ACTION_PLAY_NEXT_SONG = DEFAULT_ACTION + "SKIP_NEXT";
//    public static final String ACTION_SEEK_TO = DEFAULT_ACTION + "SEEK_TO_POSITION";
    static final String TAG = "BoomBox Music Service";
    static final int NOTIFY_ID = 2014;

    ServiceBinder serviceBinder = new ServiceBinder();

    public class ServiceBinder extends Binder {
        public void switchPlaybackStatus() {
            changePlaybackStatus();
        }

        public void skipToPreviousSong() {
            currentSongIndex--;
            if (currentSongIndex < 0)
                currentSongIndex = currentPlaylist.size() - 1;
            play(currentPlaylist.get(currentSongIndex));
        }

        public void skipToNextSong() {
            currentSongIndex++;
            if (currentSongIndex >= currentPlaylist.size() - 1)
                currentSongIndex = 0;
            play(currentPlaylist.get(currentSongIndex));
        }

        public void preparePlaybackMedia(ArrayList<Song> newSongList, int newSongIndex) {
            if (newSongList != null) {
                currentPlaylist = newSongList;
            }
            currentSongIndex = newSongIndex;
        }

        public void skipTo(int newValue) {
            mPlayer.pause();
            mPlayer.seekTo(newValue);
        }

        public void setShuffle(boolean newShufflingValue) {
            isShuffling = newShufflingValue;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mAfChangeListener = this;

        initializeMediaPlayer();
    }

    /**
     * Callback for requesting audio focus, or when an event occurs and focus changes
     *
     * When focus is gained indefinitely after a request, start playback of the current song
     * @param focusState
     */
    @Override
    public void onAudioFocusChange(int focusState) {
        //Invoked when the audio focus of the system is updated.
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                play(currentPlaylist.get(currentSongIndex));
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

    /**
     * Method callback for when a client asks to bind to the music service.
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("Test")
                .setContentText("Service Test")
                .setSmallIcon(R.drawable.ic_play_arrow_white_48dp)
                .setContentIntent(pendingIntent)
                .setTicker("PLM")
                .build();

        startForeground(2014, notification);

        return this.serviceBinder;
    }

    /**
     * Method callback for when starting a service
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    /**
     * Method callback for when stopping the service
     */
    @Override
    public void onDestroy() {
        currentPlaylist = null;
        mAudioManager = null;
        mAfChangeListener = null;

        if (mPlayer.isPlaying()) {
            mPlayer.stop();
        }
        mPlayer.reset();
        mPlayer.release();
        mPlayer = null;

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

    /**
     * Callback after seeking to a certain point.
     * @param mediaPlayer
     */
    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        mPlayer.start();
    }

    /**
     * Callback after a song playback has ended
     * @param mediaPlayer
     */
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (isShuffling) {
            Random randomValue = new Random();
            currentSongIndex = randomValue.nextInt(currentPlaylist.size());
        } else {
            currentSongIndex++;
            if (currentSongIndex >= currentPlaylist.size() - 1) {
                mPlayer.stop();
                return;
            }
        }
        requestAudioFocus();
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

    /**
     * Callback after a song has been prepared for playback
     * @param mPlayer
     */
    @Override
    public void onPrepared(MediaPlayer mPlayer) {
        changePlaybackStatus();
    }

    /**
     * Method for requesting audio focus
     * @return int value for AudioManager class constants
     */
    private int requestAudioFocus() {
        return mAudioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
        );
    }

    private boolean abandonAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager.abandonAudioFocus(this);
    }

    void play(Song newSong) {
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
        }
        mPlayer.reset();
        try {
            mPlayer.setDataSource(this, newSong.getUri());
            mPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
    }

    void changePlaybackStatus() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();
        }
    }

    void initializeMediaPlayer() {
        mPlayer = new MediaPlayer();

        mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);

        mPlayer.setOnBufferingUpdateListener(this);
        mPlayer.setOnSeekCompleteListener(this);
        mPlayer.setOnInfoListener(this);
    }
}
