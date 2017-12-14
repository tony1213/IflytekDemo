package com.abilix.robot.hrobot.voice.iflytek;

import android.os.Binder;

/**
 * @author tony 2017/12/14 11:00
 *
 * Trsnslate voice to text
 */
public class VTTBinder extends Binder {

    private final VTTService service;

    /**
     * Create a new binder for given service
     *
     * @param service
     */
    public VTTBinder(VTTService service)
    {
        super();

        this.service = service;
    }

    /**
     * Get service associated with this binder
     *
     * @return
     */
    public VTTService getService()
    {
        return service;
    }

    /**
     * Call listenBegin
     *
     */
    public void listenStart()
    {
        service.listenBegin();
    }
    /**
     * Call listenStop
     *
     */
    public void listenStop()
    {
        service.stopListen();
    }
}
