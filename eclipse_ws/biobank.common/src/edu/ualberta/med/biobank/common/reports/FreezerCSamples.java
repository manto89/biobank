package edu.ualberta.med.biobank.common.reports;

import java.util.List;

import edu.ualberta.med.biobank.model.Aliquot;
import edu.ualberta.med.biobank.model.ContainerPath;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

public class FreezerCSamples extends QueryObject {

    protected static final String NAME = "Freezer Aliquots per Study per Clinic";

    public FreezerCSamples(String op, Integer siteId) {
        super(
            "Displays the total number of freezer aliquots per study per clinic.",
            "select aliquot.patientVisit.patient.study.nameShort, "
                + "aliquot.patientVisit.shipment.clinic.name, count(*) from "
                + Aliquot.class.getName()
                + " as aliquot where aliquot.aliquotPosition.container.id "
                + "in (select path1.container.id from "
                + ContainerPath.class.getName()
                + " as path1, "
                + ContainerPath.class.getName()
                + " as path2 where locate(path2.path, path1.path) > 0 and path2.container.containerType.name like ?) and aliquot.patientVisit.patient.study.site"
                + op
                + siteId
                + " group by aliquot.patientVisit.patient.study.nameShort, aliquot.patientVisit.shipment.clinic.name",
            new String[] { "Study", "Clinic", "Total" });
    }

    @Override
    public List<Object> executeQuery(WritableApplicationService appService,
        List<Object> params) throws ApplicationException {
        params.add("%Freezer%");
        HQLCriteria c = new HQLCriteria(queryString);
        c.setParameters(params);
        return appService.query(c);
    }

    @Override
    public String getName() {
        return NAME;
    }
}