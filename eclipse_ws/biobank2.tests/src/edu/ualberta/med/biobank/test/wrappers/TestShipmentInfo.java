package edu.ualberta.med.biobank.test.wrappers;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;

import edu.ualberta.med.biobank.common.wrappers.ShipmentInfoWrapper;
import edu.ualberta.med.biobank.common.wrappers.ShippingMethodWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.common.wrappers.SpecimenWrapper;
import edu.ualberta.med.biobank.test.TestDatabase;
import edu.ualberta.med.biobank.test.internal.ShipmentInfoHelper;
import edu.ualberta.med.biobank.test.internal.SiteHelper;
import edu.ualberta.med.biobank.test.internal.SpecimenHelper;

@Deprecated
public class TestShipmentInfo extends TestDatabase {

    @Test
    public void testGettersAndSetters() throws Exception {
        String name = "testGettersAndSetters" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        SpecimenWrapper spc = SpecimenHelper.addParentSpecimen();

        ShipmentInfoWrapper shipInfo = ShipmentInfoHelper.addShipmentInfo(site,
            ShippingMethodWrapper.getShippingMethods(appService).get(0), spc);
        testGettersAndSetters(shipInfo);
    }

    @Test
    public void testReceivedToday() throws Exception {
        String name = "testReceivedToday" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        SpecimenWrapper spc = SpecimenHelper.addParentSpecimen();

        Date dateNow = new Date();

        ShipmentInfoWrapper shipInfo = ShipmentInfoHelper.addShipmentInfo(site,
            ShippingMethodWrapper.getShippingMethods(appService).get(0),
            TestCommon.getNewWaybill(r), dateNow, spc);

        Assert.assertTrue(shipInfo.isReceivedToday());

        // set date to 1 day in future
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateNow);
        cal.add(Calendar.DAY_OF_MONTH, 1);

        shipInfo.setReceivedAt(cal.getTime());
        Assert.assertFalse(shipInfo.isReceivedToday());

        // set date to 1 day ago
        cal = Calendar.getInstance();
        cal.setTime(dateNow);
        cal.add(Calendar.DAY_OF_MONTH, -1);

        shipInfo.setReceivedAt(cal.getTime());
        Assert.assertFalse(shipInfo.isReceivedToday());
    }

}
