package edu.ualberta.med.biobank.common.wrappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.ualberta.med.biobank.common.wrappers.base.BbGroupBaseWrapper;
import edu.ualberta.med.biobank.model.BbGroup;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

public class BbGroupWrapper extends BbGroupBaseWrapper {

    public BbGroupWrapper(WritableApplicationService appService,
        BbGroup wrappedObject) {
        super(appService, wrappedObject);
    }

    public BbGroupWrapper(WritableApplicationService appService) {
        super(appService);
    }

    private static final String ALL_GROUPS_QRY = " from "
        + BbGroup.class.getName();

    public static final List<BbGroupWrapper> getAllGroups(
        WritableApplicationService appService) throws ApplicationException {
        HQLCriteria criteria = new HQLCriteria(ALL_GROUPS_QRY,
            new ArrayList<Object>());

        List<BbGroup> groups = appService.query(criteria);
        return ModelWrapper.wrapModelCollection(appService, groups,
            BbGroupWrapper.class);
    }

    @Deprecated
    public List<Integer> getWorkingCenterIds() {
        // FIXME comes from all Group object
        return Collections.emptyList();
    }

    @Deprecated
    public List<Integer> getCenterFeaturesEnabled() {
        // FIXME comes from all Group object
        return null;
    }

    @Deprecated
    public void setWorkingCenterIds(List<Integer> selected) {

    }

    @Deprecated
    public void setCenterFeaturesEnabled(List<Integer> selected) {

    }

    @Override
    public int compareTo(ModelWrapper<BbGroup> group2) {
        if (group2 instanceof BbGroupWrapper) {
            String name1 = getName();
            String name2 = ((BbGroupWrapper) group2).getName();

            if (name1 == null || name2 == null)
                return 0;
            return name1.compareTo(name2);
        }
        return 0;
    }

    /**
     * Duplicate a group: create a new one that will have the exact same
     * relations. This duplicated group is not yet saved into the DB.
     */
    public BbGroupWrapper duplicate() {
        BbGroupWrapper newGroup = new BbGroupWrapper(appService);
        newGroup.setName(getName());
        // FIXME also copy relations
        return newGroup;
    }

}
