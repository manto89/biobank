package edu.ualberta.med.biobank.common.permission.collectionEvent;

import edu.ualberta.med.biobank.common.action.ActionContext;
import edu.ualberta.med.biobank.common.permission.Permission;
import edu.ualberta.med.biobank.model.CollectionEvent;
import edu.ualberta.med.biobank.model.PermissionEnum;

public class CollectionEventDeletePermission implements Permission {

    private static final long serialVersionUID = 1L;
    private Integer ceventId;

    public CollectionEventDeletePermission(Integer ceventId) {
        this.ceventId = ceventId;
    }

    @Override
    public boolean isAllowed(ActionContext context) {
        CollectionEvent cevent = context.load(CollectionEvent.class,
            ceventId);
        return PermissionEnum.COLLECTION_EVENT_DELETE
            .isAllowed(context.getUser(), cevent.getPatient().getStudy());
    }

}
