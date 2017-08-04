package uk.org.tombolo.importer.utils;

/**
 * Class representing the latitude longitude tuple
 */
public class LatLong {

    private String latitude;
    private String longitude;

    public LatLong(String latitude, String longitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getLatitude() { return latitude; }
    public String getLongitude() { return longitude; }
}
