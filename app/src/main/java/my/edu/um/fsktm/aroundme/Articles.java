package my.edu.um.fsktm.aroundme;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Articles {
    private String[] comment;
    private Integer[] articleImg;

    public String[] getComment(){
        return comment;
    }

    public void setComment(String[] comment){
        this.comment = comment;
    }

    public Integer[] getArticleImg(){
        return articleImg;
    }

    public void setArticleImg(Integer[] foodImg){
        this.articleImg = foodImg;
    }

    public static final String[] COMMENTS = {
            "You added a new place named: Casa Damansara 1 few minutes ago.",
            "You added a new place named: Restoran K. Intan an hour ago."
    };

    public static final Integer LOCATIONIMG[] = {
            R.drawable.article1,
            R.drawable.article2
    };
}
