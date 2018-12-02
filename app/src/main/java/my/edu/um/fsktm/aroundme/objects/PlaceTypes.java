package my.edu.um.fsktm.aroundme.objects;

import java.util.ArrayList;
import java.util.Arrays;

public class PlaceTypes {

    final public static String[] foodTypes = {"bakery", "cafe", "meal_delivery", "meal_takeaway", "restaurant"};
    final public static String[] landmarkTypes = {};

    public static ArrayList<String> getTypes(String tag) {
        return new ArrayList<>(Arrays.asList(foodTypes));
    }

}
