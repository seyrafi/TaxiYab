package com.taxiyab.Model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by MehrdadS on 6/20/2016.
 */
public class DestStructureGooglePlace extends DestStructureBase{
    public String description;
    public String placeId;

    public DestStructureGooglePlace(String description, String placeId) {
        this.objectType = DestStructureType.GOOLE_PLACES_MAP;
        this.description = description;
        this.placeId = placeId;
    }

    @Override
    public String toString() {
        return description.toString();
    }
}
