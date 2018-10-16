package com.pearnode.app.placero.custom;

import com.pearnode.app.placero.position.PositionElement;

/**
 * Created by USER on 10/17/2017.
 */
public interface LocationPositionReceiver {

    void receivedLocationPostion(PositionElement pe);

    void locationFixTimedOut();

    void providerDisabled();
}
