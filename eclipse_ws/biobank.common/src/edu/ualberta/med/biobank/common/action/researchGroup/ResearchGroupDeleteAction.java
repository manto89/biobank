package edu.ualberta.med.biobank.common.action.researchGroup;

import edu.ualberta.med.biobank.common.action.Action;
import edu.ualberta.med.biobank.common.action.ActionContext;
import edu.ualberta.med.biobank.common.action.EmptyResult;
import edu.ualberta.med.biobank.common.action.exception.ActionException;
import edu.ualberta.med.biobank.common.permission.researchGroup.ResearchGroupDeletePermission;
import edu.ualberta.med.biobank.model.ResearchGroup;

public class ResearchGroupDeleteAction implements Action<EmptyResult> {
    private static final long serialVersionUID = 1L;

    protected Integer rgId = null;

    public ResearchGroupDeleteAction(Integer id) {
        this.rgId = id;
    }

    @Override
    public boolean isAllowed(ActionContext context) {
        return new ResearchGroupDeletePermission(rgId).isAllowed(context);
    }

    @Override
    public EmptyResult run(ActionContext context) throws ActionException {
        // FIXME: this should work but doesn't
        ResearchGroup rg = context.get(ResearchGroup.class, rgId);
        if (rg.getRequestCollection().size() > 0)
            throw new ActionException(
                "ResearchGroups with requests may not be deleted.");
        context.getSession().delete(rg);
        return new EmptyResult();
    }
}
