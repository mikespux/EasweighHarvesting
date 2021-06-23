/*****************************************************************************
 * Copyright (c) 2014 Laird Technologies. All Rights Reserved.
 *
 * The information contained herein is property of Laird Technologies.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/

package com.plantation.vsp.ble;

public interface IBleBaseActivityUiCallback {

    void onUiConnected();

    void onUiConnecting();

    void onUiDisconnected(final int status);

    void onUiDisconnecting();

    void onUiBatteryRead(final String valueBattery);

    void onUiReadRemoteRssi(final int valueRSSI);
}