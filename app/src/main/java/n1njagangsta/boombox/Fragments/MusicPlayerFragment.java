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

import n1njagangsta.boombox.Model.Song;
import n1njagangsta.boombox.R;

public class MusicPlayerFragment extends Fragment {

    private OnPlayerInteractionListener mCallback;

    private ImageButton playbackButton, skipToPreviousButton, skipToNextButton;

    private SeekBar seekBar;

    private TextView songDurationTextView, songCurrentPositionTextView;

    private ImageView albumArtImageView;

    private Bitmap albumArtBitmap;

    private static Thread seekingThread;

    private Runnable seekingTask, uiSeekingTask;

    private int songDuration, songCurrentPosition;

    private boolean isMusicPlaying;

    private Object seekingThreadLock = new Object();

    public interface OnPlayerInteractionListener {
        void onPlaybackClick();

        void onSkipToPreviousClick();

        void onSkipToNextClick();

        void onSeek(int seekValue);

        int getSongPosition();

        int getSongDuration();

        boolean isMusicPlaying();

        boolean isSongSelected();
    }

    public MusicPlayerFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = (Activity) context;
        try {
            mCallback = (OnPlayerInteractionListener) activity;
        } catch (ClassCastException cce) {
            throw new ClassCastException(activity.toString() +
                    " must implement OnPlayerInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((songDuration = mCallback.getSongDuration()) < 0) {
            songDuration = 0;
        }

        if ((songCurrentPosition = mCallback.getSongPosition()) < 0) {
            songCurrentPosition = 0;
        }

        uiSeekingTask = new Runnable() {
            @Override
            public void run() {
                int playBackTime = mCallback.getSongPosition();
                setSeekBarPosition(playBackTime);
                setCurrentSongPositionText(playBackTime);
            }
        };

        seekingTask = new Runnable() {
            @Override
            public void run() {
                    isMusicPlaying = mCallback.isMusicPlaying();
                    if(isMusicPlaying){
//                        renderSeeking();
                    } else {
//                        waitForMusicOrVisibility();
                    }
                Thread.yield();
                System.out.println("seeking thread yielded");
            }
        };
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
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean isSeekFromUser) {
                if (isSeekFromUser) {
                    mCallback.onSeek(value);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                songCurrentPositionTextView.setText(
                        Song.getTimeInMinutesAndSeconds(seekBar.getProgress()));
            }
        });

        playbackButton = (ImageButton) rootView.findViewById(R.id.play_pause_btn);
        changePlaybackButtonImage(mCallback.isMusicPlaying());
        playbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onPlaybackClick();
            }
        });

        skipToPreviousButton = (ImageButton) rootView.findViewById(R.id.skip_to_previous_btn);
        skipToPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onSkipToPreviousClick();
            }
        });

        skipToNextButton = (ImageButton) rootView.findViewById(R.id.skip_to_next_btn);
        skipToNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onSkipToNextClick();
            }
        });

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

    private Thread getInstanceOfSeekingThread(){
        if (seekingThread == null){
            System.out.println("Thread is null. Creating new instance");
            seekingThread = new Thread(seekingTask, "Seeking Thread");
        }
        System.out.println("Returning thread singleton instance");
        return seekingThread;
    }

    public void changePlaybackButtonImage(boolean isMusicPlaying) {
        if (isMusicPlaying) {
            playbackButton.setImageResource(R.drawable.ic_pause_white_48dp);
        } else {
            playbackButton.setImageResource(R.drawable.ic_play_arrow_white_48dp);
        }
    }

    public void setAlbumArtBitmap(Bitmap newAlbumArtBitmap) {
        this.albumArtBitmap = newAlbumArtBitmap;
    }

    public void setCurrentSongPositionText(int newTimeValue) {
        songCurrentPositionTextView.setText(Song.getTimeInMinutesAndSeconds(newTimeValue));
    }

    public void setSongDurationTextView(int newSongDuration) {
        songDurationTextView.setText(Song.getTimeInMinutesAndSeconds(newSongDuration));
    }

    private void setSeekBarPosition(int newPosition) {
        seekBar.setProgress(newPosition);
    }
}

//TODO pause seeking thread when view goes away and when playback pauses
//TODO also implement so that the seeking is done right