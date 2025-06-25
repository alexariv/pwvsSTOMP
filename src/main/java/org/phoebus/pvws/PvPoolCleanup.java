package org.phoebus.pvws;

import org.phoebus.pv.PV;
import org.phoebus.pv.PVPool;
import org.phoebus.pv.RefCountMap;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
public class PvPoolCleanup {

    @PreDestroy
    public void cleanup() {
        if (!PVPool.getPVReferences().isEmpty()) {
            for (final RefCountMap.ReferencedEntry<PV> ref : PVPool.getPVReferences()) {
                System.out.println("Unreleased PV " + ref.getEntry().getName());
                PVPool.releasePV(ref.getEntry());
            }
        }
    }
}

