package n1njagangsta.boombox.Fragments;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import n1njagangsta.boombox.Model.MediaPlayerInteraction;
import n1njagangsta.boombox.Model.Song;
import n1njagangsta.boombox.R;

public class MusicPlayerFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

    private OnPlayerViewInteractionListener mCallback;

    private MediaPlayerInteraction mediaPlayerInteraction;

    private ImageButton playbackButton, skipToPreviousButton, skipToNextButton;

    private SeekBar seekBar;

    private TextView songDurationTextView, songCurrentPositionTextView;

    private ImageView albumArtImageView;

    private Bitmap albumArtBitmap;

    private static Thread seekingThread;

    private Runnable seekingTask, uiSeekingTask;

    private int songDuration, songCurrentPosition;

    private boolean isMusicPlaying;

    public interface OnPlayerViewInteractionListener {

        void onSkipToPreviousClick();

        void onSkipToNextClick();

        void onSeek(int seekValue);

        int getSongDuration();
    }

    public MusicPlayerFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = (Activity) context;
        try {
            mCallback = (OnPlayerViewInteractionListener) activity;
            mediaPlayerInteraction = (MediaPlayerInteraction) activity;
        } catch (ClassCastException cce) {
            throw new ClassCastException(activity.toString() +
                    " must implement OnPlayerInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //todo here place callback results from main activity for song position and playback status
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_music_player, container, false);

        albumArtImageView = (ImageView) rootView.findViewById(R.id.musicPlayerFragment_albumArt_ImgView);
        albumArtImageView.setImageBitmap(albumArtBitmap);

        seekBar = (SeekBar) rootView.findViewById(R.id.playback_seekBar);
        if (songDuration > 0) {
            seekBar.setMax(songDuration);
            seekBar.setProgress(songCurrentPosition);
        }


        playbackButton = (ImageButton) rootView.findViewById(R.id.play_pause_btn);
        changePlaybackButtonImage(isMusicPlaying);

        skipToPreviousButton = (ImageButton) rootView.findViewById(R.id.skip_to_previous_btn);
        skipToNextButton = (ImageButton) rootView.findViewById(R.id.skip_to_next_btn);

        songDurationTextView = (TextView) rootView.findViewById(R.id.songDurationTV);
        songDurationTextView.setText(Song.getTimeInMinutesAndSeconds(songDuration));

        songCurrentPositionTextView = (TextView) rootView.findViewById(R.id.currentSongPositionTV);
        songCurrentPositionTextView.setText(Song.getTimeInMinutesAndSeconds(songCurrentPosition));

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
//      TODO work on this... make the music player layout work!
//        seekingThread = getInstanceOfSeekingThread();
//        if(mCallback.isSongSelected()){
//            if(mCallback.isMusicPlaying()){
//                if(seekingThread.getState().compareTo(Thread.State.NEW) == 0){
//                    seekingThread.start();
//                } else {
//                    notifyFlag();
//                }
//            }
//        }
    }

//    private void renderSeeking(){
//        synchronized (seekingThreadLock){
//            while (mCallback.isMusicPlaying() && isVisible()) {
//                try {
//                    getActivity().runOnUiThread(uiSeekingTask);
//                    Thread.sleep(1000);
//                } catch (InterruptedException ie){
//                    System.out.println("Either music or visibility has gone away");
//                }
//            }
//        }
//    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        skipToPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onSkipToPreviousClick();
            }
        });

        playbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayerInteraction.onPlaybackClick();
                changePlaybackButtonImage(mediaPlayerInteraction.isMusicPlaying());
            }
        });

        skipToNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onSkipToNextClick();
            }
        });

        seekBar.setOnSeekBarChangeListener(this);

    }

    public void changePlaybackButtonImage(boolean isMusicPlaying) {
        if (isMusicPlaying) {
            playbackButton.setImageResource(R.drawable.ic_pause_white_48dp);
        } else {
            playbackButton.setImageResource(R.drawable.ic_play_arrow_white_48dp);
        }
    }

    public void resetPlayerInfo(){
        seekBar.setMax(mCallback.getSongDuration());//set seek bar max value
        //reset current position text view value
        songCurrentPositionTextView.setText(Song.getTimeInMinutesAndSeconds(0));
        seekBar.setProgress(0);// reset seek bar cursor position
        //reset song duration text view value
        songDurationTextView.setText(
                Song.getTimeInMinutesAndSeconds(mCallback.getSongDuration()));
    }

    public void setAlbumArtBitmap(Bitmap newAlbumArtBitmap) {
        this.albumArtBitmap = newAlbumArtBitmap;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int value, boolean isSeekFromUser) {
        if (isSeekFromUser) {
            mCallback.onSeek(value);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    // not setting text here due to possible performance issues and memory leaks
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        songCurrentPositionTextView.setText(
                Song.getTimeInMinutesAndSeconds(seekBar.getProgress()));
    }

    private Thread getInstanceOfSeekingThread(){
        if (seekingThread == null){
            System.out.println("Thread is null. Creating new instance");
            seekingThread = new Thread(seekingTask, "Seeking Thread");
        }
        System.out.println("Returning thread singleton instance");
        return seekingThread;
    }
}

//TODO pause seeking thread when view goes away and when playback pauses
//TODO also implement so that the seeking is done right