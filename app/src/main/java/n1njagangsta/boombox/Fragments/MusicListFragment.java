package n1njagangsta.boombox.Fragments;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import n1njagangsta.boombox.R;


public class MusicListFragment extends Fragment implements AdapterView.OnItemClickListener{
    private ListView elementsList;
    private ArrayAdapter<String> stringArrayAdapter;
    private OnItemSelectedListener mCallback;

    public MusicListFragment(){
    }

    public interface OnItemSelectedListener{
        void OnListItemPicked(int index);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = (Activity) context;
        try{
            mCallback = (OnItemSelectedListener) activity;
        } catch (ClassCastException cce){
            throw new ClassCastException(activity.toString() +
                    " must implement OnItemSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] songs = getArguments().getStringArray("songList"),
                 artists = getArguments().getStringArray("artistList"),
                 albums = getArguments().getStringArray("albumList");

        int currentTabPosition = getArguments().getInt("currentTabPosition");

        ArrayList<String> items = new ArrayList<>();
        switch (currentTabPosition){
            case 0:
                if(artists != null){
                    items.addAll(Arrays.asList(artists));
                } else {
                    Toast.makeText(getContext(), "Artist list empty on list initialization", Toast.LENGTH_SHORT).show();
                }
                break;
            case 1:
                if(albums != null){
                    items.addAll(Arrays.asList(albums));
                }else {
                    Toast.makeText(getContext(), "Album list empty on list initialization", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                if(songs != null){
                    items.addAll(Arrays.asList(songs));
                } else {
                    Toast.makeText(getContext(), "Song list empty on list initialization", Toast.LENGTH_LONG).show();
                }
                break;
        }

        stringArrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.simple_listview_item, items);
        stringArrayAdapter.setNotifyOnChange(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_music_list,container,false);
        elementsList = (ListView) rootView.findViewById(R.id.listview_items);
        elementsList.setAdapter(stringArrayAdapter);
        elementsList.setOnItemClickListener(this);
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mCallback.OnListItemPicked(i);
    }

    public void changeContents(String[] newData){
        if(!stringArrayAdapter.isEmpty() || stringArrayAdapter != null){
            stringArrayAdapter.clear();
            stringArrayAdapter.addAll(newData);
            elementsList.smoothScrollToPosition(0);
        }
    }

}
