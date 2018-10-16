package com.pearnode.app.placero.custom;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.pearnode.app.placero.position.PositionElement;

/**
 * Created by USER on 11/19/2017.
 */
public class PositionSorter {

    public static List<PositionElement> sortPositions(List<PositionElement> positions,
                                                      final PositionElement center) {

        Comparator comp = new Comparator<PositionElement>() {
            @Override
            public int compare(PositionElement p1, PositionElement p2) {
                Double distance1 = SphericalUtil.computeDistanceBetween(new LatLng(p1.getLat(), p1.getLon()),
                        new LatLng(center.getLat(), center.getLon()));

                Double distance2 = SphericalUtil.computeDistanceBetween(new LatLng(p2.getLat(), p2.getLon()),
                        new LatLng(center.getLat(), center.getLon()));
                return distance1.compareTo(distance2);
            }
        };
        Collections.sort(positions, comp);
        return positions;
    }
}
