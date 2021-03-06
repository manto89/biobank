package edu.ualberta.med.biobank.common.action.specimen;

import java.util.ArrayList;
import java.util.List;

import edu.ualberta.med.biobank.CommonBundle;
import edu.ualberta.med.biobank.common.action.Action;
import edu.ualberta.med.biobank.common.action.ActionContext;
import edu.ualberta.med.biobank.common.action.ActionResult;
import edu.ualberta.med.biobank.common.action.exception.ActionException;
import edu.ualberta.med.biobank.common.action.specimen.SpecimenAssignSaveAction.SpecimenAssignResInfo;
import edu.ualberta.med.biobank.common.action.specimen.SpecimenMicroplateConsistentAction.SpecimenMicroplateInfo;
import edu.ualberta.med.biobank.common.permission.specimen.SpecimenAssignPermission;
import edu.ualberta.med.biobank.i18n.Bundle;
import edu.ualberta.med.biobank.i18n.LocalizedException;
import edu.ualberta.med.biobank.model.Container;
import edu.ualberta.med.biobank.model.Specimen;
import edu.ualberta.med.biobank.model.util.RowColPos;

public class SpecimenAssignSaveAction implements Action<SpecimenAssignResInfo> {
    private static final long serialVersionUID = 1L;
    private static final Bundle bundle = new CommonBundle();

    public static class SpecimenInfo implements ActionResult {
        private static final long serialVersionUID = 1L;
        public Integer specimenId;
        public RowColPos position;
    }

    public static class SpecimenAssignResInfo implements ActionResult {
        private static final long serialVersionUID = 1L;

        public Integer parentContainerId;
        public String parentBarcode;
        public String parentTypeName;
        public String parentLabel;
        public String siteName;

        public List<SpecimenResInfo> specimens;
    }

    public static class SpecimenResInfo implements ActionResult {
        private static final long serialVersionUID = 1L;
        public Integer specimenId;
        public String position;
        public String inventoryId;
        public String typeName;
        public String patientPNumber;
        public String visitNumber;
        public String centerName;
    }

    private final List<SpecimenInfo> specInfos;
    private final Integer containerId;

    public SpecimenAssignSaveAction(Integer containerId,
        List<SpecimenInfo> specInfos) {
        this.specInfos = specInfos;
        this.containerId = containerId;
    }

    @Override
    public boolean isAllowed(ActionContext context) {
        Container container = context.load(Container.class, containerId);
        Integer centerId = container.getSite().getId();
        return new SpecimenAssignPermission(centerId).isAllowed(context);
    }

    @SuppressWarnings("nls")
    @Override
    public SpecimenAssignResInfo run(ActionContext context)
        throws ActionException {
        SpecimenAssignResInfo res = new SpecimenAssignResInfo();
        if (containerId == null && containerId == null)
            throw new LocalizedException(
                bundle.tr("Problem with sent data. Container id is null")
                    .format());
        res.parentContainerId = containerId;

        Container container = context.load(Container.class, containerId);

        List<SpecimenMicroplateInfo> specimenMicroplateInfos = new ArrayList<SpecimenMicroplateInfo>();
        for (SpecimenInfo si : specInfos) {
            SpecimenMicroplateInfo smi = new SpecimenMicroplateInfo();
            smi.inventoryId = context.load(Specimen.class, si.specimenId).getInventoryId();
            smi.containerId = containerId;
            smi.position = si.position;
            specimenMicroplateInfos.add(smi);
        }
        new SpecimenMicroplateConsistentAction(
                container.getSite().getId(), false, specimenMicroplateInfos).run(context);

        res.parentBarcode = container.getProductBarcode();
        res.parentTypeName = container.getContainerType().getName();
        res.parentLabel = container.getLabel();
        res.siteName = container.getSite().getNameShort();

        res.specimens =
            new ArrayList<SpecimenAssignSaveAction.SpecimenResInfo>();
        for (SpecimenInfo si : specInfos) {
            Specimen specimen =
                context.load(Specimen.class, si.specimenId);
            SpecimenActionHelper.setPosition(context, specimen,
                si.position, containerId);
            context.getSession().saveOrUpdate(specimen);

            SpecimenResInfo rInfo = new SpecimenResInfo();
            rInfo.specimenId = specimen.getId();
            rInfo.position = SpecimenActionHelper.getPositionString(specimen,
                true, false);
            rInfo.inventoryId = specimen.getInventoryId();
            rInfo.typeName = specimen.getSpecimenType().getName();
            rInfo.patientPNumber = specimen.getCollectionEvent().getPatient()
                .getPnumber();
            rInfo.visitNumber = specimen.getCollectionEvent().getVisitNumber()
                .toString();
            rInfo.centerName = specimen.getCurrentCenter().getNameShort();
            res.specimens.add(rInfo);
        }

        return res;
    }
}
