package my.edu.um.fsktm.aroundme.objects;

public class SimpleArticle {

    public String author;
    public String title;
    public Long rating;
    public String cover;

    public SimpleArticle() {

    }

    public SimpleArticle(String title, Long rating, String cover) {
        this.title = title;
        this.rating = rating;
        this.cover = cover;
    }

    @Override
    public String toString() {
        return "title: " + title + ", rating: " + rating + ", cover: " + cover;
    }

}
