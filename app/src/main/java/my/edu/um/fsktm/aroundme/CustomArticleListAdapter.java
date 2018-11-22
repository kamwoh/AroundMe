package my.edu.um.fsktm.aroundme;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomArticleListAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final Integer[] locationImg;
    private final String[] text;

    public CustomArticleListAdapter(Activity context, Integer[]locationImg, String[]text){
        super(context, R.layout.custom_articlelist, text);
        this.context = context;
        this.locationImg = locationImg;
        this.text = text;
    }

    public View getView(int position, View view, ViewGroup parent){
        LayoutInflater inflater = context.getLayoutInflater();

        View rowView = inflater.inflate(R.layout.custom_articlelist, null, true);

        TextView comment = (TextView) rowView.findViewById(R.id.article_comment);
        ImageView commentImg = (ImageView) rowView.findViewById(R.id.article_img);

        comment.setText(text[position]);
        commentImg.setImageResource(locationImg[position]);
        return rowView;

    }
}
