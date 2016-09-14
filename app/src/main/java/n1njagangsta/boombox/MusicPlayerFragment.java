package n1njagangsta.boombox;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class MusicPlayerFragment extends Fragment implements View.OnClickListener{

    private OnPlayerInteractionListener mCallback;

    private ImageButton playbackButton;

    private boolean isMusicPlaying;

    public MusicPlayerFragment(){}

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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_music_player, container, false);
        playbackButton = (ImageButton) rootView.findViewById(R.id.play_pause_btn);
        if(isMusicPlaying){
            playbackButton.setImageResource(R.drawable.ic_pause_white_24dp);
        } else {
            playbackButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        }

        playbackButton.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View view) {
        mCallback.onPlaybackClick();
    }


    public interface OnPlayerInteractionListener{
        void onPlaybackClick();
    }

    public void changePlaybackButtonImage(boolean mediaPlayerIsPlaying){
        if(mediaPlayerIsPlaying){
            playbackButton.setImageResource(R.drawable.ic_pause_white_24dp);
        } else {
            playbackButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        }
    }

}
