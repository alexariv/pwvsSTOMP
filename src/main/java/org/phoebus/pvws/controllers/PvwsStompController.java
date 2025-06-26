/*
 * Copyright (C) 2024 European Spallation Source ERIC.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package org.phoebus.pvws.controllers;

import org.epics.util.array.ListInteger;
import org.epics.vtype.Array;
import org.epics.vtype.VType;
import org.phoebus.pv.PV;
import org.phoebus.pv.PVPool;
import org.phoebus.pv.RefCountMap;
import org.phoebus.pvws.model.*;
import org.phoebus.pvws.ws.WebSocket;
import org.phoebus.pvws.ws.WebSocketPV;
import org.phoebus.util.time.TimestampFormats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils; 

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/*test add 
import org.phoebus.pv.PVListener;
import org.phoebus.pv.ValueUpdate; */

/**
 * This class replaces several servlets from the original pvws implementation,
 * which are published under the following copyright:
 * <p>
 * ******************************************************************************
 * Copyright (c) 2019-2022 UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the LICENSE
 * which accompanies this distribution
 * ******************************************************************************
 * </p>
 */
@RestController
public class PvwsStompController {

    private static final Logger logger = Logger.getLogger(PvwsStompController.class.getName());

    // Track active PVs per client (if needed, expand this into per-session storage)
    private final Map<String, PV> activePvs = new ConcurrentHashMap<>();

    @MessageMapping("/echo")
    @SendTo("/topic/echo")
    public ApplicationClientEchoMessage handleEcho(ApplicationClientEchoMessage message) {
        // Business logic or just echo back
        return message;
    }

    @MessageMapping("/write")
    @SendTo("/topic/write")
    public ApplicationClientWriteMessage handleWrite(ApplicationClientWriteMessage message) {
        try {
            PV pv = activePvs.get(message.getPv());
            if (pv == null) {
                pv = PVPool.getPV(message.getPv());
                activePvs.put(message.getPv(), pv);
            }
            pv.write(message.getValue());
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to write to PV: " + message.getPv(), ex);
        }
        return message;
    }


   /*comment out for now so i could test   @MessageMapping("/subscribe")
    @SendTo("/topic/pvs")
    public ApplicationClientPvsMessage handleSubscribe(SubscribeMessage msg) {
        List<String> subscribed = new ArrayList<>();
        for (String name : msg.getPvs()) {
            try {
                PV pv = PVPool.getPV(name);
                activePvs.put(name, pv);
                subscribed.add(name);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to subscribe to PV: " + name, ex);
            }
        }
        return new ApplicationClientPvsMessage("list", subscribed);
   } */
    /* test 1
    @MessageMapping("/subscribe")
public void handleSubscribe(SubscribeMessage msg) {
    for (String name : msg.getPvs()) {
        try {
            PV pv = PVPool.getPV(name);
            activePvs.put(name, pv);

            pv.addListener(value -> {
                if (value instanceof VType) {
                    VType v = (VType) value;
                    logger.info("Update from PV '" + name + "': " + v.toString());
                    // Optionally send update to client using messaging
                } else {
                    logger.warning("Received non-VType value from PV '" + name + "'");
                }
            });

            pv.start();

        } catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to subscribe to PV: " + name, ex);
        }
    }
} */
/*test 2
@MessageMapping("/subscribe")
public void handleSubscribe(SubscribeMessage msg) {
    for (String name : msg.getPvs()) {
        try {
            PV pv = PVPool.getPV(name);
            activePvs.put(name, pv);

            pv.addListener(new PVListener() {
                @Override
                public void valueChanged(PV pv, ValueUpdate update) {
                    logger.info("Value changed for PV '" + name + "': " + update.getValue());
                }
            });

            pv.start();  // This should be valid — PVPool returns a usable instance

        } catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to subscribe to PV: " + name, ex);
        }
    }
}*/
/*test 3
@MessageMapping("/subscribe")
public void handleSubscribe(SubscribeMessage msg) {
    for (String name : msg.getPvs()) {
        try {
            PV pv = PVPool.getPV(name);
            activePvs.put(name, pv);

            // Use the simpler two-arg listener if PVListener is unavailable
            pv.addListener((pvname, value) -> {
                logger.info("Update from '" + pvname + "': " + value);
            });

            // Only call start if it exists; otherwise skip it for now
            // If your PV class doesn't have start(), this line can be removed
            // or replace with reflection if needed.

        } catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to subscribe to PV: " + name, ex);
        }
    }
} */
/*test 4
@MessageMapping("/subscribe")
public void handleSubscribe(SubscribeMessage msg) {
    for (String name : msg.getPvs()) {
        try {
            PV pv = PVPool.getPV(name);
            activePvs.put(name, pv);

            pv.addOnValueChanged(value -> {
                logger.info("Update from '" + name + "': " + value);
                // Optionally: broadcast this value to STOMP clients here
            });

        } catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to subscribe to PV: " + name, ex);
        }
    }
}*/
//test 5
@MessageMapping("/subscribe")
public void handleSubscribe(SubscribeMessage msg) {
    for (String name : msg.getPvs()) {
        try {
            PV pv = PVPool.getPV(name);
            activePvs.put(name, pv);

            Object value = pv.read();
            logger.info("Read initial value from '" + name + "': " + value);

        } catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to subscribe to PV: " + name, ex);
        }
    }
}





}
