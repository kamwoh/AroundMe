package my.edu.um.fsktm.aroundme.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import my.edu.um.fsktm.aroundme.ArticleViewActivity;
import my.edu.um.fsktm.aroundme.R;
import my.edu.um.fsktm.aroundme.adapters.CustomListAdapter;
import my.edu.um.fsktm.aroundme.objects.SimpleArticle;
import my.edu.um.fsktm.aroundme.objects.User;

public class FragmentBookmarks extends Fragment {
    ListView list;
    ArrayList<SimpleArticle> simpleArticles;

    private ProgressBar progressBar;
    private TextView indicator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_fragment_bookmarks, container, false);

        progressBar = v.findViewById(R.id.progress_bar);
        indicator = v.findViewById(R.id.no_item_text_view);
        list = v.findViewById(R.id.bookmark_list);

        progressBar.setVisibility(View.VISIBLE);
        indicator.setVisibility(View.INVISIBLE);
        list.setVisibility(View.INVISIBLE);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final User user = User.currentUser;
        final HashMap<String, String> storeTag = new HashMap<>(); // gg design

        simpleArticles = new ArrayList<>();
        final CustomListAdapter adapter = new CustomListAdapter(getActivity(), simpleArticles);

        FirebaseDatabase.getInstance()
                .getReference()
                .child("simple_articles")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int count = 0;
                        for (DataSnapshot tagData : dataSnapshot.getChildren()) {
                            for (DataSnapshot articleData : tagData.getChildren()) {
                                SimpleArticle simpleArticle = articleData.getValue(SimpleArticle.class);

                                if (simpleArticle != null) {
                                    for (String articleId : user.bookmarks) {
                                        if (articleId.equals(simpleArticle.getArticleId())) {
                                            storeTag.put(articleId, tagData.getKey());
                                            count += 1;
                                            simpleArticles.add(simpleArticle);
                                            break;
                                        }
                                    }
                                }

                                if (count == user.bookmarks.size())
                                    break;
                            }

                            if (count == user.bookmarks.size())
                                break;
                        }

                        adapter.notifyDataSetChanged();

                        progressBar.setVisibility(View.INVISIBLE);

                        if (adapter.getCount() == 0) {
                            indicator.setVisibility(View.VISIBLE);
                        } else {
                            list.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SimpleArticle simpleArticle = simpleArticles.get(position);

                if (simpleArticle != null) {
                    Intent intent = new Intent(getActivity(), ArticleViewActivity.class);
                    intent.putExtra("tag", storeTag.get(simpleArticle.getArticleId()));
                    intent.putExtra("articleId", simpleArticle.getArticleId());

                    startActivityForResult(intent, 0);
                }
            }
        });
    }


}
