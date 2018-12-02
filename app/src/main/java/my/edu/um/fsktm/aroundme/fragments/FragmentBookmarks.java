package my.edu.um.fsktm.aroundme.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import my.edu.um.fsktm.aroundme.R;
import my.edu.um.fsktm.aroundme.adapters.CustomListAdapter;
import my.edu.um.fsktm.aroundme.objects.Bookmarks;

public class FragmentBookmarks extends Fragment {
    ListView list;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.activity_fragment_bookmarks, container, false);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        CustomListAdapter adapter = new CustomListAdapter(getActivity(), Bookmarks.FOODIMG,Bookmarks.LOCATIONS);
        list = (ListView) getActivity().findViewById(R.id.bookmark_list);
        list.setAdapter(adapter);

    }


}
