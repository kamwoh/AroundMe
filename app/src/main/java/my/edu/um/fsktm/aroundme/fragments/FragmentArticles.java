package my.edu.um.fsktm.aroundme.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import my.edu.um.fsktm.aroundme.ArticleViewActivity;
import my.edu.um.fsktm.aroundme.R;
import my.edu.um.fsktm.aroundme.adapters.CustomArticleListAdapter;
import my.edu.um.fsktm.aroundme.objects.Article;
import my.edu.um.fsktm.aroundme.objects.User;

public class FragmentArticles extends Fragment {
    ListView list;
    ArrayList<Article> articles;

    private ProgressBar progressBar;
    private TextView indicator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_fragment_articles, container, false);

        progressBar = v.findViewById(R.id.progress_bar);
        indicator = v.findViewById(R.id.no_item_text_view);
        list = v.findViewById(R.id.article_list);

        progressBar.setVisibility(View.VISIBLE);
        indicator.setVisibility(View.INVISIBLE);
        list.setVisibility(View.INVISIBLE);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final User user = User.currentUser;

        articles = new ArrayList<>();
        final CustomArticleListAdapter adapter = new CustomArticleListAdapter(getActivity(), articles);

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference()
                .child("articles");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (articles == null)
                    articles = new ArrayList<>();

                articles.clear();

                Log.d("FragmentArticles", "loaded: " + dataSnapshot.toString());

                int count = 0;
                for (DataSnapshot tagData : dataSnapshot.getChildren()) {
                    for (DataSnapshot articleData : tagData.getChildren()) {
                        if (articleData.getValue() == null)
                            continue;

                        Article article = new Article((HashMap) articleData.getValue());

                        for (String articleId : user.posts) {
                            if (articleId.equals(article.articleId)) {
                                Log.d("FragmentArticles", articleId + " vs " + article.articleId);
                                count += 1;
                                articles.add(article);
                                break;
                            }
                        }

                        if (count == user.posts.size())
                            break;
                    }

                    if (count == user.posts.size())
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
                Article article = articles.get(position);

                if (article != null) {
                    Intent intent = new Intent(getActivity(), ArticleViewActivity.class);
                    intent.putExtra("tag", article.tag);
                    intent.putExtra("articleId", article.articleId);

                    startActivityForResult(intent, 0);
                }
            }
        });
    }
}
