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

package org.strongback.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.strongback.function.DoubleToDoubleFunction;

public class ValuesTest {

    @Test
    public void shouldMapRangeFromZeroCenteredToPositiveOne() {
        DoubleToDoubleFunction func = Values.mapRange(-1.0, 1.0, 0.0, 1.0);

        // Verify minimum range is properly limited ...
        assertEquals(func.applyAsDouble(-1.0), 0.0, 0.00001);
        assertEquals(func.applyAsDouble(-1.01), 0.0, 0.00001);
        assertEquals(func.applyAsDouble(-2.0), 0.0, 0.00001);

        // Verify maximum range is properly limited ...
        assertEquals(func.applyAsDouble(1.0), 1.0, 0.00001);
        assertEquals(func.applyAsDouble(1.01), 1.0, 0.00001);
        assertEquals(func.applyAsDouble(2.0), 1.0, 0.00001);

        // Verify mid-range
        assertEquals(func.applyAsDouble(0.0), 0.5, 0.00001);

        // Verify other values within the range ...
        assertEquals(func.applyAsDouble(-0.75), 0.125, 0.00001);
        assertEquals(func.applyAsDouble(-0.5), 0.25, 0.00001);
        assertEquals(func.applyAsDouble(-0.25), 0.375, 0.00001);
        assertEquals(func.applyAsDouble(0.25), 0.625, 0.00001);
        assertEquals(func.applyAsDouble(0.5), 0.75, 0.00001);
        assertEquals(func.applyAsDouble(0.75), 0.875, 0.00001);
    }

    @Test
    public void shouldMapRangeUsingTranslationOnly() {
        DoubleToDoubleFunction func = Values.mapRange(0.0, 4.0, 10.0, 14.0);

        // Verify minimum range is properly limited ...
        assertEquals(func.applyAsDouble(0.0), 10.0, 0.00001);
        assertEquals(func.applyAsDouble(-0.01), 10.0, 0.00001);
        assertEquals(func.applyAsDouble(-2.0), 10.0, 0.00001);

        // Verify maximum range is properly limited ...
        assertEquals(func.applyAsDouble(4.0), 14.0, 0.00001);
        assertEquals(func.applyAsDouble(4.01), 14.0, 0.00001);
        assertEquals(func.applyAsDouble(6.0), 14.0, 0.00001);

        // Verify mid-range
        assertEquals(func.applyAsDouble(2.0), 12.0, 0.00001);

        // Verify other values within the range ...
        assertEquals(func.applyAsDouble(0.5), 10.5, 0.00001);
        assertEquals(func.applyAsDouble(1.0), 11.0, 0.00001);
        assertEquals(func.applyAsDouble(1.5), 11.5, 0.00001);
        assertEquals(func.applyAsDouble(2.0), 12.0, 0.00001);
        assertEquals(func.applyAsDouble(2.5), 12.5, 0.00001);
        assertEquals(func.applyAsDouble(3.0), 13.0, 0.00001);
        assertEquals(func.applyAsDouble(3.5), 13.5, 0.00001);
        assertEquals(func.applyAsDouble(4.0), 14.0, 0.00001);
    }

    @Test
    public void shouldMapRangeUsingScaleOnly() {
        DoubleToDoubleFunction func = Values.mapRange(1.0, 5.0).toRange(1.0, 2.0);

        // Verify minimum range is properly limited ...
        assertEquals(func.applyAsDouble(0.0), 1.0, 0.00001);
        assertEquals(func.applyAsDouble(-0.01), 1.0, 0.00001);
        assertEquals(func.applyAsDouble(-2.0), 1.0, 0.00001);

        // Verify maximum range is properly limited ...
        assertEquals(func.applyAsDouble(5.0), 2.0, 0.00001);
        assertEquals(func.applyAsDouble(5.01), 2.0, 0.00001);
        assertEquals(func.applyAsDouble(5.0), 2.0, 0.00001);

        // Verify mid-range
        assertEquals(func.applyAsDouble(3.0), 1.5, 0.00001);

        // Verify other values within the range ...
        assertEquals(func.applyAsDouble(1.04), 1.01, 0.00001);
        assertEquals(func.applyAsDouble(1.8), 1.2, 0.00001);
        assertEquals(func.applyAsDouble(2.0), 1.25, 0.00001);
        assertEquals(func.applyAsDouble(2.6), 1.4, 0.00001);
        assertEquals(func.applyAsDouble(3.4), 1.6, 0.00001);
        assertEquals(func.applyAsDouble(3.8), 1.7, 0.00001);
        assertEquals(func.applyAsDouble(4.2), 1.8, 0.00001);
        assertEquals(func.applyAsDouble(4.6), 1.9, 0.00001);
    }

}
