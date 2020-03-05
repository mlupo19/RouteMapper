package gov.unsc.routemapper;

import android.graphics.Bitmap;
import android.media.Image;

import java.io.Serializable;

public class Achievement implements Serializable {

    private boolean achieved;
    private String name;
    private int image;

    public Achievement(String name, int image) {
        this.name = name;
        this.image = image;
    }

    public void achieve() {
        achieved = true;
    }

    public boolean isAchieved() {
        return achieved;
    }

    public String getName() {
        return name;
    }

    public int getImage() {
        return image;
    }
}
