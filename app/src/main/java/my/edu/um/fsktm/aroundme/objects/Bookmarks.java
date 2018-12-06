package my.edu.um.fsktm.aroundme.objects;

import my.edu.um.fsktm.aroundme.R;

public class Bookmarks {

    private String[] location;
    private Integer[] foodImg;

    public String[] getLocation(){
        return location;
    }

    public void setLocation(String[] location){
        this.location = location;
    }

    public Integer[] getFoodImg(){
        return foodImg;
    }

    public void setFoodImg(Integer[] foodImg){
        this.foodImg = foodImg;
    }

    public static final String LOCATIONS[] = {
            "Tiffin’s By Chef Korn",
            "An Viet"
    };

    public static final Integer FOODIMG[] = {
            R.drawable.article1,
            R.drawable.article2
    };


}
