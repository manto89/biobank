package edu.ualberta.med.biobank.common.peer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import edu.ualberta.med.biobank.common.util.TypeReference;
import edu.ualberta.med.biobank.common.wrappers.Property;
import edu.ualberta.med.biobank.model.CollectionEvent;
import edu.ualberta.med.biobank.model.Comment;
import edu.ualberta.med.biobank.model.Patient;
import edu.ualberta.med.biobank.model.Study;

public class PatientPeer {
	public static final Property<Integer, Patient> ID = Property.create(
		"id" //$NON-NLS-1$
		, Patient.class
		, new TypeReference<Integer>() {}
		, new Property.Accessor<Integer, Patient>() { private static final long serialVersionUID = 1L;
			@Override
			public Integer get(Patient model) {
				return model.getId();
			}
			@Override
			public void set(Patient model, Integer value) {
				model.setId(value);
			}
		});

	public static final Property<Date, Patient> CREATED_AT = Property.create(
		"createdAt" //$NON-NLS-1$
		, Patient.class
		, new TypeReference<Date>() {}
		, new Property.Accessor<Date, Patient>() { private static final long serialVersionUID = 1L;
			@Override
			public Date get(Patient model) {
				return model.getCreatedAt();
			}
			@Override
			public void set(Patient model, Date value) {
				model.setCreatedAt(value);
			}
		});

	public static final Property<String, Patient> PNUMBER = Property.create(
		"pnumber" //$NON-NLS-1$
		, Patient.class
		, new TypeReference<String>() {}
		, new Property.Accessor<String, Patient>() { private static final long serialVersionUID = 1L;
			@Override
			public String get(Patient model) {
				return model.getPnumber();
			}
			@Override
			public void set(Patient model, String value) {
				model.setPnumber(value);
			}
		});

	public static final Property<Collection<Comment>, Patient> COMMENTS = Property.create(
		"comments" //$NON-NLS-1$
		, Patient.class
		, new TypeReference<Collection<Comment>>() {}
		, new Property.Accessor<Collection<Comment>, Patient>() { private static final long serialVersionUID = 1L;
			@Override
			public Collection<Comment> get(Patient model) {
				return model.getComments();
			}
			@Override
			public void set(Patient model, Collection<Comment> value) {
				model.getComments().clear();
				model.getComments().addAll(value);
			}
		});

	public static final Property<Collection<CollectionEvent>, Patient> COLLECTION_EVENTS = Property.create(
		"collectionEvents" //$NON-NLS-1$
		, Patient.class
		, new TypeReference<Collection<CollectionEvent>>() {}
		, new Property.Accessor<Collection<CollectionEvent>, Patient>() { private static final long serialVersionUID = 1L;
			@Override
			public Collection<CollectionEvent> get(Patient model) {
				return model.getCollectionEvents();
			}
			@Override
			public void set(Patient model, Collection<CollectionEvent> value) {
				model.getCollectionEvents().clear();
				model.getCollectionEvents().addAll(value);
			}
		});

	public static final Property<Study, Patient> STUDY = Property.create(
		"study" //$NON-NLS-1$
		, Patient.class
		, new TypeReference<Study>() {}
		, new Property.Accessor<Study, Patient>() { private static final long serialVersionUID = 1L;
			@Override
			public Study get(Patient model) {
				return model.getStudy();
			}
			@Override
			public void set(Patient model, Study value) {
				model.setStudy(value);
			}
		});

   public static final List<Property<?, ? super Patient>> PROPERTIES;
   static {
      List<Property<?, ? super Patient>> aList = new ArrayList<Property<?, ? super Patient>>();
      aList.add(ID);
      aList.add(CREATED_AT);
      aList.add(PNUMBER);
      aList.add(COMMENTS);
      aList.add(COLLECTION_EVENTS);
      aList.add(STUDY);
      PROPERTIES = Collections.unmodifiableList(aList);
   };
}
