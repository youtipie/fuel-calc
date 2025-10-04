package com.example.kpi_android.prac3

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kpi_android.R
import org.apache.commons.math3.analysis.UnivariateFunction
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator
import java.util.*
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

class Task1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prac3_task1)

        // Отримання користувацького вводу
        val inpPc = findViewById<EditText>(R.id.input_pc)
        val inpQ1 = findViewById<EditText>(R.id.input_q1)
        val inpQ2 = findViewById<EditText>(R.id.input_q2)
        val inpB = findViewById<EditText>(R.id.input_b)
        val btnCalc = findViewById<Button>(R.id.btn_calc)
        val resultsView = findViewById<TextView>(R.id.results)

        // Відображення результату відбувається при натисканні на кнопку
        btnCalc.setOnClickListener {
            try {
                // Парсимо введені значення
                val pc = parseInput(inpPc)
                val q1 = parseInput(inpQ1)
                val q2 = parseInput(inpQ2)
                val b = parseInput(inpB)

                // Якщо q2 більше, то це не має сенсу
                if (q2 >= q1) {
                    Toast.makeText(this, "σ₂ має бути менше за σ₁", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                // Розрахуємо частку енергії, що генерується без небалансу
                val qW1 = integrateGaussian(q1, pc)

                // Розрахуємо прибуток за 20% енергії
                val w1 = pc * 24 * qW1
                val p1 = w1 * b

                // Розрахуємо штраф за 80%
                val w2 = pc * 24 * (1 - qW1)
                val sh1 = w2 * b
                val res1 = p1 - sh1

                // Розрахуємо частку енергії, що генерується без небалансу
                // після вдосконалення системи прогнозу
                val qW2 = integrateGaussian(q2, pc)

                // Розрахуємо прибуток за 68%
                val w3 = pc * 24 * qW2
                val p2 = w3 * b

                // Розрахуємо штраф за 32%
                val w4 = pc * 24 * (1 - qW2)
                val sh2 = w4 * b
                val res2 = p2 - sh2

                // Форматування результатів
                val sb = StringBuilder()
                sb.append("Результати:\n\n")
                sb.append(
                    String.format(
                        Locale.US,
                        "1. Прибуток для σ₁=%.2f МВт. дорівнює П = %.2f тис. грн.\n\n",
                        q1,
                        res1
                    )
                )
                sb.append(
                    String.format(
                        Locale.US,
                        "2. Прибуток для σ₂=%.2f МВт. дорівнює П = %.2f тис. грн.",
                        q2,
                        res2
                    )
                )

                // Виводимо результати
                resultsView.text = sb.toString()
                resultsView.visibility = TextView.VISIBLE
            } catch (e: Exception) {
                Toast.makeText(this, "Помилка: перевірте введені значення", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun parseInput(editText: EditText): Double {
        val raw = editText.text.toString().trim().replace(',', '.')
        if (raw.isEmpty()) throw IllegalArgumentException("Empty input")
        return raw.toDouble()
    }

    // Функція густини нормального розподілу
    fun gaussianPdf(p: Double, q: Double, Pc: Double): Double {
        return (1 / (q * sqrt(2 * Math.PI))) * exp(-((p - Pc).pow(2) / (2 * q.pow(2))))
    }

    // Функція для розрахунку інтегралу
    fun integrateGaussian(q: Double, Pc: Double): Double {
        val integrator = SimpsonIntegrator()
        val func = UnivariateFunction { p ->
            gaussianPdf(p, q, Pc)
        }
        return integrator.integrate(10000, func, Pc - 0.05 * Pc, Pc + 0.05 * Pc)
    }
}