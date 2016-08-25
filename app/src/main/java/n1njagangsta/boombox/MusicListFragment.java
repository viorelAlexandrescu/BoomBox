package n1njagangsta.boombox;


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


public class MusicListFragment extends Fragment implements AdapterView.OnItemClickListener{
    private ListView elementsList;
    private ArrayAdapter<String> stringArrayAdapter;
    OnItemSelectedListener mCallback;

    public MusicListFragment(){
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = (Activity) context;
        try{
            mCallback = (OnItemSelectedListener) activity;
        } catch (ClassCastException cce){
            throw new ClassCastException(activity.toString() + " must implement OnItemSelectedListener");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] values = getArguments().getStringArray("songList");
        ArrayList<String> items = new ArrayList<>();

        if(values != null){
           items.addAll(Arrays.asList(values));
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Song list empty on list initialization", Toast.LENGTH_LONG).show();
        }

        stringArrayAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, items);
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

    public interface OnItemSelectedListener{
        void OnListItemPicked(int index);
    }

    public void changeContents(String[] newData){
        if(!stringArrayAdapter.isEmpty() || stringArrayAdapter != null){
            stringArrayAdapter.clear();
            stringArrayAdapter.addAll(newData);
        }
    }

}
