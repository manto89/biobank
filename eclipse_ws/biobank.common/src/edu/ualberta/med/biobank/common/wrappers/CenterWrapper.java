package edu.ualberta.med.biobank.common.wrappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.ualberta.med.biobank.common.exception.BiobankException;
import edu.ualberta.med.biobank.common.peer.AddressPeer;
import edu.ualberta.med.biobank.common.peer.CenterPeer;
import edu.ualberta.med.biobank.common.peer.CollectionEventPeer;
import edu.ualberta.med.biobank.common.peer.ProcessingEventPeer;
import edu.ualberta.med.biobank.common.peer.SitePeer;
import edu.ualberta.med.biobank.common.peer.SpecimenPeer;
import edu.ualberta.med.biobank.common.util.DispatchState;
import edu.ualberta.med.biobank.common.wrappers.base.CenterBaseWrapper;
import edu.ualberta.med.biobank.common.wrappers.internal.AddressWrapper;
import edu.ualberta.med.biobank.model.Center;
import edu.ualberta.med.biobank.model.ProcessingEvent;
import edu.ualberta.med.biobank.model.Site;
import edu.ualberta.med.biobank.model.Specimen;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

public abstract class CenterWrapper<E extends Center> extends
    CenterBaseWrapper<E> {
    private static final String ALL_CENTERS_HQL_STRING = "from "
        + Center.class.getName();

    private Set<CollectionEventWrapper> deletedCollectionEvents = new HashSet<CollectionEventWrapper>();

    public CenterWrapper(WritableApplicationService appService) {
        super(appService);
    }

    public CenterWrapper(WritableApplicationService appService, E c) {
        super(appService, c);
    }

    @Override
    protected List<String> getPropertyChangeNames() {
        List<String> all = new ArrayList<String>(super.getPropertyChangeNames());
        all.addAll(AddressPeer.PROP_NAMES);
        return all;
    }

    private AddressWrapper initAddress() {
        AddressWrapper address = getAddress();
        if (address == null) {
            address = new AddressWrapper(appService);
            setAddress(address);
        }
        return address;
    }

    public String getStreet1() {
        return getProperty(getAddress(), AddressPeer.STREET1);
    }

    public void setStreet1(String street1) {
        initAddress().setProperty(AddressPeer.STREET1, street1);
    }

    public String getStreet2() {
        return getProperty(getAddress(), AddressPeer.STREET2);
    }

    public void setStreet2(String street2) {
        initAddress().setProperty(AddressPeer.STREET2, street2);
    }

    public String getCity() {
        return getProperty(getAddress(), AddressPeer.CITY);
    }

    public void setCity(String city) {
        initAddress().setProperty(AddressPeer.CITY, city);
    }

    public String getProvince() {
        return getProperty(getAddress(), AddressPeer.PROVINCE);
    }

    public void setProvince(String province) {
        initAddress().setProperty(AddressPeer.PROVINCE, province);
    }

    public String getPostalCode() {
        return getProperty(getAddress(), AddressPeer.POSTAL_CODE);
    }

    public void setPostalCode(String postalCode) {
        initAddress().setProperty(AddressPeer.POSTAL_CODE, postalCode);
    }

    public static final String PROCESSING_EVENT_COUNT_QRY = "select count(proc) from "
        + ProcessingEvent.class.getName()
        + " as proc where "
        + Property.concatNames(ProcessingEventPeer.CENTER, CenterPeer.ID)
        + " = ?";

    public long getProcessingEventCount() throws ApplicationException,
        BiobankException {
        return getProcessingEventCount(false);
    }

    /**
     * fast = true will execute a hql query. fast = false will call the
     * getCollectionEventCollection().size method
     */
    public long getProcessingEventCount(boolean fast)
        throws ApplicationException, BiobankException {
        if (fast) {
            HQLCriteria criteria = new HQLCriteria(PROCESSING_EVENT_COUNT_QRY,
                Arrays.asList(new Object[] { getId() }));
            return getCountResult(appService, criteria);
        }
        List<ProcessingEventWrapper> list = getProcessingEventCollection(false);
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    /**
     * Collection event count for this center. This count is different for each
     * center: the method should be defined in each center type
     */
    public abstract long getCollectionEventCountForStudy(StudyWrapper study)
        throws ApplicationException, BiobankException;

    /**
     * Collection event count for this center. This count is different for each
     * center: the method should be defined in each center type
     */
    public abstract long getPatientCountForStudy(StudyWrapper study)
        throws ApplicationException, BiobankException;

    @Override
    protected void persistDependencies(Center origObject) throws Exception {
        deleteCollectionEvents();
    }

    private void deleteCollectionEvents() throws Exception {
        for (CollectionEventWrapper ce : deletedCollectionEvents) {
            if (!ce.isNew()) {
                ce.delete();
            }
        }
    }

    public static List<CenterWrapper<?>> getCenters(
        WritableApplicationService appService) throws ApplicationException {
        StringBuilder qry = new StringBuilder(ALL_CENTERS_HQL_STRING);
        HQLCriteria criteria = new HQLCriteria(qry.toString(),
            new ArrayList<Object>());

        List<Center> centers = appService.query(criteria);
        List<CenterWrapper<?>> centerWrappers = ModelWrapper
            .wrapModelCollection(appService, centers, null);

        return centerWrappers;
    }

    public static List<CenterWrapper<?>> getOtherCenters(
        WritableApplicationService appService, CenterWrapper<?> center)
        throws ApplicationException {
        List<CenterWrapper<?>> centers = getCenters(appService);
        centers.remove(center);
        return centers;
    }

    @SuppressWarnings("unchecked")
    public List<DispatchWrapper> getInTransitSentDispatchCollection() {
        List<DispatchWrapper> shipCollection = (List<DispatchWrapper>) propertiesMap
            .get("inTransitSentDispatchCollection");
        if (shipCollection == null) {
            List<DispatchWrapper> children = getSrcDispatchCollection(false);
            if (children != null) {
                shipCollection = new ArrayList<DispatchWrapper>();
                for (DispatchWrapper dispatch : children) {
                    if (DispatchState.IN_TRANSIT.equals(dispatch
                        .getDispatchState())) {
                        shipCollection.add(dispatch);
                    }
                }
                propertiesMap.put("inTransitSentDispatchCollection",
                    shipCollection);
            }
        }
        return shipCollection;
    }

    @SuppressWarnings("unchecked")
    public List<DispatchWrapper> getInTransitReceiveDispatchCollection() {
        List<DispatchWrapper> shipCollection = (List<DispatchWrapper>) propertiesMap
            .get("inTransitReceiveDispatchCollection");
        if (shipCollection == null) {
            List<DispatchWrapper> children = getDstDispatchCollection(false);
            if (children != null) {
                shipCollection = new ArrayList<DispatchWrapper>();
                for (DispatchWrapper dispatch : children) {
                    if (DispatchState.IN_TRANSIT.equals(dispatch
                        .getDispatchState())) {
                        shipCollection.add(dispatch);
                    }
                }
                propertiesMap.put("inTransitReceiveDispatchCollection",
                    shipCollection);
            }
        }
        return shipCollection;
    }

    @SuppressWarnings("unchecked")
    public List<DispatchWrapper> getReceivingNoErrorsDispatchCollection() {
        List<DispatchWrapper> shipCollection = (List<DispatchWrapper>) propertiesMap
            .get("receivingDispatchCollection");
        if (shipCollection == null) {
            List<DispatchWrapper> children = getDstDispatchCollection(false);
            if (children != null) {
                shipCollection = new ArrayList<DispatchWrapper>();
                for (DispatchWrapper dispatch : children) {
                    if (DispatchState.RECEIVED.equals(dispatch
                        .getDispatchState()) && !dispatch.hasErrors()) {
                        shipCollection.add(dispatch);
                    }
                }
                propertiesMap
                    .put("receivingDispatchCollection", shipCollection);
            }
        }
        return shipCollection;
    }

    @SuppressWarnings("unchecked")
    public List<DispatchWrapper> getReceivingWithErrorsDispatchCollection() {
        List<DispatchWrapper> shipCollection = (List<DispatchWrapper>) propertiesMap
            .get("receivingWithErrorsDispatchCollection");
        if (shipCollection == null) {
            List<DispatchWrapper> children = getDstDispatchCollection(false);
            if (children != null) {
                shipCollection = new ArrayList<DispatchWrapper>();
                for (DispatchWrapper dispatch : children) {
                    if (DispatchState.RECEIVED.equals(dispatch
                        .getDispatchState()) && dispatch.hasErrors()) {
                        shipCollection.add(dispatch);
                    }
                }
                propertiesMap.put("receivingWithErrorsDispatchCollection",
                    shipCollection);
            }
        }
        return shipCollection;
    }

    @SuppressWarnings("unchecked")
    public List<DispatchWrapper> getInCreationDispatchCollection() {
        List<DispatchWrapper> shipCollection = (List<DispatchWrapper>) propertiesMap
            .get("inCreationDispatchCollection");
        if (shipCollection == null) {
            List<DispatchWrapper> children = getSrcDispatchCollection(false);
            if (children != null) {
                shipCollection = new ArrayList<DispatchWrapper>();
                for (DispatchWrapper dispatch : children) {
                    if (DispatchState.CREATION.equals(dispatch
                        .getDispatchState())) {
                        shipCollection.add(dispatch);
                    }
                }
                propertiesMap.put("inCreationDispatchCollection",
                    shipCollection);
            }
        }
        return shipCollection;
    }

    public static Collection<? extends ModelWrapper<?>> getInTransitReceiveDispatchCollection(
        CenterWrapper<?> center) {
        return center.getInTransitReceiveDispatchCollection();
    }

    public static Collection<? extends ModelWrapper<?>> getReceivingNoErrorsDispatchCollection(
        CenterWrapper<?> center) {
        return center.getReceivingNoErrorsDispatchCollection();
    }

    public static Collection<? extends ModelWrapper<?>> getInCreationDispatchCollection(
        CenterWrapper<?> center) {
        return center.getInCreationDispatchCollection();
    }

    public static Collection<? extends ModelWrapper<?>> getReceivingWithErrorsDispatchCollection(
        CenterWrapper<?> center) {
        return center.getReceivingWithErrorsDispatchCollection();
    }

    public static Collection<? extends ModelWrapper<?>> getInTransitSentDispatchCollection(
        CenterWrapper<?> center) {
        return center.getInTransitSentDispatchCollection();
    }

    private static final String PATIENT_COUNT_QRY = "select count(distinct cevent."
        + CollectionEventPeer.PATIENT.getName()
        + ") from "
        + Site.class.getName()
        + " as site join site."
        + SitePeer.SPECIMEN_COLLECTION.getName()
        + " as spcs join spcs."
        + SpecimenPeer.COLLECTION_EVENT.getName()
        + " as cevent where site."
        + SitePeer.ID.getName() + "=?";

    public Long getPatientCount() throws Exception {
        HQLCriteria criteria = new HQLCriteria(PATIENT_COUNT_QRY,
            Arrays.asList(new Object[] { getId() }));
        return getCountResult(appService, criteria);
    }

    private static final String CHILD_SPECIMENS_COUNT_QRY = "select count(childSpcs) from "
        + Specimen.class.getName()
        + " sp join sp."
        + SpecimenPeer.CHILD_SPECIMEN_COLLECTION.getName()
        + " as childSpcs where childSpcs."
        + Property.concatNames(SpecimenPeer.CURRENT_CENTER, CenterPeer.ID)
        + "=?";

    public Long getAliquotedSpecimenCount() throws Exception {
        HQLCriteria criteria = new HQLCriteria(CHILD_SPECIMENS_COUNT_QRY,
            Arrays.asList(new Object[] { getId() }));
        return getCountResult(appService, criteria);
    }

    public static final String COLLECTION_EVENT_COUNT_QRY = "select count(distinct cevent) from "
        + Center.class.getName()
        + " as c join c."
        + CenterPeer.SPECIMEN_COLLECTION.getName()
        + " as spcs join spcs."
        + SpecimenPeer.COLLECTION_EVENT.getName()
        + " as cevent where c."
        + CenterPeer.ID.getName() + "=?";

    /**
     * Count events for specimen that are currently at this site
     */
    public long getCollectionEventCount() throws ApplicationException,
        BiobankException {
        HQLCriteria criteria = new HQLCriteria(COLLECTION_EVENT_COUNT_QRY,
            Arrays.asList(new Object[] { getId() }));
        return getCountResult(appService, criteria);
    }
}