package my.edu.um.fsktm.aroundme.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import my.edu.um.fsktm.aroundme.ArticleViewActivity;
import my.edu.um.fsktm.aroundme.R;
import my.edu.um.fsktm.aroundme.adapters.CustomNotiListAdapter;
import my.edu.um.fsktm.aroundme.objects.Comment;
import my.edu.um.fsktm.aroundme.objects.User;

public class FragmentNotifications extends Fragment {
    ListView list;
    CustomNotiListAdapter adapter;
    private ProgressBar progressBar;
    private TextView indicator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.activity_fragment_notifications, container, false);

        progressBar = v.findViewById(R.id.progress_bar);
        indicator = v.findViewById(R.id.no_item_text_view);
        list = v.findViewById(R.id.notification_list);

        progressBar.setVisibility(View.VISIBLE);
        indicator.setVisibility(View.INVISIBLE);
        list.setVisibility(View.INVISIBLE);

        Log.d("FragmentNotifications", "onCreateView");

//        progressBar.setVisibility(View.INVISIBLE);
//
//        if (adapter.getCount() == 0) {
//            indicator.setVisibility(View.VISIBLE);
//        } else {
//            list.setVisibility(View.VISIBLE);
//        }

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        setListAdapter(new ArrayAdapter<String>(getActivity(),
//                android.R.layout.simple_list_item_activated_1,
//                Notifications.ALERTS));
        if (User.currentUser.getNotifications().size() == 0) {
            indicator.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            list.setVisibility(View.INVISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            indicator.setVisibility(View.INVISIBLE);
            list.setVisibility(View.VISIBLE);
        }

        adapter = new CustomNotiListAdapter(getActivity(), User.currentUser.getNotifications());
        list = getActivity().findViewById(R.id.notification_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int size = User.currentUser.getNotifications().size();
                Comment cm = User.currentUser.getNotifications().get(size - 1 - position);

                if (cm != null) {
                    Intent intent = new Intent(getActivity(), ArticleViewActivity.class);
                    intent.putExtra("tag", cm.getTag());
                    intent.putExtra("articleId", cm.getArticleId());

                    startActivityForResult(intent, 0);
                }
            }
        });
    }

    public void notifyChanged() {

        Log.d("FragmentNotification", "get notifications " + User.currentUser.getNotifications());
        adapter.notifyDataSetChanged();
    }
}
