package n1njagangsta.boombox;

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

public class MusicPlayerFragment extends Fragment{

    private OnPlayerInteractionListener mCallback;

    private ImageButton playbackButton;

    private SeekBar seekBar;

    private TextView songDurationTextView, songCurrentPositionTextView;

    private ImageView albumArtImageView;

    private Bitmap albumArtBitmap;

    private int songDuration, songCurrentPosition;
    private boolean isMusicPlaying;

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

        isMusicPlaying = getArguments().getBoolean("mediaPlayerStatus");
        songDuration = getArguments().getInt("songDuration");
        songCurrentPosition = getArguments().getInt("songCurrentPosition");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_music_player, container, false);

        albumArtImageView = (ImageView)rootView.findViewById(R.id.musicPlayerFragment_albumArt_ImgView);
        albumArtImageView.setImageBitmap(albumArtBitmap);

        seekBar = (SeekBar) rootView.findViewById(R.id.playback_seekBar);
        if(songDuration > 0){
            seekBar.setMax(songDuration);
            seekBar.setProgress(songCurrentPosition);
            Toast.makeText(getActivity().getApplicationContext(), "Music Player Inflated", Toast.LENGTH_SHORT).show();
        }
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean isSeekFromUser) {
                if(isSeekFromUser){
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
        changePlaybackButtonImage(isMusicPlaying);
        playbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onPlaybackClick();
            }
        });

        songDurationTextView = (TextView) rootView.findViewById(R.id.songDurationTV);
        songDurationTextView.setText(Song.getTimeInMinutesAndSeconds(songDuration));

        songCurrentPositionTextView = (TextView) rootView.findViewById(R.id.currentSongPositionTV);
        songCurrentPositionTextView.setText(Song.getTimeInMinutesAndSeconds(songCurrentPosition));

        return rootView;
    }


    public void changePlaybackButtonImage(boolean isMusicPlaying){
        if(isMusicPlaying){
            playbackButton.setImageResource(R.drawable.ic_pause_white_48dp);
        } else {
            playbackButton.setImageResource(R.drawable.ic_play_arrow_white_48dp);
        }
    }

    public void setAlbumArtBitmap(Bitmap newAlbumArtBitmap){
        this.albumArtBitmap = newAlbumArtBitmap;
    }
}
