/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.client.navbar;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.dashbuilder.client.perspective.editor.PerspectiveEditor;
import org.uberfire.client.workbench.Header;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.uberfire.client.workbench.events.PerspectiveChange;

@ApplicationScoped
public class ComplementNavAreaPresenter implements Header {

    public interface View extends IsWidget {
        void hide();
        void show(boolean showLogo);
    }

    @Inject
    public View view;

    @Inject
    public PerspectiveEditor perspectiveEditor;

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }

    public void hide() {
        view.hide();
    }

    public void show(boolean showLogo) {
        view.show(showLogo);
    }

    /**
     * Hide the perspective context toolbar for editable perspectives
     */
    protected void onPerspectiveChanged(@Observes final PerspectiveChange event) {
        if (perspectiveEditor.isEditable()) {
            view.hide();
        }
    }
}