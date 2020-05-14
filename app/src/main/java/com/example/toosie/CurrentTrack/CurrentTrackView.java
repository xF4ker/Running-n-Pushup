package com.example.toosie.CurrentTrack;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.example.toosie.Activity.Main.MainActivity1;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class CurrentTrackView {

    enum CurrentTrackState { STOP, START, PAUSE }
    private final static int MIN_TIME_SECONDS_FOR_SAVE_TRACK = 10;

    private static CurrentTrackData currentTrackData;
    private static CurrentTrackState trackState;
    private static ArrayList<Polyline> polylines;
    private static Timer trackTimer;

    private static Context context;
    private static TextView timeTextView;
    private static TextView distanceTextView;
    private static TextView speedTextView;
    private static ImageButton stopButton;
    private static ImageButton pauseButton;
    private static ImageButton startButton;

    public static void initializeTrack(final Context context,
                                       final TextView timeTextView,
                                       final TextView distanceTextView,
                                       final TextView speedTextView,
                                       final ImageButton stopButton,
                                       final ImageButton pauseButton,
                                       final ImageButton startButton) {

        CurrentTrackView.context = context;
        CurrentTrackView.timeTextView = timeTextView;
        CurrentTrackView.distanceTextView = distanceTextView;
        CurrentTrackView.speedTextView = speedTextView;
        CurrentTrackView.stopButton = stopButton;
        CurrentTrackView.pauseButton = pauseButton;
        CurrentTrackView.startButton = startButton;

        setTrackState(CurrentTrackState.STOP);
    }

    public static void startTrack() {
        if (trackState != CurrentTrackState.START) {
            setTrackState(CurrentTrackState.START);
        }
    }

    public static void pauseTrack() {
        if (trackState == CurrentTrackState.START) {
            setTrackState(CurrentTrackState.PAUSE);
        }
    }

    public static void stopTrack(final Context context) {
        if (trackState != CurrentTrackState.STOP) {
            if (currentTrackData.getAllTimeInSeconds() >= MIN_TIME_SECONDS_FOR_SAVE_TRACK
                    && currentTrackData.getLastPosition() != null) {
                CurrentTrackState trackStateBeforeDialog = trackState;
                setTrackState(CurrentTrackState.PAUSE);
                StopTrackDialog stopTrackDialog = new StopTrackDialog();
                stopTrackDialog.create(context, trackStateBeforeDialog).show();
            }
            else {
                setTrackState(CurrentTrackState.STOP);
            }
        }
    }

    static void endTrackAndSave() {
        if (trackState != CurrentTrackState.STOP) {
            currentTrackData.saveData();
            setTrackState(CurrentTrackState.STOP);
        }
    }

    private static void setTrackState(final CurrentTrackState trackState) {
        CurrentTrackView.trackState = trackState;

        switch (trackState) {
            case START:
                startTrackTimer();
                updateDataUI();
                updateButtonsUI(false, true);
                break;
            case PAUSE:
                stopTrackTimer();
                updateDataUI();
                updateButtonsUI(true, false);
                break;
            case STOP:
                cleanTrackData();
                cleanRoute();
                stopTrackTimer();
                updateDataUI();
                updateButtonsUI(true, false);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + trackState);
        }
    }

    public static void newPosition(final Location newPosition, final GoogleMap map) {
        if (trackState == CurrentTrackState.START) {
            Location lastPosition = currentTrackData.getLastPosition();
            currentTrackData.newPosition(newPosition);
            if (lastPosition != null) {
                drawRoute(map, lastPosition, newPosition);
            }
        }
    }

    private static void drawRoute(final GoogleMap map,
                                  final Location lastPosition,
                                  final Location newPosition) {

        Polyline polyline = map.addPolyline(new PolylineOptions()
                .add(new LatLng(lastPosition.getLatitude(), lastPosition.getLongitude()),
                        new LatLng(newPosition.getLatitude(), newPosition.getLongitude()))
                .width(20)
                .color(Color.argb(90, 0, 130, 255))
        );
        polylines.add(polyline);
    }

    private static void cleanTrackData() {
        currentTrackData = new CurrentTrackData();
    }

    private static void cleanRoute() {
        if (polylines != null) {
            for(Polyline line : polylines) {
                line.remove();
            }
        }
        polylines = new ArrayList<Polyline>();
    }

    private static void updateDataUI() {
        ((MainActivity1) context).setText(timeTextView,
                currentTrackData.timeToFormatString());
        ((MainActivity1) context).setText(distanceTextView,
                currentTrackData.distanceToFormatString());
        ((MainActivity1) context).setText(speedTextView,
                currentTrackData.speedToFormatString());
    }

    private static void updateButtonsUI(final boolean startButtonEnable,
                                        final boolean pauseButtonEnable) {

        final int CHANGE_BUTTONS_DELAY = 300;

        startButton.setClickable(startButtonEnable);
        pauseButton.setClickable(pauseButtonEnable);
        stopButton.setClickable(false);

        TimerTask changeButtonsTask = new TimerTask() {
            @Override
            public void run() {
                ((MainActivity1) context).enableButton(startButton, startButtonEnable);
                ((MainActivity1) context).enableButton(pauseButton, pauseButtonEnable);
                stopButton.setClickable(true);
            }
        };
        Timer changeButtonsTimer = new Timer();
        changeButtonsTimer.schedule(changeButtonsTask, CHANGE_BUTTONS_DELAY);
    }

    private static void startTrackTimer() {
        TimerTask repeatedTimerTask = new TimerTask() {
            public void run() {
                if (trackState == CurrentTrackState.START) {
                    currentTrackData.addSecond();
                    updateDataUI();
                }
            }
        };
        trackTimer = new Timer("TrackTimer");
        trackTimer.scheduleAtFixedRate(repeatedTimerTask, 1000, 1000);
    }

    private static void stopTrackTimer() {
        if (trackTimer != null) {
            trackTimer.cancel();
        }
    }
}
