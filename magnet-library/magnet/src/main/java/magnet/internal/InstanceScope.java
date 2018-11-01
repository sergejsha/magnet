/*
 * Copyright (C) 2018 Sergej Shafarenka, www.halfbit.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package magnet.internal;

import java.util.List;

/* Subject to change. For internal use only. */
public abstract class InstanceScope {

    private boolean parentRequired;
    private InstanceScope parent;
    private MagnetScopeContainer scopeContainer;

    public InstanceScope(boolean parentRequired) {
        this.parentRequired = parentRequired;
    }

    protected void bindParentScope(InstanceScope parent) {
        if (parent == null) {
            throw new IllegalStateException("Parent must not be null");
        }
        if (this.parent != null) {
            throw new IllegalStateException(
                String.format(
                    "Parent cannot be overwritten. Existing parent %s, new parent %s",
                    this.parent, parent
                )
            );
        }
        this.parent = parent;
    }

    protected void bindInstance(Class type, Object object, String classifier) {
        requireScopeContainer().bind(type, object, classifier);
    }

    protected String getSingle(Class<String> type, String classifier) {
        return requireScopeContainer().getSingle(type, classifier);
    }

    protected String getOptional(Class<String> type, String classifier) {
        return requireScopeContainer().getOptional(type, classifier);
    }

    protected List<String> getMany(Class<String> type, String classifier) {
        return requireScopeContainer().getMany(type, classifier);
    }

    private MagnetScopeContainer requireScopeContainer() {
        if (parentRequired && parent == null) {
            throw new IllegalStateException(
                "Parent scope is required. Make sure to bind parent" +
                    " scope before calling any other scope's method.");
        }
        if (scopeContainer == null) {
            scopeContainer = new MagnetScopeContainer(
                /* parent= */ parent == null ? null : parent.scopeContainer,
                /* instanceManager= */ InternalFactory.INSTANCE_MANAGER
            );
        }
        return scopeContainer;
    }

}
