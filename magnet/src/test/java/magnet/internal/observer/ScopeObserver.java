package magnet.internal.observer;

import magnet.Visitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

public class ScopeObserver implements Visitor {

    @NotNull private final Map<Object, DefaultScopeValidator> validators = new HashMap<>();
    @Nullable private DefaultScopeValidator currentScopeValidator;

    @Override
    public boolean onEnterScope(@NotNull Visitor.Scope scope, @Nullable Visitor.Scope parent) {
        flushScope();
        currentScopeValidator = new DefaultScopeValidator(scope);
        return true;
    }

    @Override
    public boolean onInstance(@NotNull Instance instance) {
        if (currentScopeValidator != null) {
            currentScopeValidator.addInstance(instance);
        }
        return true;
    }

    @Override
    public void onExitScope(@NotNull Visitor.Scope scope) {
        flushScope();
    }

    private void flushScope() {
        if (currentScopeValidator != null) {
            //noinspection Java8ListSort,Convert2Lambda
            Collections.sort(currentScopeValidator.instances, new Comparator<Instance>() {
                @Override public int compare(Instance o1, Instance o2) {
                    return o1.getType().getName().compareTo(o2.getType().getName());
                }
            });
            validators.put(currentScopeValidator.scope, currentScopeValidator);
        }
    }

    public ScopeValidator assetThat(magnet.Scope scopeA) {
        ScopeValidator validator = validators.get(scopeA);
        if (validator == null) {
            throw new IllegalStateException(
                String.format("Scope %s was not observed", scopeA)
            );
        }
        return validator;
    }

    private static class DefaultScopeValidator implements ScopeValidator {
        private final List<Instance> instances = new ArrayList<>();
        private Scope scope;

        DefaultScopeValidator(Scope scope) {
            this.scope = scope;
        }

        void addInstance(Instance instance) {
            instances.add(instance);
        }

        @Override public void hasNoInstances() {
            assertThat(instances.size()).named("number of instances in scope").isEqualTo(0);
        }

        @Override public void hasInstanceTypes(Class<?>... instanceTypes) {
            if (instances.size() != instanceTypes.length) {
                throw new IllegalStateException(
                    String.format(
                        "Expect %s number of instances, while found %s. Actual instances: %s",
                        instanceTypes.length, instances.size(), instances.toString()
                    )
                );
            }

            for (int i = 0; i < instances.size(); i++) {
                if (!instances.get(i).getType().equals(instanceTypes[i])) {
                    throw new IllegalStateException(
                        String.format(
                            "Expect %s at position %s, while found %s. Actual instances: %s",
                            instanceTypes[i], i, instances.get(i), instances.toString()
                        )
                    );
                }
            }
        }
    }
}
