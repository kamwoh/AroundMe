package my.edu.um.fsktm.aroundme;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class FragmentArticles extends Fragment {
    ListView list;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.activity_fragment_articles, container, false);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        CustomArticleListAdapter adapter = new CustomArticleListAdapter(getActivity(), Articles.LOCATIONIMG,Articles.COMMENTS);
        list = (ListView) getActivity().findViewById(R.id.article_list);
        list.setAdapter(adapter);

    }
}
