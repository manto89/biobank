package edu.ualberta.med.biobank.test.action.helper;

import java.util.HashSet;
import java.util.Set;

import edu.ualberta.med.biobank.common.action.info.StudyInfo;
import edu.ualberta.med.biobank.common.action.study.StudyGetClinicInfoAction.ClinicInfo;
import edu.ualberta.med.biobank.common.action.study.StudySaveAction;
import edu.ualberta.med.biobank.common.action.study.StudySaveAction.AliquotedSpecimenSaveInfo;
import edu.ualberta.med.biobank.common.action.study.StudySaveAction.SourceSpecimenSaveInfo;
import edu.ualberta.med.biobank.common.action.study.StudySaveAction.StudyEventAttrSaveInfo;
import edu.ualberta.med.biobank.model.ActivityStatus;
import edu.ualberta.med.biobank.model.AliquotedSpecimen;
import edu.ualberta.med.biobank.model.Contact;
import edu.ualberta.med.biobank.model.SourceSpecimen;
import edu.ualberta.med.biobank.model.StudyEventAttr;
import edu.ualberta.med.biobank.test.action.IActionExecutor;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class StudyHelper extends Helper {
    public static StudySaveAction getSaveAction(String name, String nameShort,
        ActivityStatus activityStatus) {
        StudySaveAction saveAction = new StudySaveAction();
        saveAction.setName(name);
        saveAction.setNameShort(nameShort);
        saveAction.setActivityStatus(activityStatus);
        saveAction.setSiteIds(new HashSet<Integer>());
        saveAction.setContactIds(new HashSet<Integer>());
        saveAction
            .setSourceSpecimenSaveInfo(new HashSet<SourceSpecimenSaveInfo>());
        saveAction
            .setAliquotSpecimenSaveInfo(new HashSet<AliquotedSpecimenSaveInfo>());
        saveAction
            .setStudyEventAttrSaveInfo(new HashSet<StudyEventAttrSaveInfo>());
        return saveAction;
    }

    public static Integer createStudy(IActionExecutor actionExecutor,
        String name, ActivityStatus activityStatus)
        throws ApplicationException {
        StudySaveAction saveStudy = getSaveAction(name, name, activityStatus);
        return actionExecutor.exec(saveStudy).getId();
    }

    public static StudySaveAction getSaveAction(StudyInfo studyInfo) {
        StudySaveAction saveStudy = new StudySaveAction();
        saveStudy.setId(studyInfo.study.getId());
        saveStudy.setName(studyInfo.study.getName());
        saveStudy.setNameShort(studyInfo.study.getNameShort());
        saveStudy.setActivityStatus(studyInfo.study.getActivityStatus());

        saveStudy.setSiteIds(new HashSet<Integer>());

        Set<Integer> ids = new HashSet<Integer>();
        for (ClinicInfo infos : studyInfo.clinicInfos) {
            for (Contact c : infos.getContacts()) {
                ids.add(c.getId());
            }
        }
        saveStudy.setContactIds(ids);

        Set<SourceSpecimenSaveInfo> ssSaveInfos =
            new HashSet<SourceSpecimenSaveInfo>();
        for (SourceSpecimen ss : studyInfo.sourceSpcs) {
            ssSaveInfos.add(new SourceSpecimenSaveInfo(ss));
        }
        saveStudy.setSourceSpecimenSaveInfo(ssSaveInfos);

        Set<AliquotedSpecimenSaveInfo> asSaveInfos =
            new HashSet<AliquotedSpecimenSaveInfo>();
        for (AliquotedSpecimen as : studyInfo.aliquotedSpcs) {
            asSaveInfos.add(new AliquotedSpecimenSaveInfo(as));
        }
        saveStudy.setAliquotSpecimenSaveInfo(asSaveInfos);

        Set<StudyEventAttrSaveInfo> seAttrSaveInfos =
            new HashSet<StudyEventAttrSaveInfo>();
        for (StudyEventAttr seAttr : studyInfo.studyEventAttrs) {
            seAttrSaveInfos.add(new StudyEventAttrSaveInfo(seAttr));
        }
        saveStudy.setStudyEventAttrSaveInfo(seAttrSaveInfos);

        return saveStudy;
    }
}