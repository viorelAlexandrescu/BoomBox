package n1njagangsta.boombox;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class MusicPlayerFragment extends Fragment implements View.OnClickListener,
SeekBar.OnSeekBarChangeListener{

    private OnPlayerInteractionListener mCallback;

    private ImageButton playbackButton;

    private SeekBar seekBar;

    private TextView songDurationTextView, songCurrentPositionTextView;



    private boolean isMusicPlaying;

    private int songDuration, songCurrentPosition;

    public MusicPlayerFragment(){}

    public interface OnPlayerInteractionListener{
        void onPlaybackClick();
        void onSeek(int seekValue);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = (Activity) context;
        try{
            mCallback = (OnPlayerInteractionListener) activity;
        } catch (ClassCastException cce){
            throw new ClassCastException(activity.toString() +
                    " must implement OnPlayerInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        songDuration = getArguments().getInt("songDuration");
        songCurrentPosition = getArguments().getInt("songCurrentPosition");
        isMusicPlaying = getArguments().getBoolean("mediaPlayerStatus");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_music_player, container, false);

        seekBar = (SeekBar) rootView.findViewById(R.id.playback_seekBar);
        if(songDuration > 0){
            seekBar.setMax(songDuration);
            seekBar.setProgress(songCurrentPosition);
        }
        seekBar.setOnSeekBarChangeListener(this);

        playbackButton = (ImageButton) rootView.findViewById(R.id.play_pause_btn);
        if(isMusicPlaying){
            playbackButton.setImageResource(R.drawable.ic_pause_white_48dp);
        } else {
            playbackButton.setImageResource(R.drawable.ic_play_arrow_white_48dp);
        }
        playbackButton.setOnClickListener(this);

        songDurationTextView = (TextView) rootView.findViewById(R.id.songDurationTV);
        songDurationTextView.setText(Song.getTimeInMinutesAndSeconds(songDuration));

        songCurrentPositionTextView = (TextView) rootView.findViewById(R.id.currentSongPositionTV);
        songCurrentPositionTextView.setText(Song.getTimeInMinutesAndSeconds(songCurrentPosition));

        return rootView;
    }

    @Override
    public void onClick(View view) {
        mCallback.onPlaybackClick();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean isSeekFromUser) {
        if(isSeekFromUser){
            mCallback.onSeek(i);
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

    public void changePlaybackButtonImage(boolean mediaPlayerIsPlaying){
        if(mediaPlayerIsPlaying){
            playbackButton.setImageResource(R.drawable.ic_pause_white_48dp);
        } else {
            playbackButton.setImageResource(R.drawable.ic_play_arrow_white_48dp);
        }
    }
}
