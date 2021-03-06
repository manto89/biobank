package edu.ualberta.med.biobank.common.peer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.ualberta.med.biobank.common.util.TypeReference;
import edu.ualberta.med.biobank.common.wrappers.Property;
import edu.ualberta.med.biobank.model.Membership;
import edu.ualberta.med.biobank.model.Principal;

public class PrincipalPeer {
	public static final Property<Integer, Principal> ID = Property.create(
		"id" //$NON-NLS-1$
		, Principal.class
		, new TypeReference<Integer>() {}
		, new Property.Accessor<Integer, Principal>() { private static final long serialVersionUID = 1L;
			@Override
			public Integer get(Principal model) {
				return model.getId();
			}
			@Override
			public void set(Principal model, Integer value) {
				model.setId(value);
			}
		});

	public static final Property<Collection<Membership>, Principal> MEMBERSHIPS = Property.create(
		"memberships" //$NON-NLS-1$
		, Principal.class
		, new TypeReference<Collection<Membership>>() {}
		, new Property.Accessor<Collection<Membership>, Principal>() { private static final long serialVersionUID = 1L;
			@Override
			public Collection<Membership> get(Principal model) {
				return model.getMemberships();
			}
			@Override
			public void set(Principal model, Collection<Membership> value) {
				model.getMemberships().clear();
				model.getMemberships().addAll(value);
			}
		});

   public static final List<Property<?, ? super Principal>> PROPERTIES;
   static {
      List<Property<?, ? super Principal>> aList = new ArrayList<Property<?, ? super Principal>>();
      aList.add(ID);
      aList.add(MEMBERSHIPS);
      PROPERTIES = Collections.unmodifiableList(aList);
   };
}
