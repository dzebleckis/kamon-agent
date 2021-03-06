/*
 * =========================================================================================
 * Copyright © 2013-2017 the kamon project <http://kamon.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * =========================================================================================
 */

package kamon.agent.api.instrumentation;

import io.vavr.Function0;
import io.vavr.Function1;
import kamon.agent.api.advisor.AdvisorDescription;
import kamon.agent.api.instrumentation.mixin.MixinDescription;
import kamon.agent.util.ListBuilder;
import lombok.val;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.ByteCodeElement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.bytebuddy.matcher.ElementMatchers.*;

public abstract class KamonInstrumentation {
    private final ListBuilder<InstrumentationDescription> instrumentationDescriptions = ListBuilder.builder();

    protected final ElementMatcher.Junction<ByteCodeElement> NotDeclaredByObject = not(isDeclaredBy(Object.class));
    protected final ElementMatcher.Junction<MethodDescription> TakesArguments = not(takesArguments(0));

    private static Function0<ElementMatcher.Junction<TypeDescription>> defaultTypeMatcher = Function0.of(() -> not(isInterface()).and(not(isSynthetic()))).memoized();

    public List<TypeTransformation> collectTransformations() {
        return instrumentationDescriptions.build().map(this::buildTransformations).toJavaList();
    }

    private TypeTransformation buildTransformations(InstrumentationDescription instrumentationDescription) {
        val mixins = collect(instrumentationDescription.mixins(), MixinDescription::makeTransformer);
        val advisors = collect(instrumentationDescription.interceptors(), AdvisorDescription::makeTransformer);
        val transformers = collect(instrumentationDescription.transformers(), Function.identity());
        return TypeTransformation.of(instrumentationDescription.elementMatcher(), mixins, advisors, transformers);
    }

    private <T> Set<AgentBuilder.Transformer> collect(List<T> transformerList, Function<T, AgentBuilder.Transformer> f) {
        return transformerList.stream()
                .map(f)
                .collect(Collectors.toSet());
    }

    public void forTargetType(Supplier<String> f, Function1<InstrumentationDescription.Builder, InstrumentationDescription> instrumentationFunction) {
        val builder = new InstrumentationDescription.Builder();
        builder.addElementMatcher(() -> defaultTypeMatcher.apply().and(named(f.get())));
        instrumentationDescriptions.add(instrumentationFunction.apply(builder));
    }

    public void forSubtypeOf(Supplier<String> f, Function1<InstrumentationDescription.Builder, InstrumentationDescription> instrumentationFunction) {
        val builder = new InstrumentationDescription.Builder();
        builder.addElementMatcher(() -> defaultTypeMatcher.apply().and(hasSuperType(named(f.get()))));
        instrumentationDescriptions.add(instrumentationFunction.apply(builder));
    }

    public void annotatedWith(Supplier<String> f, Function1<InstrumentationDescription.Builder, InstrumentationDescription> instrumentationFunction) {
        val builder = new InstrumentationDescription.Builder();
        builder.addElementMatcher(() -> defaultTypeMatcher.apply().and(isAnnotatedWith(named(f.get()))));
        instrumentationDescriptions.add(instrumentationFunction.apply(builder));
    }

    public boolean isActive() {
        return true;
    }

    public int order() {
        return 1;
    }
}
