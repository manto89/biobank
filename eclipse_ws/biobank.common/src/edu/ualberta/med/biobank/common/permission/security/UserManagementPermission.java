package edu.ualberta.med.biobank.common.permission.security;

import edu.ualberta.med.biobank.common.action.ActionContext;
import edu.ualberta.med.biobank.common.permission.Permission;
import edu.ualberta.med.biobank.common.permission.PermissionEnum;

public class UserManagementPermission implements Permission {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean isAllowed(ActionContext context) {
        return PermissionEnum.USER_MANAGEMENT.isAllowed(context.getUser());
    }
}
