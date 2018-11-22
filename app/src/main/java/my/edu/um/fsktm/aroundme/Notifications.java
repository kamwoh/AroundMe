package my.edu.um.fsktm.aroundme;

public class Notifications {
    private String[] alerts;

    public String[] getAlerts(){
        return alerts;
    }

    public void setAlerts(String[] alerts){
        this.alerts = alerts;
    }

    public static final String[] ALERTS = {
            "Sina commented on Casa Damansara 1",
            "Kam Woh gave a review on Restoran K.Intan",
            "Anonymous commented on Restoran K.Intan",
            "Sina gave a review on Casa Damansara 1"
    };

}
