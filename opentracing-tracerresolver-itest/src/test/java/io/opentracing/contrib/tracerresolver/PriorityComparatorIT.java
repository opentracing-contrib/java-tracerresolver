/*
 * Copyright 2017 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.opentracing.contrib.tracerresolver;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * This class tests that the {@link PriorityComparator} doesn't throw any exceptions
 * when the {@literal @}Priority annotation is not available on the classpath.
 *
 * @author Sjoerd Talsma
 */
public class PriorityComparatorIT {

    @Test(expected = ClassNotFoundException.class)
    public void verifyMissingPriorityAnnotation() throws ClassNotFoundException {
        Class.forName("javax.annotation.Priority");
        fail("Priority annotation should not be on the classpath.");
    }

    @Test
    public void testPrioritize() {
        Iterable anyIterable = mock(Iterable.class);
        Iterable prioritized = PriorityComparator.prioritize(anyIterable);
        assertThat(prioritized, is(sameInstance(anyIterable))); // Return iterable as-is
    }

}
