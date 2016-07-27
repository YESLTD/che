/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.part.editor.multipart;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.google.gwt.dom.client.Style.Display.BLOCK;
import static com.google.gwt.dom.client.Style.Unit.PCT;

/**
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 * @author Vitaliy Guliy
 */
public class EditorMultiPartStackViewImpl extends ResizeComposite implements EditorMultiPartStackView {
    private static final PartStackUiBinder UI_BINDER = GWT.create(PartStackUiBinder.class);
    interface PartStackUiBinder extends UiBinder<Widget, EditorMultiPartStackViewImpl> {
    }

    @UiField
    DockLayoutPanel parent;

    @UiField
    SplitLayoutPanel contentPanel;

    private final Map<PartPresenter, TabItem>         tabs;
    private final Map<PartStack, SplitEditorPartView> splitEditorParts;
    private       AcceptsOneWidget                    partViewContainer;
    private final LinkedList<PartPresenter>           contents;
    private final PartStackUIResources                resources;
    private final SplitEditorPartFactory              splitEditorPartFactory;

    List<IsWidget> widgets = new ArrayList<>();

    private ActionDelegate delegate;
    private TabItem        activeTab;

    @Inject
    public EditorMultiPartStackViewImpl(PartStackUIResources resources, SplitEditorPartFactory splitEditorPartFactory) {
        this.resources = resources;
        this.splitEditorPartFactory = splitEditorPartFactory;
        this.tabs = new HashMap<>();
        this.splitEditorParts = new HashMap<>();
        this.contents = new LinkedList<>();

        initWidget(UI_BINDER.createAndBindUi(this));
    }

    /** {@inheritDoc} */
    @Override
    protected void onAttach() {
        super.onAttach();

        com.google.gwt.dom.client.Style style = getElement().getParentElement().getStyle();
        style.setHeight(100, PCT);
        style.setWidth(100, PCT);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public void addTab(@NotNull TabItem tabItem, @NotNull PartPresenter partPresenter) {
        /** Show editor area if it is empty and hidden */
        if (contents.isEmpty()) {
            getElement().getParentElement().getStyle().setDisplay(BLOCK);
        }

//        /** Add editor tab to tab panel */
//        tabsPanel.add(tabItem.getView());

        /** Process added editor tab */
        tabs.put(partPresenter, tabItem);
        contents.add(partPresenter);
        partPresenter.go(partViewContainer);
    }

    @Override
    public void addPartStack(@NotNull final PartStack partStack, final PartStack specimenPartStack, final Constraints constraints) {
        partViewContainer = new AcceptsOneWidget() {
            @Override
            public void setWidget(IsWidget widget) {
                if (specimenPartStack == null) {
                    SplitEditorPartView splitEditorPartView = splitEditorPartFactory.create(widget);
                    splitEditorParts.put(partStack, splitEditorPartView);
                    contentPanel.add(splitEditorPartView);
                    return;
                }

                SplitEditorPartView specimenView = splitEditorParts.get(specimenPartStack);
                if (specimenView == null) {
                    Log.error(getClass(), "Can not find container for specified editor");
                    return;
                }


                specimenView.split(widget, constraints.direction);
                splitEditorParts.put(partStack, specimenView.getReplica());
                splitEditorParts.put(specimenPartStack, specimenView.getSpecimen());
            }
        };
        partStack.go(partViewContainer);
    }

    @Override
    public void removePartStack(@NotNull PartStack partStack) {
        SplitEditorPartView splitEditorPartView = splitEditorParts.remove(partStack);
        if (splitEditorPartView != null) {
            splitEditorPartView.removeFromParent();
        }
    }


    @Override
    public void removeTab(@NotNull PartPresenter presenter) {}

    /** {@inheritDoc} */
    @Override
    public void selectTab(@NotNull PartPresenter partPresenter) {
//        IsWidget view = partPresenter.getView();
//
//        int viewIndex = contentPanel.getWidgetIndex(view);
//        if (viewIndex < 0) {
//            partPresenter.go(partViewContainer);
//            viewIndex = contentPanel.getWidgetIndex(view);
//        }
//
////        contentPanel.showWidget(viewIndex);
//        setActiveTab(partPresenter);
    }

    /** {@inheritDoc} */
    @Override
    public void setTabPositions(List<PartPresenter> partPositions) {
        throw new UnsupportedOperationException("The method doesn't allowed in this class " + getClass());
    }

    /** {@inheritDoc} */
    @Override
    public void setFocus(boolean focused) {
        if (focused) {
            activeTab.select();
        } else {
            activeTab.unSelect();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void updateTabItem(@NotNull PartPresenter partPresenter) {
        TabItem tab = tabs.get(partPresenter);
        tab.update(partPresenter);
    }

    @Override
    public void onResize() {
        super.onResize();
//        updateDropdownVisibility();
//        ensureActiveTabVisible();
    }

}
