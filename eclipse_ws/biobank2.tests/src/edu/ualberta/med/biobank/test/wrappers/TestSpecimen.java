package edu.ualberta.med.biobank.test.wrappers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.ualberta.med.biobank.common.debug.DebugUtil;
import edu.ualberta.med.biobank.common.exception.BiobankCheckException;
import edu.ualberta.med.biobank.common.exception.DuplicateEntryException;
import edu.ualberta.med.biobank.common.util.DispatchSpecimenState;
import edu.ualberta.med.biobank.common.util.DispatchState;
import edu.ualberta.med.biobank.common.util.RowColPos;
import edu.ualberta.med.biobank.common.wrappers.ActivityStatusWrapper;
import edu.ualberta.med.biobank.common.wrappers.AliquotedSpecimenWrapper;
import edu.ualberta.med.biobank.common.wrappers.CollectionEventWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.common.wrappers.DispatchWrapper;
import edu.ualberta.med.biobank.common.wrappers.ProcessingEventWrapper;
import edu.ualberta.med.biobank.common.wrappers.ShippingMethodWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.common.wrappers.SpecimenTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.SpecimenWrapper;
import edu.ualberta.med.biobank.server.applicationservice.exceptions.BiobankSessionException;
import edu.ualberta.med.biobank.server.applicationservice.exceptions.DuplicatePropertySetException;
import edu.ualberta.med.biobank.server.applicationservice.exceptions.InvalidOptionException;
import edu.ualberta.med.biobank.server.applicationservice.exceptions.ModelIsUsedException;
import edu.ualberta.med.biobank.server.applicationservice.exceptions.ValueNotSetException;
import edu.ualberta.med.biobank.test.TestDatabase;
import edu.ualberta.med.biobank.test.Utils;
import edu.ualberta.med.biobank.test.internal.ContainerHelper;
import edu.ualberta.med.biobank.test.internal.ContainerTypeHelper;
import edu.ualberta.med.biobank.test.internal.DbHelper;
import edu.ualberta.med.biobank.test.internal.DispatchHelper;
import edu.ualberta.med.biobank.test.internal.ProcessingEventHelper;
import edu.ualberta.med.biobank.test.internal.SiteHelper;
import edu.ualberta.med.biobank.test.internal.SpecimenHelper;
import edu.ualberta.med.biobank.test.internal.SpecimenTypeHelper;

public class TestSpecimen extends TestDatabase {

    private SpecimenWrapper parentSpc;

    private SpecimenWrapper childSpc;

    private SiteWrapper site;

    private ContainerWrapper topContainer;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        parentSpc = SpecimenHelper.addParentSpecimen();
        site = SiteHelper.addSite("testsite" + r.nextInt());
        ContainerTypeWrapper typeChild = ContainerTypeHelper.addContainerType(
            site, "ctTypeChild" + r.nextInt(), "ctChild", 1, 4, 5, false);
        typeChild.addToSpecimenTypeCollection(Arrays.asList(DbHelper
            .chooseRandomlyInList(SpecimenTypeWrapper.getAllSpecimenTypes(
                appService, false))));
        typeChild.persist();

        ContainerTypeWrapper topType = ContainerTypeHelper.addContainerType(
            site, "topType" + r.nextInt(), "ct", 1, 4, 5, true);
        topType.addToChildContainerTypeCollection(Arrays.asList(typeChild));
        topType.persist();

        topContainer = ContainerHelper.addContainer("top" + r.nextInt(), "cc",
            site, topType);

        ContainerWrapper container = ContainerHelper.addContainer(null, "2nd",
            topContainer, site, typeChild, 3, 3);

        childSpc = SpecimenHelper.addSpecimens(parentSpc, container, 0, 0, 1)
            .get(0);
    }

    @Test
    public void testGettersAndSetters() throws Exception {
        testGettersAndSetters(parentSpc);
    }

    @Test
    public void testPersistFailActivityStatusNull() throws Exception {
        parentSpc.setActivityStatus(null);
        try {
            parentSpc.persist();
            Assert.fail("Should not insert the specimen : no activity status");
        } catch (ValueNotSetException vnse) {
            Assert.assertTrue(true);
        }
        parentSpc.setActivityStatus(ActivityStatusWrapper
            .getActiveActivityStatus(appService));
        parentSpc.persist();
    }

    @Test
    public void testCheckInventoryIdUnique() throws BiobankCheckException,
        Exception {
        SpecimenWrapper duplicate = SpecimenHelper.newSpecimen(parentSpc,
            childSpc.getSpecimenType(),
            ActivityStatusWrapper.ACTIVE_STATUS_STRING,
            parentSpc.getCollectionEvent(), childSpc.getProcessingEvent(),
            childSpc.getParentContainer(), 2, 2);

        duplicate.setInventoryId(parentSpc.getInventoryId());
        try {
            duplicate.checkInventoryIdUnique();
            Assert.fail("The check should detect that this is the same");
        } catch (DuplicateEntryException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testCheckInventoryIdUniqueCaseSensitive()
        throws BiobankCheckException, Exception {
        int i = r.nextInt();
        parentSpc.setInventoryId("toto" + i);
        parentSpc.persist();
        SpecimenWrapper duplicate = SpecimenHelper.newSpecimen(parentSpc,
            childSpc.getSpecimenType(),
            ActivityStatusWrapper.ACTIVE_STATUS_STRING,
            parentSpc.getCollectionEvent(), childSpc.getProcessingEvent(),
            childSpc.getParentContainer(), 2, 2);

        duplicate.setInventoryId("TOTO" + i);
        try {
            duplicate.checkInventoryIdUnique();
            Assert.assertTrue(true);
        } catch (BiobankCheckException bce) {
            Assert.fail("InventoryId is case sensitive. Should not fail");
        }
    }

    @Test
    public void testPersistCheckInventoryIdUnique()
        throws BiobankCheckException, Exception {

        SpecimenWrapper duplicate = SpecimenHelper.newSpecimen(parentSpc,
            childSpc.getSpecimenType(),
            ActivityStatusWrapper.ACTIVE_STATUS_STRING,
            parentSpc.getCollectionEvent(), childSpc.getProcessingEvent(),
            childSpc.getParentContainer(), 2, 2);
        duplicate.setInventoryId(parentSpc.getInventoryId());

        try {
            duplicate.persist();
            Assert.fail("same inventory id !");
        } catch (DuplicatePropertySetException e) {
            Assert.assertTrue(true);
        }
        duplicate.setInventoryId("qqqq" + r.nextInt());
        duplicate.persist();

        duplicate.setInventoryId(parentSpc.getInventoryId());
        try {
            duplicate.persist();
            Assert
                .fail("still can't save it with  the same inventoryId after a first add with anotehr inventoryId!");
        } catch (DuplicatePropertySetException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testPersistCheckInventoryIdUniqueCaseSensitive()
        throws BiobankCheckException, Exception {
        int i = r.nextInt();
        parentSpc.setInventoryId("toto" + i);
        parentSpc.persist();

        SpecimenWrapper duplicate = SpecimenHelper.newSpecimen(parentSpc,
            childSpc.getSpecimenType(),
            ActivityStatusWrapper.ACTIVE_STATUS_STRING,
            parentSpc.getCollectionEvent(), childSpc.getProcessingEvent(),
            childSpc.getParentContainer(), 2, 2);
        duplicate.setInventoryId("toto" + i);

        try {
            duplicate.persist();
            Assert.fail("same inventory id !");
        } catch (DuplicatePropertySetException dee) {
            Assert.assertTrue(true);
        }

        duplicate.setInventoryId("TOTO" + r.nextInt());
        duplicate.persist();
    }

    @Test
    public void testPersistPositionAlreadyUsed() throws BiobankCheckException,
        Exception {
        parentSpc.persist();
        RowColPos pos = childSpc.getPosition();

        SpecimenWrapper duplicate = SpecimenHelper.newSpecimen(parentSpc,
            childSpc.getSpecimenType(),
            ActivityStatusWrapper.ACTIVE_STATUS_STRING,
            parentSpc.getCollectionEvent(), childSpc.getProcessingEvent(),
            childSpc.getParentContainer(), pos.getRow(), pos.getCol());

        try {
            duplicate.persist();
            Assert
                .fail("should not be allowed to add an specimen in a position that is not empty");
        } catch (BiobankSessionException bce) {
            Assert.assertTrue(true);
        }

        duplicate
            .setParent(duplicate.getParentContainer(), new RowColPos(2, 3));
        duplicate.persist();

        duplicate.setInventoryId(Utils.getRandomString(5));
        duplicate.persist();
    }

    @Test
    public void testPersistCheckParentAcceptSpecimenType()
        throws BiobankCheckException, Exception {
        SpecimenTypeWrapper oldSpecimenType = childSpc.getSpecimenType();

        SpecimenTypeWrapper type2 = SpecimenTypeHelper
            .addSpecimenType("sampletype_2");
        childSpc.setSpecimenType(type2);
        try {
            childSpc.persist();
            Assert.fail("Container can't hold this type !");
        } catch (InvalidOptionException e) {
            Assert.assertTrue(true);
        }

        childSpc.setSpecimenType(oldSpecimenType);
        childSpc.persist();
    }

    @Test
    public void testCheckProcessingEventNotNull() throws BiobankCheckException,
        Exception {
        parentSpc.setCollectionEvent(null);
        try {
            parentSpc.persist();
            Assert.fail("Patient visit should be set!");
        } catch (ValueNotSetException vnse) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testDelete() throws Exception {
        String name = "testDelete" + r.nextInt();
        SpecimenTypeWrapper type1 = childSpc.getSpecimenType();

        try {
            type1.delete();
            Assert.fail("cannot delete a type in use by a specimen");
        } catch (ModelIsUsedException e) {
            Assert.assertTrue(true);
        }

        SpecimenTypeWrapper type2 = SpecimenTypeHelper.addSpecimenType(name
            + "_st2");
        SpecimenTypeHelper.removeFromCreated(type2);

        ContainerTypeWrapper typeChild = childSpc.getParentContainer()
            .getContainerType();

        typeChild.addToSpecimenTypeCollection(Arrays.asList(type2));
        typeChild.persist();

        childSpc.reload();
        childSpc.setSpecimenType(type2);
        childSpc.persist();

        try {
            type2.delete();
            Assert.fail("cannot delete a type in use by a specimen");
        } catch (ModelIsUsedException e) {
            Assert.assertTrue(true);
        }

        childSpc.setSpecimenType(type1);
        childSpc.persist();

        typeChild.removeFromSpecimenTypeCollectionWithCheck(Arrays
            .asList(type2));
        typeChild.persist();

        type2.delete();
    }

    @Test
    public void testGetSetCollectionEvent() {
        CollectionEventWrapper cevent = new CollectionEventWrapper(appService);
        parentSpc.setCollectionEvent(cevent);
        Assert.assertTrue(parentSpc.getCollectionEvent().getId() == cevent
            .getId());
    }

    @Test
    public void testSetSpecimenPositionFromString() throws Exception {
        childSpc.setParentFromPositionString("A1",
            childSpc.getParentContainer());
        childSpc.persist();
        Assert
            .assertTrue(childSpc.getPositionString(false, false).equals("A1"));
        RowColPos pos = childSpc.getPosition();
        Assert.assertTrue((pos.getCol() == 0) && (pos.getRow() == 0));

        childSpc.setParentFromPositionString("C2",
            childSpc.getParentContainer());
        childSpc.persist();
        Assert
            .assertTrue(childSpc.getPositionString(false, false).equals("C2"));
        pos = childSpc.getPosition();
        Assert.assertTrue((pos.getCol() == 1) && (pos.getRow() == 2));

        try {
            childSpc.setParentFromPositionString("79",
                childSpc.getParentContainer());
            Assert.fail("invalid position");
        } catch (Exception bce) {
            Assert.assertTrue(true);
        }

        SpecimenWrapper specimen = new SpecimenWrapper(appService);
        Assert.assertNull(specimen.getPositionString());
    }

    @Test
    public void testGetPositionString() throws Exception {
        childSpc.setParentFromPositionString("A1",
            childSpc.getParentContainer());
        Assert
            .assertTrue(childSpc.getPositionString(false, false).equals("A1"));
        String parentLabel = childSpc.getParentContainer().getLabel();
        Assert.assertTrue(childSpc.getPositionString(true, false).equals(
            parentLabel + "A1"));
        Assert.assertTrue(childSpc.getPositionString().equals(
            parentLabel + "A1 ("
                + topContainer.getContainerType().getNameShort() + ")"));
    }

    @Test
    public void testGetSetPosition() throws Exception {
        RowColPos position = new RowColPos(1, 3);
        childSpc.setParent(childSpc.getParentContainer(), position);
        RowColPos newPosition = childSpc.getPosition();
        Assert.assertEquals(position.getRow(), newPosition.getRow());
        Assert.assertEquals(position.getCol(), newPosition.getCol());

        // ensure position remains after persist
        childSpc.persist();
        childSpc.reload();
        newPosition = childSpc.getPosition();
        Assert.assertEquals(position.getRow(), newPosition.getRow());
        Assert.assertEquals(position.getCol(), newPosition.getCol());

        // test setting position to null
        childSpc.setParent(null, null);
        childSpc.persist();
        childSpc.reload();
        Assert.assertEquals(null, childSpc.getPosition());
        Assert.assertEquals(null, childSpc.getParentContainer());
    }

    @Test
    public void testGetSetParent() throws Exception {
        Assert.assertTrue(childSpc.hasParent());
        ContainerWrapper oldParent = childSpc.getParentContainer();
        ContainerTypeWrapper type = ContainerTypeHelper.addContainerType(site,
            "newCtType", "ctNew", 1, 4, 5, true);
        type.addToSpecimenTypeCollection(Arrays.asList(childSpc
            .getSpecimenType()));
        type.persist();
        ContainerWrapper parent = ContainerHelper.addContainer(
            "newcontainerParent", "ccNew", site, type);

        childSpc.setParent(parent, childSpc.getPosition());
        childSpc.persist();
        // check to make sure gone from old parent
        oldParent.reload();
        Assert.assertTrue(oldParent.getSpecimens().size() == 0);
        // check to make sure added to new parent
        parent.reload();
        Assert.assertTrue(childSpc.getParentContainer() != null);
        Collection<SpecimenWrapper> sampleWrappers = parent.getSpecimens()
            .values();
        boolean found = false;
        for (SpecimenWrapper sampleWrapper : sampleWrappers) {
            if (sampleWrapper.getId().equals(childSpc.getId()))
                found = true;
        }
        Assert.assertTrue(found);

        // test for no parent
        SpecimenWrapper specimen2 = new SpecimenWrapper(appService);
        Assert.assertFalse(specimen2.hasParent());
    }

    @Test
    public void testGetSetSpecimenType() throws BiobankCheckException,
        Exception {
        SpecimenTypeWrapper stw = parentSpc.getSpecimenType();
        SpecimenTypeWrapper newType = SpecimenTypeHelper
            .addSpecimenType("newStw");
        stw.persist();
        Assert.assertTrue(stw.getId() != newType.getId());
        parentSpc.setSpecimenType(newType);
        Assert.assertTrue(newType.getId() == parentSpc.getSpecimenType()
            .getId());

        SpecimenWrapper sample1 = new SpecimenWrapper(appService);
        sample1.setSpecimenType(null);
        Assert.assertNull(sample1.getSpecimenType());
    }

    @Test
    public void testGetSetQuantityFromType() throws Exception {
        Double quantity = parentSpc.getQuantity();
        parentSpc.setQuantityFromType();
        // no sample storages defined yet, should be null
        Assert.assertTrue(quantity == null);

        ActivityStatusWrapper activeStatus = ActivityStatusWrapper
            .getActiveActivityStatus(appService);

        AliquotedSpecimenWrapper ss1 = new AliquotedSpecimenWrapper(appService);
        ss1.setSpecimenType(SpecimenTypeHelper.addSpecimenType("ss1"));
        ss1.setVolume(1.0);
        ss1.setStudy(parentSpc.getCollectionEvent().getPatient().getStudy());
        ss1.setActivityStatus(activeStatus);
        ss1.persist();
        AliquotedSpecimenWrapper ss2 = new AliquotedSpecimenWrapper(appService);
        ss2.setSpecimenType(SpecimenTypeHelper.addSpecimenType("ss2"));
        ss2.setVolume(2.0);
        ss2.setStudy(parentSpc.getCollectionEvent().getPatient().getStudy());
        ss2.setActivityStatus(activeStatus);
        ss2.persist();
        AliquotedSpecimenWrapper ss3 = new AliquotedSpecimenWrapper(appService);
        ss3.setSpecimenType(parentSpc.getSpecimenType());
        ss3.setVolume(3.0);
        ss3.setStudy(parentSpc.getCollectionEvent().getPatient().getStudy());
        ss3.setActivityStatus(activeStatus);
        ss3.persist();
        parentSpc.getCollectionEvent().getPatient().getStudy()
            .addToAliquotedSpecimenCollection(Arrays.asList(ss1, ss2, ss3));
        // should be 3
        parentSpc.setQuantityFromType();
        Assert.assertTrue(parentSpc.getQuantity().equals(3.0));
    }

    @Test
    public void testGetFormattedLinkDate() throws Exception {
        Date date = Utils.getRandomDate();
        parentSpc.setCreatedAt(date);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Assert.assertTrue(sdf.format(date).equals(
            parentSpc.getFormattedCreatedAt()));
    }

    @Test
    public void testCompareTo() throws BiobankCheckException, Exception {
        parentSpc.setInventoryId("defgh");
        parentSpc.persist();
        SpecimenWrapper sample2 = SpecimenHelper.newSpecimen(parentSpc,
            childSpc.getSpecimenType(),
            ActivityStatusWrapper.ACTIVE_STATUS_STRING,
            parentSpc.getCollectionEvent(), childSpc.getProcessingEvent(),
            childSpc.getParentContainer(), 2, 3);
        sample2.setInventoryId("awert");
        sample2.persist();
        Assert.assertTrue(parentSpc.compareTo(sample2) > 0);

        sample2.setInventoryId("qwerty");
        sample2.persist();
        Assert.assertTrue(parentSpc.compareTo(sample2) < 0);
    }

    @Test
    public void testGetSpecimen() throws Exception {
        ContainerWrapper container = childSpc.getParentContainer();
        ContainerTypeWrapper containerType = container.getContainerType();
        SpecimenTypeWrapper sampleType = containerType
            .getSpecimenTypeCollection(false).get(0);
        Assert.assertNotNull(sampleType);
        childSpc.setInventoryId(Utils.getRandomString(5));
        childSpc.persist();

        SpecimenHelper.newSpecimen(childSpc, childSpc.getSpecimenType(),
            ActivityStatusWrapper.ACTIVE_STATUS_STRING,
            childSpc.getCollectionEvent(), childSpc.getProcessingEvent(),
            childSpc.getParentContainer(), 3, 3);

        SpecimenWrapper foundSpecimen = SpecimenWrapper.getSpecimen(appService,
            childSpc.getInventoryId(), null);
        Assert.assertNotNull(foundSpecimen);
        Assert.assertEquals(foundSpecimen, childSpc);
    }

    @Test
    public void testGetSpecimensNonActive() throws Exception {
        ContainerWrapper container = childSpc.getParentContainer();
        ContainerTypeWrapper containerType = container.getContainerType();
        SpecimenTypeWrapper sampleType = containerType
            .getSpecimenTypeCollection(false).get(0);
        Assert.assertNotNull(sampleType);

        ActivityStatusWrapper activityStatusActive = ActivityStatusWrapper
            .getActiveActivityStatus(appService);
        ActivityStatusWrapper activityStatusNonActive = null;
        for (ActivityStatusWrapper a : ActivityStatusWrapper
            .getAllActivityStatuses(appService)) {
            if (!a.equals(activityStatusActive)) {
                activityStatusNonActive = a;
                break;
            }
        }

        List<SpecimenWrapper> activeSpecimens = new ArrayList<SpecimenWrapper>();
        List<SpecimenWrapper> nonActiveSpecimens = new ArrayList<SpecimenWrapper>();

        activeSpecimens.add(childSpc);
        for (int i = 1, n = container.getColCapacity(); i < n; ++i) {
            activeSpecimens.add(SpecimenHelper.newSpecimen(parentSpc,
                childSpc.getSpecimenType(),
                ActivityStatusWrapper.ACTIVE_STATUS_STRING,
                childSpc.getCollectionEvent(), childSpc.getProcessingEvent(),
                childSpc.getParentContainer(), 0, i));

            SpecimenWrapper a = SpecimenHelper.newSpecimen(parentSpc,
                childSpc.getSpecimenType(),
                ActivityStatusWrapper.ACTIVE_STATUS_STRING,
                childSpc.getCollectionEvent(), childSpc.getProcessingEvent(),
                childSpc.getParentContainer(), 1, i);
            a.setActivityStatus(activityStatusNonActive);
            a.persist();
            nonActiveSpecimens.add(a);
        }

        List<SpecimenWrapper> specimens = SpecimenWrapper
            .getSpecimensNonActiveInCenter(appService, site);
        Assert.assertEquals(nonActiveSpecimens.size(), specimens.size());
        Assert.assertTrue(specimens.containsAll(nonActiveSpecimens));
        Assert.assertFalse(specimens.containsAll(activeSpecimens));
    }

    @Test
    public void testGetSpecimensInSiteWithPositionLabel() throws Exception {
        ContainerWrapper container = childSpc.getParentContainer();
        ContainerTypeWrapper containerType = container.getContainerType();
        SpecimenTypeWrapper sampleType = containerType
            .getSpecimenTypeCollection(false).get(0);
        Assert.assertNotNull(sampleType);
        childSpc.setInventoryId(Utils.getRandomString(5));
        childSpc.persist();

        SpecimenHelper.newSpecimen(childSpc, childSpc.getSpecimenType(),
            ActivityStatusWrapper.ACTIVE_STATUS_STRING,
            childSpc.getCollectionEvent(), childSpc.getProcessingEvent(),
            childSpc.getParentContainer(), 0, 1);

        SpecimenHelper.newSpecimen(childSpc, childSpc.getSpecimenType(),
            ActivityStatusWrapper.ACTIVE_STATUS_STRING,
            childSpc.getCollectionEvent(), childSpc.getProcessingEvent(),
            childSpc.getParentContainer(), 1, 0);

        childSpc = SpecimenHelper.newSpecimen(childSpc,
            childSpc.getSpecimenType(),
            ActivityStatusWrapper.ACTIVE_STATUS_STRING,
            childSpc.getCollectionEvent(), childSpc.getProcessingEvent(),
            childSpc.getParentContainer(), 0, 2);
        childSpc.setInventoryId(Utils.getRandomString(5));
        childSpc.persist();

        List<SpecimenWrapper> specimens = SpecimenWrapper
            .getSpecimensInSiteWithPositionLabel(appService, site,
                childSpc.getPositionString(true, false));
        Assert.assertEquals(1, specimens.size());
        Assert.assertEquals(specimens.get(0), childSpc);
    }

    @Test
    public void testResetAlreadyInDatabase() throws Exception {
        parentSpc.persist();
        String old = parentSpc.getInventoryId();
        parentSpc.setInventoryId("toto");
        parentSpc.reset();
        Assert.assertEquals(old, parentSpc.getInventoryId());
    }

    @Test
    public void testResetNew() throws Exception {
        SpecimenWrapper newSpec = SpecimenHelper.newSpecimen(parentSpc
            .getSpecimenType());
        newSpec.setInventoryId("toto");
        newSpec.reset();
        Assert.assertEquals(null, newSpec.getInventoryId());
    }

    @Test
    public void testCheckPosition() throws BiobankCheckException, Exception {
        ContainerWrapper container = childSpc.getParentContainer();

        Assert.assertFalse(container.isPositionFree(childSpc.getPosition()));
        Assert.assertTrue(container.isPositionFree(new RowColPos(2, 3)));
    }

    @Test
    public void testDebugRandomMethods() throws Exception {
        ContainerWrapper container = childSpc.getParentContainer();
        ContainerTypeWrapper containerType = container.getContainerType();
        SpecimenTypeWrapper spcType = containerType.getSpecimenTypeCollection(
            false).get(0);
        Assert.assertNotNull(spcType);

        ProcessingEventWrapper pevent = ProcessingEventHelper
            .addProcessingEvent(childSpc.getCurrentCenter(), childSpc
                .getCollectionEvent().getPatient(), Utils.getRandomDate());

        // add aliquoted specimen
        SpecimenWrapper specimen = SpecimenHelper.newSpecimen(parentSpc,
            childSpc.getSpecimenType(),
            ActivityStatusWrapper.ACTIVE_STATUS_STRING,
            childSpc.getCollectionEvent(), childSpc.getProcessingEvent(),
            childSpc.getParentContainer(), 2, 3);
        specimen.setInventoryId(Utils.getRandomString(5));
        specimen.persist();

        pevent.addToSpecimenCollection(Arrays.asList(parentSpc));
        pevent.persist();

        SpecimenWrapper s2 = SpecimenHelper.newSpecimen(childSpc,
            childSpc.getSpecimenType(),
            ActivityStatusWrapper.ACTIVE_STATUS_STRING,
            childSpc.getCollectionEvent(), childSpc.getProcessingEvent(),
            childSpc.getParentContainer(), 2, 4);
        s2.setParent(null, null);
        s2.setParentSpecimen(null);
        s2.persist();

        try {
            Assert.assertTrue(DebugUtil.getRandomLinkedAliquotedSpecimens(
                appService, site.getId()).size() > 0);
            Assert.assertTrue(DebugUtil.getRandomAssignedSpecimens(appService,
                site.getId()).size() > 0);
            List<SpecimenWrapper> randomNonAssociatedNonDispatchedSpecimens = DebugUtil
                .getRandomNonAssignedNonDispatchedSpecimens(appService,
                    site.getId(), 10);
            Assert
                .assertTrue(randomNonAssociatedNonDispatchedSpecimens.size() > 0);
        } catch (Exception e) {
            Assert.fail(e.getCause().getMessage());
        }

        try {
            s2.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * childSpc created in setUp()
     */
    @Test
    public void testGetDispatches() throws Exception {
        String name = "testGetDispatches" + r.nextInt();
        SiteWrapper destSite = SiteHelper.addSite(name);
        ShippingMethodWrapper method = ShippingMethodWrapper
            .getShippingMethods(appService).get(0);
        DispatchWrapper d = DispatchHelper.addDispatch(site, destSite, method);

        d.addSpecimens(Arrays.asList(childSpc), DispatchSpecimenState.NONE);
        d.persist();
        childSpc.reload();

        List<DispatchWrapper> specimenDispatches = childSpc.getDispatches();
        Assert.assertEquals(1, specimenDispatches.size());
        Assert.assertTrue(specimenDispatches.contains(d));

        Assert.assertTrue(d.isInCreationState());

        // site send specimens
        d.setState(DispatchState.IN_TRANSIT);
        d.persist();
        Assert.assertTrue(d.isInTransitState());

        // dest site receive specimen
        d.setState(DispatchState.RECEIVED);
        d.receiveSpecimens(Arrays.asList(childSpc));
        d.persist();
        Assert.assertTrue(d.isInReceivedState());

        // make sure spc now belongs to destSite
        destSite.reload();
        Assert.assertTrue(destSite.getSpecimenCollection(false).contains(
            childSpc));

        // dispatch specimen to second site
        SiteWrapper destSite2 = SiteHelper.addSite(name + "_2");

        DispatchWrapper d2 = DispatchHelper.addDispatch(destSite, destSite2,
            method);
        d2.addSpecimens(Arrays.asList(childSpc), DispatchSpecimenState.NONE);

        parentSpc.reload();
        // assign a position to this specimen
        ContainerTypeWrapper topType = ContainerTypeHelper.addContainerType(
            destSite, "ct11", "ct11", 1, 5, 6, true);
        ContainerWrapper topCont = ContainerHelper.addContainer("11", "11",
            destSite, topType);
        ContainerTypeWrapper childType = ContainerTypeHelper.addContainerType(
            destSite, "ct22", "ct22", 2, 4, 7, false);
        topType.addToChildContainerTypeCollection(Arrays.asList(childType));
        topType.persist();
        ContainerWrapper cont = ContainerHelper.addContainer("22", "22",
            topCont, destSite, childType, 4, 5);
        childType.addToSpecimenTypeCollection(Arrays.asList(childSpc
            .getSpecimenType()));
        childType.persist();
        cont.reload();
        cont.addSpecimen(2, 3, childSpc);
        parentSpc.persist();

        // add to new dispatch
        d2.addSpecimens(Arrays.asList(childSpc), DispatchSpecimenState.NONE);
        d2.persist();

        // make sure spc still belongs to destSite
        destSite2.reload();
        Assert.assertTrue(destSite.getSpecimenCollection(false).contains(
            childSpc));

        childSpc.reload();
        specimenDispatches = childSpc.getDispatches();
        Assert.assertEquals(2, specimenDispatches.size());
        Assert.assertTrue(specimenDispatches.contains(d));
        Assert.assertTrue(specimenDispatches.contains(d2));
    }
}
