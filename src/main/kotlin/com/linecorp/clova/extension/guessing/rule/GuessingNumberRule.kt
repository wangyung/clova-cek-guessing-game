/*
 * Copyright (c) 2018 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.linecorp.clova.extension.guessing.rule

class GuessingNumberRule {
    private var numbers = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
    val expectedNumber: String

    init {
       expectedNumber = generateRandomNumber()
    }
    private fun generateRandomNumber(): String {
        numbers = numbers.shuffled()
        //prevent 0 from being first number
        while (numbers[0] == 0) {
            numbers = numbers.shuffled()
        }
        return numbers.take(4).joinToString(separator = "") { it.toString() }
    }

    fun compareNumber(input: String, expect: String): Pair<Boolean, String> {
        if (input.length != 4) {
            return Pair(false, "もう一度話してください")
        }


        var a = 0 //Xa means the number and position is the same
        var b = 0 //Xb means the number is the same but position is different
        if (input == expect) {
            return Pair(true, "正答です")
        }

        for (i in 0..3) {
            if (input[i] == expect[i]) {
                a++
            } else if (input[i] in expect) {
                b++
            }
        }

        return if (a == 0 && b == 0) {
            Pair(false, "なんでもない")
        } else {
            Pair(false, "数字${separateNumbers(input)}は $a A $b Bです")
        }
    }

    private fun separateNumbers(number: String): String =
        number.toList().joinToString(separator = ",")

}
