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

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.event.ActivePartChangedHandler;
import org.eclipse.che.ide.api.parts.EditorPartStack;
import org.eclipse.che.ide.api.parts.EditorTab;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStackView.TabItem;
import org.eclipse.che.ide.part.PartStackPresenter;
import org.eclipse.che.ide.part.editor.EditorPartStackFactory;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * EditorPartStackPresenter is a special PartStackPresenter that is shared among all
 * Perspectives and used to display Editors.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class EditorMultiPartStackPresenter implements EditorPartStack, ActivePartChangedHandler {
    private final LinkedList<EditorPartStack> partStackPresenters;

    private final EditorPartStackFactory   editorPartStackFactory;
    private final EditorMultiPartStackView view;

    private PartPresenter activeEditor;

    @Inject
    public EditorMultiPartStackPresenter(EventBus eventBus,
                                         EditorMultiPartStackView view,
                                         EditorPartStackFactory editorPartStackFactory) {

        this.view = view;
        this.editorPartStackFactory = editorPartStackFactory;
        this.partStackPresenters = new LinkedList<>();

        eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
    }


    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    @Override
    public boolean containsPart(PartPresenter part) {
        for (EditorPartStack partStackPresenter : partStackPresenters) {
            if (partStackPresenter.containsPart(part)) {
                return true;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void addPart(@NotNull PartPresenter part, Constraints constraint) {
        if (constraint == null) {
            addPart(part);
            return;
        }

        EditorTab relativeTab;
        EditorPartStack relatedPartStack = null;
        for (EditorPartStack partStackPresenter : partStackPresenters) {
            relativeTab = partStackPresenter.getTabById(constraint.relativeId);
            if (relativeTab != null && relativeTab.getId().equals(constraint.relativeId)) {
                relatedPartStack = partStackPresenter;
                break;
            }
        }

        if (relatedPartStack != null && constraint.direction != null) {
            //split vertically/horizontally in the same editor part stack
            EditorPartStack editorPartStack = editorPartStackFactory.create();
            partStackPresenters.add(editorPartStack);

            view.addPartStack(editorPartStack, relatedPartStack, constraint);
            editorPartStack.addPart(part);
        }

    }

    @Override
    public void setFocus(boolean focused) {
        Log.error(getClass(), "**************** setFocus " + focused);
    }

    /** {@inheritDoc} */
    @Override
    public void addPart(@NotNull PartPresenter part) {
        EditorPartStack editorPartStack = getPartStackByPart(activeEditor);
        if (editorPartStack != null) {
            editorPartStack.addPart(part);
            return;
        }

        editorPartStack = editorPartStackFactory.create();
        partStackPresenters.add(editorPartStack);

        view.addPartStack(editorPartStack, null, null);
        editorPartStack.addPart(part);
    }

    /** {@inheritDoc} */
    @Override
    public PartPresenter getActivePart() {
        return activeEditor;
    }

    @Override
    public TabItem getTabByPart(@NotNull PartPresenter part) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void setActivePart(@NotNull PartPresenter part) {
        activeEditor = part;
        EditorPartStack editorPartStack = getPartStackByPart(part);
        if (editorPartStack != null) {
            editorPartStack.setActivePart(part);
        }
    }

    @Override
    public void hidePart(PartPresenter part) {
        EditorPartStack editorPartStack = getPartStackByPart(part);
        if (editorPartStack != null) {
            editorPartStack.hidePart(part);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removePart(PartPresenter part) {
        for (EditorPartStack partStackPresenter : partStackPresenters) {
            if (partStackPresenter.containsPart(part)) {
                partStackPresenter.removePart(part);

                if (partStackPresenter.getActivePart() == null) {
                    view.removePartStack(partStackPresenter);
                    partStackPresenters.remove(partStackPresenter);

                    if (!partStackPresenters.isEmpty()) {
                        EditorPartStack lastStackPresenter = partStackPresenters.getLast();
                        lastStackPresenter.openPreviousActivePart();
                    }

                }
            }
        }
    }

    @Override
    public void openPreviousActivePart() {

    }

    @Override
    public void updateStack() {
        Log.error(getClass(), "**************** updateStack ");
    }

    @Override
    public EditorTab getTabById(@NotNull String tabId) {
        Log.error(getClass(), "**************** getTabById ");
        return null;
    }

    @Nullable
    private EditorPartStack getPartStackByPart(PartPresenter part) {
        if (part == null) {
            return null;
        }

        for (EditorPartStack editorPartStack : partStackPresenters) {
            if (editorPartStack.containsPart(part)) {
                return editorPartStack;
            }
        }
        return null;
    }

    @Override
    public EditorPartPresenter getPartByTabId(@NotNull String tabId) {
        for(EditorPartStack editorPartStack : partStackPresenters) {
            EditorPartPresenter editorPart = editorPartStack.getPartByTabId(tabId);
            if (editorPart != null) {
                return editorPart;
            }
        }
        return null;
    }

    @Override
    public void onActivePartChanged(ActivePartChangedEvent event) {
        if (event.getActivePart() instanceof EditorPartPresenter) {
            activeEditor = event.getActivePart();
        }
    }
}
