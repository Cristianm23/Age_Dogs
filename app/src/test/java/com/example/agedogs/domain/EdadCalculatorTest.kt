package com.example.agedogs.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EdadCalculatorTest {

    @Test
    fun testHalfYearDogAge() {
        val result = EdadCalculator.parseAndCalculate("0.5")
        assertTrue(result is EdadResult.Success)
        val success = result as EdadResult.Success
        assertEquals(6.25, success.edadHumana, 0.001)
    }

    @Test
    fun testOneYearDogAge() {
        val result = EdadCalculator.parseAndCalculate("1")
        assertTrue(result is EdadResult.Success)
        val success = result as EdadResult.Success
        assertEquals(12.5, success.edadHumana, 0.001)
    }

    @Test
    fun testTwoYearsDogAge() {
        val result = EdadCalculator.parseAndCalculate("2")
        assertTrue(result is EdadResult.Success)
        val success = result as EdadResult.Success
        assertEquals(25.0, success.edadHumana, 0.001)
    }

    @Test
    fun testThreeYearsDogAge() {
        val result = EdadCalculator.parseAndCalculate("3")
        assertTrue(result is EdadResult.Success)
        val success = result as EdadResult.Success
        assertEquals(29.0, success.edadHumana, 0.001)
    }

    @Test
    fun testTenYearsDogAge() {
        val result = EdadCalculator.parseAndCalculate("10")
        assertTrue(result is EdadResult.Success)
        val success = result as EdadResult.Success
        assertEquals(57.0, success.edadHumana, 0.001)
    }

    @Test
    fun testDecimalWithCommaParsing() {
        val result = EdadCalculator.parseAndCalculate("2,5")
        assertTrue(result is EdadResult.Success)
        val success = result as EdadResult.Success
        assertEquals(27.0, success.edadHumana, 0.001)
    }

    @Test
    fun testZeroAgeReturnsError() {
        val result = EdadCalculator.parseAndCalculate("0")
        assertTrue(result is EdadResult.Error)
        val error = result as EdadResult.Error
        assertEquals("La edad debe ser mayor a 0", error.message)
    }

    @Test
    fun testNegativeAgeReturnsError() {
        val result = EdadCalculator.parseAndCalculate("-2")
        assertTrue(result is EdadResult.Error)
        val error = result as EdadResult.Error
        assertEquals("La edad debe ser mayor a 0", error.message)
    }

    @Test
    fun testInvalidTextReturnsError() {
        val result = EdadCalculator.parseAndCalculate("abc")
        assertTrue(result is EdadResult.Error)
        val error = result as EdadResult.Error
        assertEquals("Ingresa un número válido", error.message)
    }
}
