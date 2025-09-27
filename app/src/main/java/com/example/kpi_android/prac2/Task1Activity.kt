package com.example.kpi_android.prac2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kpi_android.R
import java.util.Locale
import kotlin.math.pow

class Task1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prac2_task1)

        // Отримання користувацького вводу
        val inpCoal = findViewById<EditText>(R.id.input_coal)
        val inpOil = findViewById<EditText>(R.id.input_oil)
        val inpGas = findViewById<EditText>(R.id.input_gas)

        // Константи
        val inpAp = findViewById<EditText>(R.id.input_ap)
        val inpQpi = findViewById<EditText>(R.id.input_qpi)
        val inpQgiOil = findViewById<EditText>(R.id.input_qgi_oil)
        val inpWpOil = findViewById<EditText>(R.id.input_wp_oil)
        val inpGvun = findViewById<EditText>(R.id.input_gvun)
        val inpNzu = findViewById<EditText>(R.id.input_nzu)

        val btnCalc = findViewById<Button>(R.id.btn_calc2)
        val resultsView = findViewById<TextView>(R.id.results2)

        // Встановлюємо значення за замовчуванням
        inpAp.setText("25.20")
        inpQpi.setText("20.47")
        inpQgiOil.setText("40.40")
        inpWpOil.setText("2")
        inpGvun.setText("1.5")
        inpNzu.setText("0.985")

        btnCalc.setOnClickListener {
            try {
                val coal = parseInput(inpCoal)
                val oil = parseInput(inpOil)
                val gas = parseInput(inpGas)

                // Константи
                val apCoal = parseInput(inpAp)
                val qpiCoal = parseInput(inpQpi)
                val qgiOil = parseInput(inpQgiOil)
                val wpOil = parseInput(inpWpOil)
                val gvun = parseInput(inpGvun)
                val nzu = parseInput(inpNzu)

                // Значення частки леткої золи для вугілля та мазуту, взяті з таблиці 2.1
                val avunCoal = 0.8
                val avunOil = 1.0

                // Шукаємо нижчу теплоту згоряння робочої маси для мазуту
                val qriOil = qgiOil * (100 - wpOil - 0.15) / 100 - 0.025 * wpOil

                // Обчислюємо показник емісії твердих частинок при спалюванні вугілля
                // та валовий викид при спалюванні вугілля
                val ktvCoal = 10.0.pow(6) / qpiCoal * avunCoal * apCoal / (100 - gvun) * (1 - nzu)
                val etvCoal = 10.0.pow(-6) * ktvCoal * qpiCoal * coal

                // Обчислюємо показник емісії твердих частинок при спалюванні мазуту
                // та валовий викид при спалюванні мазуту
                val ktvOil = 10.0.pow(6) / qriOil * avunOil * 0.15 / 100 * (1 - nzu)
                val etvOil = 10.0.pow(-6) * ktvOil * qriOil * oil

                // Оскільки при спалюванні природного газу тверді частинки відсутні,
                // то показник емісії та валовий викид = 0
                val ktvGas = 0.0
                val etvGas = 0.0

                // Форматування результатів
                val sb = StringBuilder()
                sb.append(String.format(Locale.US, "1.1. Показник емісії твердих частинок при спалюванні вугілля становитиме: %.2f г/ГДж\n", ktvCoal))
                sb.append(String.format(Locale.US, "1.2. Валовий викид при спалюванні вугілля становитиме: %.2f т.\n", etvCoal))
                sb.append(String.format(Locale.US, "1.3. Показник емісії твердих частинок при спалюванні мазуту становитиме: %.2f г/ГДж\n", ktvOil))
                sb.append(String.format(Locale.US, "1.4. Валовий викид при спалюванні мазуту становитиме: %.2f т.\n", etvOil))
                sb.append(String.format(Locale.US, "1.5. Показник емісії твердих частинок при спалюванні природного газу становитиме: %.2f г/ГДж\n", ktvGas))
                sb.append(String.format(Locale.US, "1.6. Валовий викид при спалюванні природного газу становитиме: %.2f т.", etvGas))

                // Виводимо результати
                resultsView.text = sb.toString()
                resultsView.visibility = TextView.VISIBLE
            } catch (e: Exception) {
                // Показуємо повідомлення про помилку у випадку некоректного введення
                Toast.makeText(this, "Помилка: перевірте введені значення", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Функція для перетворення введеного тексту на Double
    private fun parseInput(editText: EditText): Double {
        val raw = editText.text.toString().trim().replace(',', '.')
        if (raw.isEmpty()) throw IllegalArgumentException("Empty input")
        return raw.toDouble()
    }
}