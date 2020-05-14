package com.example.toosie.CurrentTrack;

import android.location.Location;

import com.example.toosie.CurrentUser.CurrentUserData;
import com.example.toosie.Data.Storage.Position;
import com.example.toosie.Data.Storage.Track;
import com.example.toosie.Services.GoogleMapUtils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

class CurrentTrackData {

    private int allTimeInSeconds;
    private double allDistanceInMetres;
    private double currentSpeedInKmH;
    private final ArrayList<Location> allPositions;

    CurrentTrackData() {
        allTimeInSeconds = 0;
        allDistanceInMetres = 0.0;
        currentSpeedInKmH = 0.0;
        allPositions = new ArrayList<Location>();
    }

    void addSecond() {
        allTimeInSeconds++;
    }

    int getAllTimeInSeconds() {
        return allTimeInSeconds;
    }

    void newPosition(final Location newPosition) {
        updateSpeed(newPosition);
        updateDistance(newPosition);
        allPositions.add(newPosition);
    }

    private void updateSpeed(final Location newPosition) {
        currentSpeedInKmH = newPosition.getSpeed();
    }

    private void updateDistance(final Location newPosition) {
        Location lastPosition = getLastPosition();
        if (lastPosition != null) {
            double newDistance = GoogleMapUtils
                    .distanceBetweenTwoCoordinates(lastPosition, newPosition);
            allDistanceInMetres += newDistance;
        }
    }

    Location getLastPosition() {
        if (!allPositions.isEmpty()) {
            return allPositions.get(allPositions.size() - 1);
        }

        return null;
    }

    void saveData() {
        long id = CurrentUserData.getNewTrackId();
        Track track = toTrackData(id);
        CurrentUserData.addTrack(track);
    }

    private Track toTrackData(final long id) {
        String dateTime = DateFormat.getDateTimeInstance().format(new Date());
        double distance = allDistanceInMetres;
        int time = allTimeInSeconds;
        double avgSpeed = (allDistanceInMetres / allTimeInSeconds) * 3.6;
        String mapImagePath = getMapImageFileName(id);
        ArrayList<Position> positions = new ArrayList<Position>();
        for (Location pos : allPositions) {
            Position position = new Position(pos.getLongitude(), pos.getLatitude());
            positions.add(position);
        }

        return new Track(id, dateTime, distance, time, avgSpeed, mapImagePath, positions);
    }

    private static String getMapImageFileName(final long id) {
        final String FILE_NAME_TEMPLATE = "mapImage_";
        final String FILE_NAME_EXTENSION = ".jpeg";
        return FILE_NAME_TEMPLATE + id + FILE_NAME_EXTENSION;
    }

    String timeToFormatString() {
        int hours = allTimeInSeconds / 3600;
        int minutes = (allTimeInSeconds - hours * 3600) / 60;
        int seconds = allTimeInSeconds % 60;

        return String.format("%s:%s:%s",
                String.format("%02d", hours),
                String.format("%02d", minutes),
                String.format("%02d", seconds));
    }

    String distanceToFormatString() {
        return new DecimalFormat("00.00").format(allDistanceInMetres / 1000);
    }

    String speedToFormatString() {
        return new DecimalFormat("00.00").format(currentSpeedInKmH);
    }
}
