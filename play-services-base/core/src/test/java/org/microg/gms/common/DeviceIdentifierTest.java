/*
 * SPDX-FileCopyrightText: 2026 Bear MicroG contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.gms.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Covers the pure-logic pieces of {@link DeviceIdentifier} (no Android Context involved),
 * mainly the MEID Luhn checksum, which had no test coverage before.
 */
public class DeviceIdentifierTest {

    @Test
    public void meidHasExpectedLengthAndPrefix() {
        DeviceIdentifier identifier = new DeviceIdentifier();
        assertEquals(15, identifier.meid.length());
        assertTrue(identifier.meid.startsWith("35503104"));
    }

    @Test
    public void meidPassesLuhnChecksum() {
        // Regenerate several times since the value is randomized per instance.
        for (int i = 0; i < 100; i++) {
            String meid = new DeviceIdentifier().meid;
            assertTrue("MEID " + meid + " failed Luhn checksum", isValidLuhn(meid));
        }
    }

    @Test
    public void wifiMacHasExpectedFormatAndOuiPrefix() {
        DeviceIdentifier identifier = new DeviceIdentifier();
        assertEquals(12, identifier.wifiMac.length());
        assertTrue(identifier.wifiMac.startsWith("b407f9"));
        assertTrue(identifier.wifiMac.matches("[0-9a-f]{12}"));
    }

    /**
     * Standard Luhn algorithm check, same shape as the one used to generate the MEID's
     * trailing check digit in {@link DeviceIdentifier}.
     */
    private static boolean isValidLuhn(String digits) {
        int sum = 0;
        for (int i = 0; i < digits.length(); i++) {
            int c = Character.getNumericValue(digits.charAt(i));
            if ((digits.length() - i - 1) % 2 == 1) {
                c *= 2;
                if (c > 9) c -= 9;
            }
            sum += c;
        }
        return sum % 10 == 0;
    }
}
