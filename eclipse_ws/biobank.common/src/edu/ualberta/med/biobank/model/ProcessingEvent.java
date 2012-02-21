package edu.ualberta.med.biobank.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.validator.constraints.NotEmpty;

import edu.ualberta.med.biobank.validator.constraint.NotUsed;
import edu.ualberta.med.biobank.validator.constraint.Unique;
import edu.ualberta.med.biobank.validator.group.PreDelete;
import edu.ualberta.med.biobank.validator.group.PrePersist;

@Entity
@Table(name = "PROCESSING_EVENT")
@NotUsed.List({
    @NotUsed(by = Specimen.class, property = "processingEvent", groups = PreDelete.class),
    @NotUsed(by = Specimen.class, property = "parentSpecimen.processingEvent", groups = PreDelete.class)
})
@Unique(properties = "worksheet", groups = PrePersist.class)
public class ProcessingEvent extends AbstractBiobankModel {
    private static final long serialVersionUID = 1L;

    private String worksheet;
    private Date createdAt;
    private Center center;
    private Set<Specimen> specimenCollection = new HashSet<Specimen>(0);
    private ActivityStatus activityStatus = ActivityStatus.ACTIVE;
    private Set<Comment> commentCollection = new HashSet<Comment>(0);

    @NotEmpty(message = "{edu.ualberta.med.biobank.model.ProcessingEvent.worksheet.NotEmpty}")
    @Column(name = "WORKSHEET", length = 150, unique = true)
    public String getWorksheet() {
        return this.worksheet;
    }

    public void setWorksheet(String worksheet) {
        this.worksheet = worksheet;
    }

    @NotNull(message = "{edu.ualberta.med.biobank.model.ProcessingEvent.createdAt.NotNull}")
    @Column(name = "CREATED_AT", nullable = false)
    public Date getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @NotNull(message = "{edu.ualberta.med.biobank.model.ProcessingEvent.center.NotNull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CENTER_ID", nullable = false)
    public Center getCenter() {
        return this.center;
    }

    public void setCenter(Center center) {
        this.center = center;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "processingEvent")
    @Cascade({ CascadeType.SAVE_UPDATE })
    public Set<Specimen> getSpecimenCollection() {
        return this.specimenCollection;
    }

    public void setSpecimenCollection(Set<Specimen> specimenCollection) {
        this.specimenCollection = specimenCollection;
    }

    @NotNull(message = "{edu.ualberta.med.biobank.model.ProcessingEvent.activityStatus.NotNull}")
    @Column(name = "ACTIVITY_STATUS_ID", nullable = false)
    public ActivityStatus getActivityStatus() {
        return this.activityStatus;
    }

    public void setActivityStatus(ActivityStatus activityStatus) {
        this.activityStatus = activityStatus;
    }

    @ManyToMany(cascade = javax.persistence.CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JoinTable(name = "PROCESSING_EVENT_COMMENT",
        joinColumns = { @JoinColumn(name = "PROCESSING_EVENT_ID", nullable = false, updatable = false) },
        inverseJoinColumns = { @JoinColumn(name = "COMMENT_ID", unique = true, nullable = false, updatable = false) })
    public Set<Comment> getCommentCollection() {
        return this.commentCollection;
    }

    public void setCommentCollection(Set<Comment> commentCollection) {
        this.commentCollection = commentCollection;
    }
}