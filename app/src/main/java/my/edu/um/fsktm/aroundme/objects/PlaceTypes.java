package my.edu.um.fsktm.aroundme.objects;

public class PlaceTypes {

    final public static String[] foodTypes = {"bakery", "cafe", "meal_delivery", "meal_takeaway", "restaurant"};
    final public static String[] landmarkTypes = {};

    public static String getTypes(String tag) {
        if (tag.equalsIgnoreCase("food"))
            return tag;
        else if (tag.equalsIgnoreCase("landmarks"))
            return "";

        else if (tag.equalsIgnoreCase("transportation"))
            return "";
        else

            return "";
    }

}
