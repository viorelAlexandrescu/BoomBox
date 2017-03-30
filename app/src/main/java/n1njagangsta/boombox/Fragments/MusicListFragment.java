package n1njagangsta.boombox.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

import n1njagangsta.boombox.Model.MediaPlayerInteraction;
import n1njagangsta.boombox.R;

public class MusicListFragment extends Fragment implements AdapterView.OnItemClickListener{
    private ListView elementsList;
    private ArrayAdapter<String> stringArrayAdapter;
    private OnItemSelectedListener mCallback;
    private MediaPlayerInteraction mediaPlayerInteraction;
    private FloatingActionButton quickFAB;

    public MusicListFragment(){
    }

    public interface OnItemSelectedListener{
        void onListItemPicked(int index);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = (Activity) context;
        try{
            mCallback = (OnItemSelectedListener) activity;
            mediaPlayerInteraction = (MediaPlayerInteraction) activity;
        } catch (ClassCastException cce){
            throw new ClassCastException(activity.toString() +
                    " must implement OnItemSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] itemsAsStringArray;

        if((itemsAsStringArray = getArguments().getStringArray("currentList")) != null){
            ArrayList<String> items = new ArrayList<>(Arrays.asList(itemsAsStringArray));

            stringArrayAdapter = new ArrayAdapter<>(getActivity().
                    getApplicationContext(), R.layout.simple_listview_item, items);
            stringArrayAdapter.setNotifyOnChange(true);
        } else {
            throw new NullPointerException("Null Items List");
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_music_list,container,false);
        elementsList = (ListView) rootView.findViewById(R.id.listview_items);
        quickFAB = (FloatingActionButton) rootView.findViewById(R.id.quickPlayFAB);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        elementsList.setAdapter(stringArrayAdapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)  {
        super.onViewCreated(view, savedInstanceState);
        elementsList.setOnItemClickListener(this);
        quickFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayerInteraction.onPlaybackClick();
                changeFABImage(mediaPlayerInteraction.isMusicPlaying());
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mCallback.onListItemPicked(i);
    }

    public void changeContents(String[] newData){
        if(!stringArrayAdapter.isEmpty() || stringArrayAdapter != null){
            stringArrayAdapter.clear();
            stringArrayAdapter.addAll(newData);
            elementsList.smoothScrollToPosition(0);
        }
    }

    public void changeFABImage(boolean isMusicPlaying){
        if(isMusicPlaying){
            quickFAB.setImageResource(R.drawable.ic_pause_white_48dp);
        } else {
            quickFAB.setImageResource(R.drawable.ic_play_arrow_white_48dp);
        }
    }

    public void changeFABVisibility(boolean showOrNot){
        if(showOrNot == true){
            quickFAB.show();
        } else {
            quickFAB.hide();
        }
    }
}
