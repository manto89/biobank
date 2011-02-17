package edu.ualberta.med.biobank.test.internal;

import java.util.Arrays;
import java.util.Date;

import edu.ualberta.med.biobank.common.wrappers.ActivityStatusWrapper;
import edu.ualberta.med.biobank.common.wrappers.CenterWrapper;
import edu.ualberta.med.biobank.common.wrappers.CollectionEventWrapper;
import edu.ualberta.med.biobank.common.wrappers.PatientWrapper;
import edu.ualberta.med.biobank.common.wrappers.ShippingMethodWrapper;
import edu.ualberta.med.biobank.common.wrappers.SourceVesselWrapper;
import edu.ualberta.med.biobank.common.wrappers.StudyWrapper;
import edu.ualberta.med.biobank.test.Utils;
import edu.ualberta.med.biobank.test.wrappers.TestCommon;

public class CollectionEventHelper extends DbHelper {

    public static CollectionEventWrapper newCollectionEvent(
        CenterWrapper<?> center, ShippingMethodWrapper method, String waybill,
        Date dateReceived, SourceVesselWrapper... svs) throws Exception {
        CollectionEventWrapper cevent = new CollectionEventWrapper(appService);
        if (center != null) {
            cevent.setSourceCenter(center);
            center.addToCollectionEventCollection(Arrays.asList(cevent));
        }
        cevent.setActivityStatus(ActivityStatusWrapper
            .getActiveActivityStatus(appService));
        cevent.setShippingMethod(method);
        cevent.setWaybill(waybill);
        if (dateReceived != null) {
            cevent.setDateReceived(dateReceived);
        }

        cevent.setDeparted(Utils.getRandomDate());

        if ((svs != null) && (svs.length != 0)) {
            cevent.addToSourceVesselCollection(Arrays.asList(svs));
            for (SourceVesselWrapper sv : svs) {
                sv.setCollectionEvent(cevent);
            }
        }
        return cevent;
    }

    public static CollectionEventWrapper newCollectionEvent(
        CenterWrapper<?> center, ShippingMethodWrapper method) throws Exception {
        return newCollectionEvent(center, method, TestCommon.getNewWaybill(r),
            Utils.getRandomDate());
    }

    public static CollectionEventWrapper addCollectionEvent(
        CenterWrapper<?> center, ShippingMethodWrapper method, String waybill,
        Date dateReceived, SourceVesselWrapper... svs) throws Exception {
        CollectionEventWrapper ce = newCollectionEvent(center, method, waybill,
            dateReceived, svs);
        ce.persist();
        return ce;
    }

    public static CollectionEventWrapper addCollectionEvent(
        CenterWrapper<?> center, ShippingMethodWrapper method,
        SourceVesselWrapper... svs) throws Exception {
        return addCollectionEvent(center, method, TestCommon.getNewWaybill(r),
            svs);
    }

    public static CollectionEventWrapper addCollectionEvent(
        CenterWrapper<?> center, ShippingMethodWrapper method, String waybill,
        SourceVesselWrapper... svs) throws Exception {
        CollectionEventWrapper cevent = newCollectionEvent(center, method,
            waybill, Utils.getRandomDate(), svs);
        cevent.persist();
        return cevent;
    }

    public static CollectionEventWrapper addCollectionEventWithRandomPatient(
        CenterWrapper<?> center, String name) throws Exception {
        StudyWrapper study = StudyHelper.addStudy(name);
        study.persist();

        PatientWrapper patient = PatientHelper.addPatient(name, study);
        SourceVesselWrapper sv = SourceVesselHelper.newSourceVessel(patient,
            Utils.getRandomDate(), 0.1);

        return addCollectionEvent(center, ShippingMethodWrapper
            .getShippingMethods(appService).get(0), "wb-54", sv);

    }

    public static CollectionEventWrapper addCollectionEventNoWaybill(
        CenterWrapper<?> center, ShippingMethodWrapper method,
        SourceVesselWrapper... svs) throws Exception {
        CollectionEventWrapper cevent = newCollectionEvent(center, method,
            null, Utils.getRandomDate(), svs);
        cevent.persist();
        return cevent;
    }
}
