package com.example.kpi_android.prac1

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kpi_android.R
import java.util.Locale

class Task1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prac1_task1)

        //  Отримання користувацього вводу
        val inpHp = findViewById<EditText>(R.id.input_hp)
        val inpCp = findViewById<EditText>(R.id.input_cp)
        val inpSp = findViewById<EditText>(R.id.input_sp)
        val inpNp = findViewById<EditText>(R.id.input_np)
        val inpOp = findViewById<EditText>(R.id.input_op)
        val inpWp = findViewById<EditText>(R.id.input_wp)
        val inpAp = findViewById<EditText>(R.id.input_ap)
        val btnCalc = findViewById<Button>(R.id.btn_calc1)
        val resultsView = findViewById<TextView>(R.id.results1)

        // Відображення результату відбувається при натисканні на кнопку,
        // отже потрібен обробник натискань
        btnCalc.setOnClickListener {
            try {
                val hp = parseInput(inpHp)
                val cp = parseInput(inpCp)
                val sp = parseInput(inpSp)
                val np = parseInput(inpNp)
                val op = parseInput(inpOp)
                val wp = parseInput(inpWp)
                val ap = parseInput(inpAp)

                // Обчислюємо коефіцієнт переходу від робочої до сухої маси та
                // коефіцієнт переходу від робочої до горючої маси
                val kpc = 100.0 / (100.0 - wp)
                val kpg = 100.0 / (100.0 - wp - ap)

                // Обчислюємо нижчу теплоту згоряння для робочої, сухої та горючої маси
                val qph = (339.0 * cp + 1030.0 * hp - 108.8 * (op - sp) - 25.0 * wp) / 1000.0
                val qch = (qph + 0.025 * wp) * 100.0 / (100.0 - wp)
                val qgh = (qph + 0.025 * wp) * 100.0 / (100.0 - wp - ap)

                // Обчислюємо склад сухої маси палива
                val hc = hp * kpc
                val cc = cp * kpc
                val sc = sp * kpc
                val nc = np * kpc
                val oc = op * kpc
                val ac = ap * kpc

                // Обчислюємо склад горючої маси палива
                val hg = hp * kpg
                val cg = cp * kpg
                val sg = sp * kpg
                val ng = np * kpg
                val og = op * kpg

                // Форматування результатів
                val sb = StringBuilder()
                sb.append(String.format(Locale.US, "1.1. Коефіцієнт переходу від робочої до сухої маси становить: %.2f\n", kpc))
                sb.append(String.format(Locale.US, "1.2. Коефіцієнт переходу від робочої до горючої маси становить: %.2f\n", kpg))
                sb.append(String.format(Locale.US, "1.3. Склад сухої маси палива становитиме: Hᶜ%.2f%%; Cᶜ=%.2f%%; Sc=%.2f%%; Nᶜ=%.2f%%; Oᶜ=%.2f%%; Aᶜ=%.2f%%\n", hc, cc, sc, nc, oc, ac))
                sb.append(String.format(Locale.US, "1.4. Склад горючої маси палива становитиме: Hᵍ=%.2f%%, Cᵍ=%.2f%%, Sᵍ=%.2f%%, Nᵍ=%.2f%%, Oᵍ=%.2f%%\n", hg, cg, sg, ng, og))
                sb.append(String.format(Locale.US, "1.5. Нижча теплота згоряння для робочої маси за заданим складом компонентів палива становить: %.4f МДж/кг\n", qph))
                sb.append(String.format(Locale.US, "1.6. Нижча теплота згоряння для сухої маси за заданим складом компонентів палива становить: %.4f МДж/кг\n", qch))
                sb.append(String.format(Locale.US, "1.7. Нижча теплота згоряння для горючої маси за заданим складом компонентів палива становить: %.4f МДж/кг" , qgh))

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