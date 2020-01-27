package gov.unsc.routemapper;

import com.google.android.gms.maps.model.LatLng;

public class Place {
    private double lat, lng;
    private LatLng latLng;

    Place(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
        this.latLng = new LatLng(lat, lng);
    }

    Place(double l, double m, double s, boolean ws, double ll, double mm, double ss, boolean wsws) {
        this(toCoordDouble(l, m, s, ws), toCoordDouble(ll, mm, ss, wsws));
    }

    private static double toCoordDouble(double l, double m, double s, boolean ws) {
        return l + (m / 60.0) + (s / 3600.0) * (ws ? -1 : 1);
}

    static double dist(LatLng place1, LatLng place2) {
        double p1 = Math.toRadians(place1.latitude);
        double p2 = Math.toRadians(place2.latitude);
        double dp = p2 - p1;
        double dl = Math.toRadians(place2.longitude - place1.longitude);
        double a = Math.sin(dp/2) * Math.sin(dp/2) + Math.cos(p1) * Math.cos(p2) * Math.sin(dl/2) * Math.sin(dl/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double R = 6371e3;
        return R * c;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}
