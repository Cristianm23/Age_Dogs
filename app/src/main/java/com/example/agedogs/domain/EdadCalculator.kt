package com.example.agedogs.domain

sealed class EdadResult {
    data class Success(val edadPerro: Double, val edadHumana: Double) : EdadResult()
    data class Error(val message: String) : EdadResult()
}

object EdadCalculator {

    /**
     * Parsea la edad ingresada en texto (soportando tanto punto como coma decimal)
     * y calcula la equivalencia en años humanos.
     */
    fun parseAndCalculate(input: String): EdadResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return EdadResult.Error("Por favor ingresa la edad del perro")
        }

        // Permite comas regionales (ej: "2,5" -> "2.5")
        val normalizedInput = trimmed.replace(',', '.')
        val edadDouble = normalizedInput.toDoubleOrNull()

        if (edadDouble == null) {
            return EdadResult.Error("Ingresa un número válido")
        }

        if (edadDouble <= 0) {
            return EdadResult.Error("La edad debe ser mayor a 0")
        }

        val edadHumana = calculate(edadDouble)
        return EdadResult.Success(edadPerro = edadDouble, edadHumana = edadHumana)
    }

    /**
     * Función pura que aplica la fórmula de conversión de edad canina a humana:
     * - Hasta 2 años: edad * 12.5
     * - Más de 2 años: 25 + (edad - 2) * 4
     */
    fun calculate(edadPerro: Double): Double {
        return if (edadPerro <= 2.0) {
            edadPerro * 12.5
        } else {
            25.0 + (edadPerro - 2.0) * 4.0
        }
    }
}
