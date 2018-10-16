package com.pearnode.app.placero.custom;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.maps.model.Marker;

public abstract class OnInfoWindowElemTouchListener implements View.OnTouchListener {

    private final View view;
    private final Handler handler = new Handler();

    private Marker marker;
    private boolean pressed;

    public OnInfoWindowElemTouchListener(View view) {
        this.view = view;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    @Override
    public boolean onTouch(View vv, MotionEvent event) {
        if (0 <= event.getX() && event.getX() <= this.view.getWidth() && 0 <= event.getY() && event.getY() <= this.view.getHeight()) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    this.startPress();
                    break;

                case MotionEvent.ACTION_UP:
                    this.handler.postDelayed(this.runnable, 150);
                    break;

                case MotionEvent.ACTION_CANCEL:
                    this.endPress();
                    break;
                default:
                    break;
            }
        } else {
            this.endPress();
        }
        return false;
    }

    private void startPress() {
        if (!this.pressed) {
            this.pressed = true;
            this.handler.removeCallbacks(this.runnable);
            if (this.marker != null)
                this.marker.showInfoWindow();
        }
    }


    private boolean endPress() {
        if (this.pressed) {
            pressed = false;
            this.handler.removeCallbacks(this.runnable);
            if (this.marker != null)
                this.marker.showInfoWindow();
            return true;
        } else
            return false;
    }


    private final Runnable runnable = new Runnable() {
        public void run() {
            if (OnInfoWindowElemTouchListener.this.endPress()) {
                OnInfoWindowElemTouchListener.this.onClickConfirmed(OnInfoWindowElemTouchListener.this.view, OnInfoWindowElemTouchListener.this.marker);
            }
        }
    };


    /**
     * This is called after a successful click
     */
    protected abstract void onClickConfirmed(View v, Marker marker);


}