package my.edu.um.fsktm.aroundme.objects;

import android.support.annotation.NonNull;

public class SimpleArticle implements Comparable<SimpleArticle> {

    public String author;
    public String title;
    public Double rating;
    public String cover;

    public Double lat;
    public Double lng;

    public SimpleArticle() {

    }

    public SimpleArticle(String author, String title, Double rating, String cover, Double lat, Double lng) {
        this.author = author;
        this.title = title;
        this.rating = rating;
        this.cover = cover;
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    public String toString() {
        return "title: " + title + ", rating: " + rating + ", cover: " + cover;
    }

    @Override
    public int compareTo(@NonNull SimpleArticle o) {
        return (int) ((this.rating - o.rating) * 100);
    }
}
