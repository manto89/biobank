package edu.ualberta.med.biobank.common.permission.dispatch;

import edu.ualberta.med.biobank.common.action.ActionContext;
import edu.ualberta.med.biobank.common.permission.Permission;
import edu.ualberta.med.biobank.model.Center;
import edu.ualberta.med.biobank.model.PermissionEnum;

public class DispatchCreatePermission implements Permission {

    private static final long serialVersionUID = 3389344794449225238L;
    private Integer centerId;

    public DispatchCreatePermission(Integer centerId) {
        this.centerId = centerId;
    }

    @Override
    public boolean isAllowed(ActionContext context) {
        return PermissionEnum.DISPATCH_CREATE.isAllowed(context.getUser(),
            context.load(Center.class, centerId));
    }

}
