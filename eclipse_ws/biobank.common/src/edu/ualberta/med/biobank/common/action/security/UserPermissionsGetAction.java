package edu.ualberta.med.biobank.common.action.security;

import edu.ualberta.med.biobank.common.action.Action;
import edu.ualberta.med.biobank.common.action.ActionContext;
import edu.ualberta.med.biobank.common.action.ActionResult;
import edu.ualberta.med.biobank.common.action.exception.ActionException;
import edu.ualberta.med.biobank.common.action.security.UserPermissionsGetAction.UserCreatePermissions;
import edu.ualberta.med.biobank.common.permission.GlobalAdminPermission;
import edu.ualberta.med.biobank.common.permission.clinic.ClinicCreatePermission;
import edu.ualberta.med.biobank.common.permission.container.ContainerCreatePermission;
import edu.ualberta.med.biobank.common.permission.containerType.ContainerTypeCreatePermission;
import edu.ualberta.med.biobank.common.permission.dispatch.DispatchCreatePermission;
import edu.ualberta.med.biobank.common.permission.labelPrinting.LabelPrintingPermission;
import edu.ualberta.med.biobank.common.permission.patient.PatientCreatePermission;
import edu.ualberta.med.biobank.common.permission.processingEvent.ProcessingEventCreatePermission;
import edu.ualberta.med.biobank.common.permission.researchGroup.ResearchGroupCreatePermission;
import edu.ualberta.med.biobank.common.permission.security.UserManagerPermission;
import edu.ualberta.med.biobank.common.permission.shipment.OriginInfoUpdatePermission;
import edu.ualberta.med.biobank.common.permission.site.SiteCreatePermission;
import edu.ualberta.med.biobank.common.permission.specimen.BatchOperationPermission;
import edu.ualberta.med.biobank.common.permission.specimen.SpecimenAssignPermission;
import edu.ualberta.med.biobank.common.permission.specimen.SpecimenLinkPermission;
import edu.ualberta.med.biobank.common.permission.specimenType.SpecimenTypeCreatePermission;
import edu.ualberta.med.biobank.common.permission.study.StudyCreatePermission;
import edu.ualberta.med.biobank.model.Center;
import edu.ualberta.med.biobank.model.Site;

/**
 * Note: center ID can be null and the permissions that require a working center
 * will be left as false.
 * 
 * @author Nelson Loyola
 * 
 */
public class UserPermissionsGetAction implements Action<UserCreatePermissions> {
    private static final long serialVersionUID = 1L;

    private final Integer centerId;

    public UserPermissionsGetAction() {
        this.centerId = null;
    }

    public UserPermissionsGetAction(Center center) {
        this.centerId = center.getId();
    }

    @Override
    public boolean isAllowed(ActionContext context) throws ActionException {
        return true;
    }

    @Override
    public UserCreatePermissions run(ActionContext context)
        throws ActionException {
        UserCreatePermissions p = new UserCreatePermissions();

        p.clinicCreatePermission =
            new ClinicCreatePermission().isAllowed(context);
        p.globalAdminPermission =
            new GlobalAdminPermission().isAllowed(context);
        p.researchGroupCreatePermission =
            new ResearchGroupCreatePermission().isAllowed(context);
        p.siteCreatePermission = new SiteCreatePermission().isAllowed(context);
        p.specimenTypeCreatePermission =
            new SpecimenTypeCreatePermission().isAllowed(context);
        p.studyCreatePermission =
            new StudyCreatePermission().isAllowed(context);
        p.userManagerPermission =
            new UserManagerPermission().isAllowed(context);
        p.labelPrintingPermission = new LabelPrintingPermission()
        .isAllowed(context);
        p.patientCreatePermission =
            new PatientCreatePermission(null).isAllowed(context);

        if (centerId != null) {
            p.containerCreatePermission =
                new ContainerCreatePermission(centerId).isAllowed(context);
            p.containerTypeCreatePermission =
                new ContainerTypeCreatePermission(centerId).isAllowed(context);
            p.dispatchCreatePermission =
                new DispatchCreatePermission(centerId).isAllowed(context);
            p.originInfoUpdatePermission =
                new OriginInfoUpdatePermission(centerId).isAllowed(context);
            p.specimenLinkPermission =
                new SpecimenLinkPermission(centerId, null).isAllowed(context);

            p.batchOperationPermission =
                new BatchOperationPermission(centerId, null).isAllowed(context);

            // specimen assign not allowed for clinics
            Site site = context.get(Site.class, centerId);
            p.specimenAssignPermission = (site != null)
                && new SpecimenAssignPermission(centerId).isAllowed(context);

            Center center = context.load(Center.class, centerId);
            p.processingEventCreatePermission =
                new ProcessingEventCreatePermission(center)
            .isAllowed(context);
        }

        return p;
    }

    public static class UserCreatePermissions implements ActionResult {
        private static final long serialVersionUID = 1L;

        private boolean clinicCreatePermission;
        private boolean containerCreatePermission;
        private boolean containerTypeCreatePermission;
        private boolean dispatchCreatePermission;
        private boolean globalAdminPermission;
        private boolean originInfoUpdatePermission;
        private boolean patientCreatePermission;
        private boolean patientMergePermission;
        private boolean processingEventCreatePermission;
        private boolean researchGroupCreatePermission;
        private boolean siteCreatePermission;
        private boolean specimenAssignPermission;
        private boolean specimenLinkPermission;
        private boolean specimenTypeCreatePermission;
        private boolean studyCreatePermission;
        private boolean userManagerPermission;
        private boolean labelPrintingPermission;
        private boolean batchOperationPermission;

        public static long getSerialversionuid() {
            return serialVersionUID;
        }

        public boolean isClinicCreatePermission() {
            return clinicCreatePermission;
        }

        public boolean isContainerCreatePermission() {
            return containerCreatePermission;
        }

        public boolean isContainerTypeCreatePermission() {
            return containerTypeCreatePermission;
        }

        public boolean isDispatchCreatePermission() {
            return dispatchCreatePermission;
        }

        public boolean isGlobalAdminPermission() {
            return globalAdminPermission;
        }

        public boolean isOriginInfoUpdatePermission() {
            return originInfoUpdatePermission;
        }

        public boolean isPatientCreatePermission() {
            return patientCreatePermission;
        }

        public boolean isPatientMergePermission() {
            return patientMergePermission;
        }

        public boolean isProcessingEventCreatePermission() {
            return processingEventCreatePermission;
        }

        public boolean isResearchGroupCreatePermission() {
            return researchGroupCreatePermission;
        }

        public boolean isSiteCreatePermission() {
            return siteCreatePermission;
        }

        public boolean isSpecimenAssignPermission() {
            return specimenAssignPermission;
        }

        public boolean isSpecimenLinkPermission() {
            return specimenLinkPermission;
        }

        public boolean isSpecimenTypeCreatePermission() {
            return specimenTypeCreatePermission;
        }

        public boolean isStudyCreatePermission() {
            return studyCreatePermission;
        }

        public boolean isUserManagerPermission() {
            return userManagerPermission;
        }

        public boolean isLabelPrintingPermission() {
            return labelPrintingPermission;
        }

        public boolean isBatchOperationPermission() {
            return batchOperationPermission;
        }

        public static UserCreatePermissions getUserCreatePermissionsLoggedOut() {
            UserCreatePermissions permissions = new UserCreatePermissions();

            permissions.clinicCreatePermission = false;
            permissions.containerCreatePermission = false;
            permissions.containerTypeCreatePermission = false;
            permissions.dispatchCreatePermission = false;
            permissions.globalAdminPermission = false;
            permissions.originInfoUpdatePermission = false;
            permissions.patientCreatePermission = false;
            permissions.patientMergePermission = false;
            permissions.processingEventCreatePermission = false;
            permissions.researchGroupCreatePermission = false;
            permissions.siteCreatePermission = false;
            permissions.specimenAssignPermission = false;
            permissions.specimenLinkPermission = false;
            permissions.specimenTypeCreatePermission = false;
            permissions.studyCreatePermission = false;
            permissions.userManagerPermission = false;
            permissions.labelPrintingPermission = false;

            return permissions;
        }
    }

}
