package com.github.libgraviton.workerbase.util;

import com.github.libgraviton.workerbase.model.QueueEvent;
import org.jetbrains.annotations.NotNull;


public class Finnova {
    public @NotNull static String getCoreUserid(@NotNull QueueEvent queueEvent, String def) throws Exception {
        String coreUserId = queueEvent != null ? queueEvent.getCoreUserId() : def;

        if (coreUserId == null || coreUserId.isEmpty()) {
            throw new Exception("No core user in queue event and no default given.");
        }

        if (coreUserId.equals("anonymous")) {
            if (def == null || def.isEmpty()) {
                throw new Exception("core user id is 'anonymous' and now default given.");
            } else {
                return def;
            }
        } else {
            return coreUserId;
        }
    }


}
