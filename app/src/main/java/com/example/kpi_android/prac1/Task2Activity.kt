package com.example.kpi_android.prac1

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kpi_android.R
import java.util.Locale

class Task2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prac1_task2)

        //  Отримання користувацього вводу
        val inpHg = findViewById<EditText>(R.id.input_hg)
        val inpCg = findViewById<EditText>(R.id.input_cg)
        val inpSg = findViewById<EditText>(R.id.input_sg)
        val inpOg = findViewById<EditText>(R.id.input_og)
        val inpVg = findViewById<EditText>(R.id.input_vg)
        val inpWg = findViewById<EditText>(R.id.input_wg)
        val inpAg = findViewById<EditText>(R.id.input_ag)
        val inpQi = findViewById<EditText>(R.id.input_qi)
        val btnCalc = findViewById<Button>(R.id.btn_calc2)
        val resultsView = findViewById<TextView>(R.id.results2)

        // Відображення результату відбувається при натисканні на кнопку,
        // отже потрібен обробник натискань
        btnCalc.setOnClickListener {
            try {
                // Парсимо введені значення
                val hg = parseInput(inpHg)
                val cg = parseInput(inpCg)
                val sg = parseInput(inpSg)
                val og = parseInput(inpOg)
                val vg = parseInput(inpVg)
                val wg = parseInput(inpWg)
                val ag = parseInput(inpAg)
                val qi = parseInput(inpQi)

                // Обчислюємо склад робочої маси мазуту
                val hp = hg * (100.0 - wg - ag) / 100.0
                val cp = cg * (100.0 - wg - ag) / 100.0
                val sp = sg * (100.0 - wg - ag) / 100.0
                val op = og * (100.0 - wg - ag) / 100.0
                val ap = ag * (100.0 - wg) / 100.0
                val vp = vg * (100.0 - wg) / 100.0

                // Обчислюємо нижчу теплоту згоряння мазуту на робочу масу
                val qri = qi * (100.0 - wg - ag) / 100.0 - 0.025 * wg

                // Форматування результатів
                val sb = StringBuilder()
                sb.append(String.format(Locale.US, "2.1. Склад робочої маси мазуту становитиме: Hᵖ=%.2f%%, Cᵖ=%.2f%%, Sᵖ=%.2f%%, Oᵖ=%.2f%%, Vᵖ=%.2f мг/кг, Aᵖ=%.2f%%\n", hp, cp, sp, op, vp, ap))
                sb.append(String.format(Locale.US, "2.2. Нижча теплота згоряння мазуту на робочу масу для робочої маси за заданим складом компонентів палива становить: %.4f МДж/кг", qri))

                // Виводимо результати
                resultsView.text = sb.toString()
                resultsView.visibility = TextView.VISIBLE
            } catch (e: Exception) {
                // Показуємо повідомлення про помилку у випадку некоректного введення
            // Toast.makeText(this, "Помилка: перевірте введені значення", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun parseInput(editText: EditText): Double {
        val raw = editText.text.toString().trim().replace(',', '.')
        if (raw.isEmpty()) throw IllegalArgumentException("Empty input")
        return raw.toDouble()
    }
}