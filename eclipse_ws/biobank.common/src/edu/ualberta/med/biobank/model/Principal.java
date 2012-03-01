package edu.ualberta.med.biobank.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;

import edu.ualberta.med.biobank.validator.group.PreInsert;

@Entity
@Table(name = "PRINCIPAL")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DISCRIMINATOR",
    discriminatorType = DiscriminatorType.STRING)
public class Principal extends AbstractBiobankModel {
    private static final long serialVersionUID = 1L;

    private Set<Membership> membershipCollection =
        new HashSet<Membership>(0);
    private ActivityStatus activityStatus = ActivityStatus.ACTIVE;

    // Require at least one membership on creation so there is some loose
    // association between the creator and the created user.
    @NotEmpty(groups = PreInsert.class, message = "{edu.ualberta.med.biobank.model.Principal.membershipCollection.NotEmpty}")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "principal")
    public Set<Membership> getMembershipCollection() {
        return this.membershipCollection;
    }

    public void setMembershipCollection(
        Set<Membership> membershipCollection) {
        this.membershipCollection = membershipCollection;
    }

    @NotNull(message = "{edu.ualberta.med.biobank.model.Principal.activityStatus.NotNull}")
    @Column(name = "ACTIVITY_STATUS_ID", nullable = false)
    @Type(type = "activityStatus")
    public ActivityStatus getActivityStatus() {
        return this.activityStatus;
    }

    public void setActivityStatus(ActivityStatus activityStatus) {
        this.activityStatus = activityStatus;
    }

    /**
     * Return true if this {@link Principal} can be removed by the given
     * {@link User}, i.e. if the given {@link User} is of <em>equal</em> or
     * greater power.
     * 
     * @param user potential (co?)-manager
     * @return true if this is subordinate to the given {@link User} user.
     */
    @Transient
    public boolean isRemovable(User user) {
        for (Membership membership : getMembershipCollection()) {
            if (!membership.isRemovable(user)) return false;
        }
        return true;
    }
}
