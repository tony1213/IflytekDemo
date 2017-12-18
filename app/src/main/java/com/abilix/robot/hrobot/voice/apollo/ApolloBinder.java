package com.abilix.robot.hrobot.voice.apollo;


import android.os.Binder;


/**
 * @author tony 2017/12/15 16:30
 *         Apollo Client
 */

public class ApolloBinder extends Binder {

    private final ApolloService service;

    public ApolloBinder(ApolloService service) {
        this.service = service;
    }

    /**
     * Get service associated with this binder
     *
     */
    public ApolloService getService() {
        return service;
    }

    /**
     * @param content speak content
     * @param isTypeCloud use cloud or local
     *
     */
//    public void speak(String content, boolean isTypeCloud) {
//        service.speak(content, isTypeCloud);
//    }
}
