package edu.ualberta.med.biobank.treeview.order;

import java.util.Collection;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;

import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.OrderWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.treeview.AdapterBase;

public class ApprovedOrderNode extends AdapterBase {

    private SiteWrapper site;

    public ApprovedOrderNode(AdapterBase parent, int id, SiteWrapper site) {
        super(parent, id, "Approved", true, false);
        this.site = site;
    }

    @Override
    protected String getLabelInternal() {
        return null;
    }

    @Override
    public String getTooltipText() {
        return null;
    }

    @Override
    public void popupMenu(TreeViewer tv, Tree tree, Menu menu) {
    }

    @Override
    protected AdapterBase createChildNode() {
        return null;
    }

    @Override
    protected AdapterBase createChildNode(ModelWrapper<?> child) {
        return new OrderAdapter(this, (OrderWrapper) child);
    }

    @Override
    protected Collection<? extends ModelWrapper<?>> getWrapperChildren()
        throws Exception {
        return site.getOrderCollection();
    }

    @Override
    protected int getWrapperChildCount() throws Exception {
        return 0;
    }

    @Override
    public String getViewFormId() {
        return null;
    }

    @Override
    public String getEntryFormId() {
        return null;
    }

    @Override
    public void rebuild() {
        for (AdapterBase adaper : getChildren()) {
            adaper.rebuild();
        }
    }

    @Override
    public AdapterBase search(Object searchedObject) {
        return searchChildren(searchedObject);
    }

}