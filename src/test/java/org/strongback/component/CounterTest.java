/*
 * Strongback
 * Copyright 2015, Strongback and individual contributors by the @authors tag.
 * See the COPYRIGHT.txt in the distribution for a full listing of individual
 * contributors.
 *
 * Licensed under the MIT License; you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://opensource.org/licenses/MIT
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.strongback.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.strongback.components.Counter;

/**
 * @author Randall Hauch
 *
 */
public class CounterTest {

    @Test
    public void shouldNotAllowMaximumThatEqualsInitial() {
        assertThrows(IllegalArgumentException.class, () -> Counter.circular(100, 100, 100));
    }

    @Test
    public void shouldCirculateAfterReachingMaximum() {
        Counter counter = Counter.circular(0, 100, 200);
        assertEquals(counter.get(), 0);
        counter.increment();
        assertEquals(counter.get(), 100);
        counter.increment();
        assertEquals(counter.get(), 200);
        counter.increment();
        assertEquals(counter.get(), 0);
    }

    @Test
    public void shouldNotAllowNegativeIncrement() {
        assertThrows(IllegalArgumentException.class, () -> Counter.circular(100, -100, 100));
    }

    @Test
    public void shouldNotAllowNegativeInitialValue() {
        assertThrows(IllegalArgumentException.class, () -> Counter.circular(0, -1, 100));
    }

    @Test
    public void shouldCirculateAfterReachingMaximumWithDefaultIncrement() {
        Counter counter = Counter.circular(2);
        assertEquals(counter.get(), 0);
        counter.increment();
        assertEquals(counter.get(), 1);
        counter.increment();
        assertEquals(counter.get(), 2);
        counter.increment();
        assertEquals(counter.get(), 0);
    }

    @Test
    public void shouldZeroValue() {
        Counter counter = Counter.circular(200);
        assertEquals(counter.get(), 0);
        counter.increment();
        assertEquals(counter.get(), 1);
        counter.zero();
        assertEquals(counter.get(), 0);
        counter.increment();
        assertEquals(counter.get(), 1);
    }
}
