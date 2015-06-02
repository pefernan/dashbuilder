package org.dashbuilder.client.navbar;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import org.uberfire.client.workbench.Header;
import org.uberfire.client.workbench.widgets.menu.WorkbenchMenuBarPresenter;

import static java.lang.Integer.*;

@ApplicationScoped
public class AppNavBar extends Composite implements Header {

    @Inject
    private WorkbenchMenuBarPresenter menuBarPresenter;

    @Override
    public Widget asWidget() {
        return menuBarPresenter.getView().asWidget();
    }

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }

    
}
