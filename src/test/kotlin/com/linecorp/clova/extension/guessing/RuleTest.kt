/*
 * Copyright (c) 2018 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.linecorp.clova.extension.guessing

import com.linecorp.clova.extension.guessing.rule.GuessingNumberRule
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleTest {

    @Test
    fun testInitNumber() {
        val rule = GuessingNumberRule()
        assertTrue(rule.expectedNumber.length == 4)
        assertTrue(rule.expectedNumber[0] != '0')
    }
}
