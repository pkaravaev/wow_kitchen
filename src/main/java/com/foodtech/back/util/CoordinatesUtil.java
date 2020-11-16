package com.foodtech.back.util;

import com.foodtech.back.entity.model.iiko.DeliveryCoordinate;
import com.foodtech.back.entity.model.iiko.DeliveryZone;

import java.util.List;

public class CoordinatesUtil {

    // Работает! Не трогать!
    // https://stackoverflow.com/questions/12083093/how-to-define-if-a-determinate-point-is-inside-a-region-lat-long
    public static boolean coordinateInZone(DeliveryZone zone, DeliveryCoordinate coordinate) {
        boolean isInside = false;
        List<DeliveryCoordinate> coordinates = zone.getCoordinates();
        int sides = coordinates.size();
        for (int i = 0, j = sides - 1; i < sides; j = i++) {
            if ((((coordinates.get(i).getLongitudeDouble() <= coordinate.getLongitudeDouble())
                    && (coordinate.getLongitudeDouble() < coordinates.get(j).getLongitudeDouble()))
                    || ((coordinates.get(j).getLongitudeDouble() <= coordinate.getLongitudeDouble())
                    && (coordinate.getLongitudeDouble() < coordinates.get(i).getLongitudeDouble())))
                    && (coordinate.getLatitudeDouble() < (coordinates.get(j).getLatitudeDouble() - coordinates.get(i).getLatitudeDouble())
                    * (coordinate.getLongitudeDouble() - coordinates.get(i).getLongitudeDouble())
                    / (coordinates.get(j).getLongitudeDouble() - coordinates.get(i).getLongitudeDouble()) + coordinates.get(i).getLatitudeDouble()))
            {
                isInside = !isInside;
            }
        }
        return isInside;
    }
}
