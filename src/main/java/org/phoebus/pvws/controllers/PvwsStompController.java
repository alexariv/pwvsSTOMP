package org.phoebus.pvws.controllers;

import org.phoebus.pv.PV;
import org.phoebus.pv.PVPool;
import org.phoebus.pvws.model.ApplicationClientWriteMessage;
import org.phoebus.pvws.model.SubscribeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Controller
public class PvwsStompController {

    private static final Logger logger = Logger.getLogger(PvwsStompController.class.getName());

    private final SimpMessagingTemplate broker;
    private final Map<String, PV> activePvs = new ConcurrentHashMap<>();

    @Autowired
    public PvwsStompController(SimpMessagingTemplate broker) {
        this.broker = broker;
    }

    @MessageMapping("/echo")
    public String handleEcho(String msg) {
        return msg;
    }

    @MessageMapping("/write")
    public void handleWrite(ApplicationClientWriteMessage message) {
        try {
            String pvName = message.getPv();
            PV pv = PVPool.getPV(pvName);
        } catch (Exception ex) {
            logger.warning("Write failed for PV " + message.getPv() + ": " + ex);
        }
    }

    @MessageMapping("/subscribe")
    public void handleSubscribe(SubscribeMessage msg,
                                @Header("simpSessionId") String sessionId) {
        for (String name : msg.getPvs()) {
            String key = sessionId + ":" + name;
            if (activePvs.containsKey(key)) {
                continue;
            }

            try {
                PV pv = PVPool.getPV(name);
                activePvs.put(key, pv);

                // 1) send initial value
                Object initial = pv.read();
                broker.convertAndSend(
                        "/topic/pvs",
                        Map.of(
                                "type",  "update",
                                "pv",    HtmlUtils.htmlEscape(name),
                                "value", initial,
                                "ts",    Instant.now().toString()
                        )
                );

                // 2) register streaming listener
                pv.onValueEvent().subscribe(vtype -> {
                    broker.convertAndSend(
                            "/topic/pvs",
                            Map.of(
                                    "type",  "update",
                                    "pv",    HtmlUtils.htmlEscape(name),
                                    "value", vtype,
                                    "ts",    Instant.now().toString()
                            )

                    );
                    System.out.println("PV " + name + " subscribed");
                });

            } catch (Exception ex) {
                logger.warning("Subscribe failed for PV " + name + ": " + ex.getMessage());
            }
        }
    }
}
