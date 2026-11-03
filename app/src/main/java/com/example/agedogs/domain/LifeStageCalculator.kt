package com.example.agedogs.domain

enum class TamanioPerro {
    PEQUENO, MEDIANO, GRANDE, GIGANTE
}

enum class EtapaVida(val titulo: String, val descripcion: String) {
    CACHORRO("Cachorro", "Crecimiento rápido, desarrollo óseo y socialización temprana."),
    JOVEN("Joven", "Energía elevada, consolidación de adiestramiento y madurez física."),
    ADULTO("Adulto", "Mantenimiento físico, control de peso y actividad regular."),
    SENIOR("Senior", "Cuidados articulares preventivos, revisiones geriátricas y menor intensidad de ejercicio.")
}

data class RecomendacionEtapa(
    val etapa: EtapaVida,
    val alimentacion: String,
    val salud: String,
    val ejercicio: String,
    val chequeos: String
)

object LifeStageCalculator {

    fun determinarTamanio(pesoKg: Double): TamanioPerro {
        return when {
            pesoKg < 10.0 -> TamanioPerro.PEQUENO
            pesoKg <= 25.0 -> TamanioPerro.MEDIANO
            pesoKg <= 45.0 -> TamanioPerro.GRANDE
            else -> TamanioPerro.GIGANTE
        }
    }

    fun determinarEtapa(edadAnios: Double, pesoKg: Double): EtapaVida {
        val tamanio = determinarTamanio(pesoKg)
        return when (tamanio) {
            TamanioPerro.PEQUENO -> when {
                edadAnios <= 1.0 -> EtapaVida.CACHORRO
                edadAnios <= 3.0 -> EtapaVida.JOVEN
                edadAnios <= 9.0 -> EtapaVida.ADULTO
                else -> EtapaVida.SENIOR
            }
            TamanioPerro.MEDIANO -> when {
                edadAnios <= 1.0 -> EtapaVida.CACHORRO
                edadAnios <= 3.0 -> EtapaVida.JOVEN
                edadAnios <= 8.0 -> EtapaVida.ADULTO
                else -> EtapaVida.SENIOR
            }
            TamanioPerro.GRANDE -> when {
                edadAnios <= 1.5 -> EtapaVida.CACHORRO
                edadAnios <= 4.0 -> EtapaVida.JOVEN
                edadAnios <= 7.0 -> EtapaVida.ADULTO
                else -> EtapaVida.SENIOR
            }
            TamanioPerro.GIGANTE -> when {
                edadAnios <= 1.5 -> EtapaVida.CACHORRO
                edadAnios <= 4.0 -> EtapaVida.JOVEN
                edadAnios <= 6.0 -> EtapaVida.ADULTO
                else -> EtapaVida.SENIOR
            }
        }
    }

    fun obtenerRecomendaciones(etapa: EtapaVida): RecomendacionEtapa {
        return when (etapa) {
            EtapaVida.CACHORRO -> RecomendacionEtapa(
                etapa = etapa,
                alimentacion = "Alimento balanceado especializado para cachorros (alta densidad energética y proteica).",
                salud = "Plan primario de vacunación y desparasitación estricta según criterio veterinario.",
                ejercicio = "Juegos moderados y socialización. Evitar ejercicio de alto impacto en articulaciones en desarrollo.",
                chequeos = "Visitas veterinarias mensuales durante los primeros meses."
            )
            EtapaVida.JOVEN -> RecomendacionEtapa(
                etapa = etapa,
                alimentacion = "Transición gradual a dieta de mantenimiento para adulto joven.",
                salud = "Refuerzos anuales de vacunación y control antiparasitario periódico.",
                ejercicio = "Alta actividad física, paseos largos, estimulación mental y entrenamiento en obediencia.",
                chequeos = "Chequeo médico veterinario semestral o anual."
            )
            EtapaVida.ADULTO -> RecomendacionEtapa(
                etapa = etapa,
                alimentacion = "Control de porciones para evitar sobrepeso. Alimento de mantenimiento de alta calidad.",
                salud = "Mantenimiento preventivo, limpieza dental y control de parásitos externos.",
                ejercicio = "Rutina constante de paseos y juegos adaptada a la raza y temperamento.",
                chequeos = "Examen clínico anual completo con analítica preventiva opcional."
            )
            EtapaVida.SENIOR -> RecomendacionEtapa(
                etapa = etapa,
                alimentacion = "Dietas senior con protectores articulares (condroprotectores) y menor aporte calórico si hay baja actividad.",
                salud = "Monitoreo de movilidad, función renal, cardíaca y detección temprana de dolencias geriátricas.",
                ejercicio = "Paseos más cortos pero frecuentes, evitando superficies resbaladizas o saltos.",
                chequeos = "Chequeos geriátricos semestrales obligatorios."
            )
        }
    }
}
