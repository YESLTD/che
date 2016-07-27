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

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStackView;

import javax.validation.constraints.NotNull;

/**
 * Provides methods to control view representation of multi part stack container.
 *
 * @author Roman nikitenko
 */
@ImplementedBy(EditorMultiPartStackViewImpl.class)
public interface EditorMultiPartStackView extends PartStackView {

    /**
     * Adds container to main panel which contains all containers.
     *
     * @param partStack
     *         partStack which will be added
     */
    void addPartStack(@NotNull final PartStack partStack, final PartStack relativePartStack, final Constraints constraints);

    void removePartStack(@NotNull final PartStack partStack);
}
