package com.example

import com.example.auth.PhoneAuthHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneAuthHelperTest {
    @Test
    fun `normalizes Bangladeshi local and international mobile numbers to E164`() {
        assertEquals("+8801712345678", PhoneAuthHelper.normalizeToE164("01712345678"))
        assertEquals("+8801812345678", PhoneAuthHelper.normalizeToE164("০১৮১২৩৪৫৬৭৮"))
        assertEquals("+8801912345678", PhoneAuthHelper.normalizeToE164("+880 1912-345678"))
        assertEquals("+8801612345678", PhoneAuthHelper.normalizeToE164("8801612345678"))
    }

    @Test
    fun `rejects invalid Bangladeshi mobile numbers`() {
        assertNull(PhoneAuthHelper.normalizeToE164("01212345678"))
        assertNull(PhoneAuthHelper.normalizeToE164("0171234"))
        assertFalse(PhoneAuthHelper.isValidPhoneForProfile("01712"))
        assertTrue(PhoneAuthHelper.isValidPhoneForProfile("01712345678"))
    }
}
