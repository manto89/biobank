package edu.ualberta.med.biobank;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

import edu.ualberta.med.biobank.common.action.Dispatcher;
import edu.ualberta.med.biobank.mvp.presenter.impl.AddressEntryPresenter;
import edu.ualberta.med.biobank.mvp.presenter.impl.SiteEntryPresenter;
import edu.ualberta.med.biobank.mvp.view.AddressEntryView;
import edu.ualberta.med.biobank.mvp.view.SiteEntryView;

public class GuiceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(EventBus.class).to(SimpleEventBus.class).in(Singleton.class);
        bind(Dispatcher.class).to(BiobankDispatcher.class).in(Singleton.class);

        bind(SiteEntryPresenter.View.class).to(SiteEntryView.class);
        bind(AddressEntryPresenter.View.class).to(AddressEntryView.class);
        // bind(ActivityStatusComboPresenter.View.class).to(ActivityStatusComboView.class);
        // bind(FormManagerPresenter.View.class).to(SiteEntryView.class);
    }
}
