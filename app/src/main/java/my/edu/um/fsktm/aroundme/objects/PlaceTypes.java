package my.edu.um.fsktm.aroundme.objects;

public class PlaceTypes {

    final public static String[] foodTypes = {"bakery", "cafe", "meal_delivery", "meal_takeaway", "restaurant"};
    final public static String[] landmarkTypes = {};

    public static String getTypes(String tag) {
        if (tag.equalsIgnoreCase("food"))
            return "food cafe restaurant";
        else if (tag.equalsIgnoreCase("landmark"))
//            return "art_gallery,amusement_park,shopping_mall,library";
            return "museum garden park mall tower";

        else if (tag.equalsIgnoreCase("transportation"))
            return "bus mrt lrt station";
        else
            return "hotel homestay";
    }

    public static String[] getCategories() {
        return new String[]{"food", "landmark", "transportation", "accommodation"};
    }

}
