package com.pearnode.app.placero.custom;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.pearnode.app.placero.position.Position;

/**
 * Created by USER on 11/19/2017.
 */
public class PositionSorter {

    public static List<Position> sortPositions(List<Position> positions,
                                               final Position center) {

        Comparator comp = new Comparator<Position>() {
            @Override
            public int compare(Position p1, Position p2) {
                Double distance1 = SphericalUtil.computeDistanceBetween(new LatLng(p1.getLat(), p1.getLng()),
                        new LatLng(center.getLat(), center.getLng()));

                Double distance2 = SphericalUtil.computeDistanceBetween(new LatLng(p2.getLat(), p2.getLng()),
                        new LatLng(center.getLat(), center.getLng()));
                return distance1.compareTo(distance2);
            }
        };
        Collections.sort(positions, comp);
        return positions;
    }
}
