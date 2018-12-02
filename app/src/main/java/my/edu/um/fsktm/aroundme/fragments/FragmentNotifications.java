package my.edu.um.fsktm.aroundme.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import my.edu.um.fsktm.aroundme.Notifications;
import my.edu.um.fsktm.aroundme.R;
import my.edu.um.fsktm.aroundme.adapters.CustomNotiListAdapter;

public class FragmentNotifications extends Fragment {
    ListView list;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.activity_fragment_notifications, container, false);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
//        setListAdapter(new ArrayAdapter<String>(getActivity(),
//                android.R.layout.simple_list_item_activated_1,
//                Notifications.ALERTS));
        CustomNotiListAdapter adapter = new CustomNotiListAdapter(getActivity(), Notifications.ALERTS);
        list = (ListView) getActivity().findViewById(R.id.notification_list);
        list.setAdapter(adapter);


    }
}
