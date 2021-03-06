package database;

import java.awt.*;

public class Lodging {
    private String name, placeID, formattedAddress;
    private Point coordinates = new Point();
    private int priceLevel;
    private double rating;
    private boolean openNow;

    public Lodging() {

    }

    public Lodging(String name, String placeID, String formattedAddress,
                   Point coordinates, int priceLevel, double rating,
                   boolean openNow) {
        this.name = name;
        this.placeID = placeID;
        this.formattedAddress = formattedAddress;
        this.coordinates = coordinates;
        this.priceLevel = priceLevel;
        this.rating = rating;
        this.openNow = openNow;
    }

    public String getName() {
        return name;
    }
    public String getPlaceID() {
        return placeID;
    }
    public String getFormattedAddress() {
        return formattedAddress;
    }
    public Point getCoordinates() {
        return coordinates;
    }
    public int getPriceLevel() {
        return priceLevel;
    }
    public double getRating() {
        return rating;
    }
    public boolean isOpenNow() {
        return openNow;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setPlaceID(String placeID) {
        this.placeID = placeID;
    }
    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }
    public void setPriceLevel(int priceLevel) {
        this.priceLevel = priceLevel;
    }
    public void setRating(double rating) {
        this.rating = rating;
    }
    public void setOpenNow(boolean openNow) {
        this.openNow = openNow;
    }
}
