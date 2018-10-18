package com.pearnode.app.placero.custom;

import com.pearnode.app.placero.position.Position;

/**
 * Created by USER on 10/17/2017.
 */
public interface LocationPositionReceiver {

    void receivedLocationPostion(Position pe);

    void locationFixTimedOut();

    void providerDisabled();
}
