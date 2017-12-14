package com.abilix.robot.hrobot.voice.iflytek;

import android.os.Binder;

/**
 * @author tony 2017/12/14 13:48
 *         <p>
 *         Text to speech(tts)
 */

public class TTSBinder extends Binder {

    private final TTSService service;

    public TTSBinder(TTSService service) {
        this.service = service;
    }

    /**
     * Get service associated with this binder
     *
     */
    public TTSService getService() {
        return service;
    }

    /**
     * @param content speak content
     * @param isTypeCloud use cloud or local
     *
     */
    public void speak(String content, boolean isTypeCloud) {
        service.speak(content, isTypeCloud);
    }
}
