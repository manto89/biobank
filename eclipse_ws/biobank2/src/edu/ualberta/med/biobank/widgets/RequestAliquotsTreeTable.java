package edu.ualberta.med.biobank.widgets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import edu.ualberta.med.biobank.BiobankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.util.RequestSpecimenState;
import edu.ualberta.med.biobank.common.wrappers.RequestSpecimenWrapper;
import edu.ualberta.med.biobank.common.wrappers.RequestWrapper;
import edu.ualberta.med.biobank.forms.utils.RequestTableGroup;
import edu.ualberta.med.biobank.treeview.Node;
import edu.ualberta.med.biobank.treeview.RequestAliquotAdapter;
import edu.ualberta.med.biobank.treeview.admin.RequestContainerAdapter;

public class RequestAliquotsTreeTable extends BiobankWidget {

    private TreeViewer tv;
    private RequestWrapper shipment;
    protected List<RequestTableGroup> groups;

    public RequestAliquotsTreeTable(Composite parent, RequestWrapper shipment) {
        super(parent, SWT.NONE);

        this.shipment = shipment;

        setLayout(new FillLayout());
        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.FILL;
        gd.grabExcessHorizontalSpace = true;
        gd.heightHint = 400;
        setLayoutData(gd);

        tv = new TreeViewer(this, SWT.MULTI | SWT.BORDER);
        Tree tree = tv.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

        TreeColumn tc = new TreeColumn(tree, SWT.LEFT);
        tc.setText("Inventory Id");
        tc.setWidth(200);

        tc = new TreeColumn(tree, SWT.LEFT);
        tc.setText("Type");
        tc.setWidth(100);

        tc = new TreeColumn(tree, SWT.LEFT);
        tc.setText("Location");
        tc.setWidth(120);

        tc = new TreeColumn(tree, SWT.LEFT);
        tc.setText("Claimed By");
        tc.setWidth(100);

        ITreeContentProvider contentProvider = new ITreeContentProvider() {
            @Override
            public void dispose() {
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput,
                Object newInput) {
                groups = RequestTableGroup
                    .getGroupsForShipment(RequestAliquotsTreeTable.this.shipment);
            }

            @Override
            public Object[] getElements(Object inputElement) {
                return groups.toArray();
            }

            @Override
            public Object[] getChildren(Object parentElement) {
                return ((Node) parentElement).getChildren().toArray();
            }

            @Override
            public Object getParent(Object element) {
                return ((Node) element).getParent();
            }

            @Override
            public boolean hasChildren(Object element) {
                return ((Node) element).getChildren().size() != 0;
            }
        };
        tv.setContentProvider(contentProvider);

        final BiobankLabelProvider labelProvider = new BiobankLabelProvider() {
            @Override
            public String getColumnText(Object element, int columnIndex) {
                if (element instanceof RequestTableGroup) {
                    if (columnIndex == 0)
                        return ((RequestTableGroup) element).getTitle();
                    return "";
                } else if (element instanceof RequestContainerAdapter) {
                    if (columnIndex == 0)
                        return ((RequestContainerAdapter) element)
                            .getLabelInternal();
                    return "";
                } else if (element instanceof RequestAliquotAdapter) {
                    switch (columnIndex) {
                    case 0:
                        return ((RequestAliquotAdapter) element)
                            .getLabelInternal();
                    case 1:
                        return ((RequestAliquotAdapter) element)
                            .getSpecimenType();
                    case 2:
                        return ((RequestAliquotAdapter) element).getPosition();
                    case 3:
                        return ((RequestAliquotAdapter) element).getClaimedBy();
                    default:
                        return "";
                    }
                }
                return "";
            }
        };
        tv.setLabelProvider(labelProvider);
        tv.setInput("root");

        tv.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                Object o = ((IStructuredSelection) tv.getSelection())
                    .getFirstElement();
                if (o instanceof RequestAliquotAdapter) {
                    RequestSpecimenWrapper ra = ((RequestAliquotAdapter) o)
                        .getSpecimen();
                    SessionManager.openViewForm(ra.getSpecimen());
                }
            }
        });

        final Menu menu = new Menu(this);
        tv.getTree().setMenu(menu);

        menu.addListener(SWT.Show, new Listener() {
            @Override
            public void handleEvent(Event event) {
                for (MenuItem menuItem : menu.getItems()) {
                    menuItem.dispose();
                }

                RequestSpecimenWrapper ra = getSelectedAliquot();
                if (ra != null) {
                    addClipboardCopySupport(menu, labelProvider);
                    addSetUnavailableMenu(menu);
                    addClaimMenu(menu);
                } else {
                    Object node = getSelectedNode();
                    if (node != null) {
                        addClaimMenu(menu);
                    }
                }
            }
        });
    }

    protected Object getSelectedNode() {
        IStructuredSelection selection = (IStructuredSelection) tv
            .getSelection();
        if (selection != null
            && selection.size() > 0
            && (selection.getFirstElement() instanceof RequestAliquotAdapter || selection
                .getFirstElement() instanceof RequestContainerAdapter))
            return selection.getFirstElement();
        return null;
    }

    protected RequestSpecimenWrapper getSelectedAliquot() {
        Object node = getSelectedNode();
        if (node != null && node instanceof RequestAliquotAdapter) {
            return ((RequestAliquotAdapter) node).getSpecimen();
        }
        return null;
    }

    protected void addClaimMenu(Menu menu) {
        MenuItem item;
        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Claim");
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                claim(getSelectedNode());
                refresh();
            }
        });
    }

    protected void claim(Object node) {
        try {
            if (node instanceof RequestAliquotAdapter) {
                RequestSpecimenWrapper a = ((RequestAliquotAdapter) node)
                    .getSpecimen();
                a.setClaimedBy(SessionManager.getUser().getFirstName());
                a.persist();
            } else {
                List<Object> children = ((RequestContainerAdapter) node)
                    .getChildren();
                for (Object child : children)
                    claim(child);
            }
        } catch (Exception e) {
            BiobankPlugin.openAsyncError("Failed to claim", e);
        }
    }

    private void addSetUnavailableMenu(final Menu menu) {
        MenuItem item;
        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Flag as unavailable");
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                getSelectedAliquot().setState(
                    RequestSpecimenState.UNAVAILABLE_STATE.getId());
                try {
                    getSelectedAliquot().persist();
                } catch (Exception e) {
                    BiobankPlugin.openAsyncError("Save Error", e);
                }
                refresh();
            }
        });
    }

    public void refresh() {
        tv.setInput("refresh");
    }

    private void addClipboardCopySupport(Menu menu,
        final BiobankLabelProvider labelProvider) {
        Assert.isNotNull(menu);
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText("Copy");
        item.addSelectionListener(new SelectionAdapter() {
            @SuppressWarnings("unchecked")
            @Override
            public void widgetSelected(SelectionEvent event) {
                int numCols = tv.getTree().getColumnCount();
                List<Object> selectedRows = new ArrayList<Object>();
                IStructuredSelection sel = (IStructuredSelection) tv
                    .getSelection();
                for (Iterator<Object> iterator = sel.iterator(); iterator
                    .hasNext();) {
                    Object item = iterator.next();
                    String row = "";
                    for (int i = 0; i < numCols; i++) {
                        String text = labelProvider.getColumnText(item, i);
                        if (text != null)
                            row += text;
                        if (i < numCols - 1)
                            row += ", ";
                    }
                    selectedRows.add(row);
                }
                if (selectedRows.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (Object row : selectedRows) {
                        if (sb.length() != 0) {
                            sb.append(System.getProperty("line.separator"));
                        }
                        sb.append(row.toString());
                    }
                    TextTransfer textTransfer = TextTransfer.getInstance();
                    Clipboard cb = new Clipboard(Display.getDefault());
                    cb.setContents(new Object[] { sb.toString() },
                        new Transfer[] { textTransfer });
                }
            }
        });
    }

}