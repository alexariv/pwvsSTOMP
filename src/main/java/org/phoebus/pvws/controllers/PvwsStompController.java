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
import java.util.logging.Level;
import java.util.logging.Logger;

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
@Controller
public class PvwsStompController {

    @MessageMapping("/echo")
    @SendTo("/topic/echo")
    public ApplicationClientEchoMessage handleEcho(ApplicationClientEchoMessage message) {
        // Business logic or just echo back
        return message;
    }

    @MessageMapping("/write")
    @SendTo("/topic/write")
    public ApplicationClientWriteMessage handleWrite(ApplicationClientWriteMessage message) {
        // You would add PVPool/EPICS write logic here
        return message;
    }

    @MessageMapping("/subscribe")
    @SendTo("/topic/pvs")
    public ApplicationClientPvsMessage handleSubscribe(SubscribeMessage msg) {
        // For example, resolve PVs here and return
        return new ApplicationClientPvsMessage("list", msg.getPvs());
    }
}
