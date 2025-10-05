package com.example.kpi_android.prac4

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.kpi_android.R
import org.json.JSONObject
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

class Task1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prac4_task1)

        // Отримання користувацького вводу
        val inpIk = findViewById<EditText>(R.id.input_ik)
        val spinnerCabel = findViewById<Spinner>(R.id.spinner_cabel)
        val inpTf = findViewById<EditText>(R.id.input_tf)
        val inpSm = findViewById<EditText>(R.id.input_sm)
        val inpTm = findViewById<EditText>(R.id.input_tm)
        val inpSk = findViewById<EditText>(R.id.input_sk)
        val btnCalc = findViewById<Button>(R.id.btn_calc)
        val resultsView = findViewById<TextView>(R.id.results)

        // Налаштування Spinner для вибору кабеля
        val cabelOptions = arrayOf(
            "Оберіть тип кабеля",
            "Мідні неізольовані проводи та шини",
            "Алюмінієві неізольовані проводи та шини",
            "Кабелі з паперовою і проводи з гумовою та полівінілхлоридною ізоляцією з мідними жилами",
            "Кабелі з паперовою і проводи з гумовою та полівінілхлоридною ізоляцією з алюмінієвими жилами",
            "Кабелі з гумовою та пластмасовою ізоляцією з мідними жилами",
            "Кабелі з гумовою та пластмасовою ізоляцією з алюмінієвими жилами"
        )
        val adapter = ArrayAdapter(this, R.layout.spinner_item, cabelOptions)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerCabel.adapter = adapter

        // Відображення результату відбувається при натисканні на кнопку
        btnCalc.setOnClickListener {
            try {
                // Парсимо введені значення
                val cabel = spinnerCabel.selectedItemPosition - 1
                if (cabel < 0) {
                    Toast.makeText(this, "Оберіть тип кабеля", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val ik = parseInput(inpIk)
                val tf = parseInput(inpTf)
                val sm = parseInput(inpSm)
                val tm = parseInput(inpTm)
                val sk = parseInput(inpSk)

                // 1. Розрахунковий струм для нормального і післяаварійного режимів
                val im = (sm / 2) / (sqrt(3.0) * 10)
                val imPa = 2 * im

                // Отримуємо економічну густину струму
                val jek = getJek(cabel, tm)
                // Рахуємо економічний переріз
                val sek = im / jek
                // Шукаємо мінімальний переріз
                val sMin = (ik * sqrt(tf)) / 92
                // На основі мінімального перерізу шукаємо кабель з потрібним перерізом
                val s = getCrossSection(sMin)

                // 2. Рахуємо опори елементів
                val xc = 10.5.pow(2) / sk
                val xt = (10.5 / 100) * (10.5.pow(2) / 6.3)
                // Сумарний опір
                val xe = xc + xt
                // Початкове діюче значення струму трифазного КЗ
                val ip0 = 10.5 / (sqrt(3.0) * xe)

                // 3. Сталі дані, передані з підстанції
                val rcn = 10.65
                val xcn = 24.02
                val rcmin = 34.88
                val xcmin = 65.68
                val ukMax = 11.1
                val uvn = 115.0
                val unn = 11.0
                val snomt = 6.3

                // Розрахуємо реактивний опір силового трансформатора
                val xtTrans = (ukMax * uvn.pow(2)) / (100 * snomt)

                // Розрахуємо опори на шинах 10 кВ в нормальному та мінімальному режимах
                val rsh = rcn
                val xsh = xcn + xtTrans
                val zsh = sqrt(rsh.pow(2) + xsh.pow(2))
                val rshmin = rcmin
                val xshmin = xcmin + xtTrans
                val zshmin = sqrt(rshmin.pow(2) + xshmin.pow(2))

                // Розраховуємо струми трифазного та двофазного КЗ на шинах 10 кВ
                val ish3 = (uvn * 10.0.pow(3)) / (sqrt(3.0) * zsh)
                val ish2 = ish3 * sqrt(3.0) / 2
                val ishMin3 = (uvn * 10.0.pow(3)) / (sqrt(3.0) * zshmin)
                val ishMin2 = ishMin3 * sqrt(3.0) / 2

                // Розраховуємо коефіцієнт приведення
                val kpr = unn.pow(2) / uvn.pow(2)

                // Розраховуємо опори на шинах 10 кВ
                val rshn = rsh * kpr
                val xshn = xsh * kpr
                val zshn = sqrt(rshn.pow(2) + xshn.pow(2))
                val rshnMin = rshmin * kpr
                val xshnMin = xshmin * kpr
                val zshnMin = sqrt(rshnMin.pow(2) + xshnMin.pow(2))

                // Розраховуємо дійсні струми трифазного та двофазного КЗ
                val ishn3 = (unn * 10.0.pow(3)) / (sqrt(3.0) * zshn)
                val ishn2 = ishn3 * sqrt(3.0) / 2
                val ishnMin3 = (unn * 10.0.pow(3)) / (sqrt(3.0) * zshnMin)
                val ishnMin2 = ishnMin3 * sqrt(3.0) / 2

                // Розрахунок струмів короткого замикання відхідних ліній 10 кВ
                val r0 = 0.64
                val x0 = 0.363
                val il = 0.2 + 0.35 + 0.2 + 0.6 + 2 + 2.55 + 3.37 + 3.1
                val rl = il * r0
                val xl = il * x0

                // Розрахуємо опори в нормальному та мінімальному режимах
                val ren = rl + rshn
                val xen = xl + xshn
                val zen = sqrt(ren.pow(2) + xen.pow(2))
                val renMin = rl + rshnMin
                val xenMin = xl + xshnMin
                val zenMin = sqrt(renMin.pow(2) + xenMin.pow(2))

                // Розрахуємо струми трифазного і двофазного КЗ
                val iln3 = (unn * 10.0.pow(3)) / (sqrt(3.0) * zen)
                val iln2 = iln3 * sqrt(3.0) / 2
                val ilnMin3 = (unn * 10.0.pow(3)) / (sqrt(3.0) * zenMin)
                val ilnMin2 = ilnMin3 * sqrt(3.0) / 2

                // Форматування результатів
                val sb = StringBuilder()
                sb.append("Результати:\n\n")
                sb.append(String.format(Locale.US, "1.1 Розрахунковий струм для нормального режиму: %.2f A. Для післяаварійного режиму: %.2f A\n\n", im, imPa))
                sb.append(String.format(Locale.US, "1.2 Економічний переріз становить: %.2f. Переріз жил кабеля: %.2f\n\n", sek, s))
                sb.append(String.format(Locale.US, "2. Початкове діюче значення струму трифазного КЗ становить: %.2f кА\n\n", ip0))
                sb.append("3.1 Струми трифазного та двофазного КЗ на шинах 10 кВ в нормальному та мінімальному режимах, приведені до напруги 110 кВ:\n")
                sb.append(String.format(Locale.US, "Iш⁽³⁾=%.2f A\n", ish3))
                sb.append(String.format(Locale.US, "Iш⁽²⁾=%.2f A\n", ish2))
                sb.append(String.format(Locale.US, "Iш.min⁽³⁾=%.2f A\n", ishMin3))
                sb.append(String.format(Locale.US, "Iш.min⁽²⁾=%.2f A\n\n", ishMin2))
                sb.append("3.2 Дійсні струми трифазного та двофазного КЗ на шинах 10 кВ в нормальному та мінімальному режимах:\n")
                sb.append(String.format(Locale.US, "Iш.н⁽³⁾=%.2f A\n", ishn3))
                sb.append(String.format(Locale.US, "Iш.н⁽²⁾=%.2f A\n", ishn2))
                sb.append(String.format(Locale.US, "Iш.н.min⁽³⁾=%.2f A\n", ishnMin3))
                sb.append(String.format(Locale.US, "Iш.н.min⁽²⁾=%.2f A\n\n", ishnMin2))
                sb.append("3.3 Струми короткого замикання:\n")
                sb.append(String.format(Locale.US, "Iл.н⁽³⁾=%.2f A\n", iln3))
                sb.append(String.format(Locale.US, "Iл.н⁽²⁾=%.2f A\n", iln2))
                sb.append(String.format(Locale.US, "Iл.н.min⁽³⁾=%.2f A\n", ilnMin3))
                sb.append(String.format(Locale.US, "Iл.н.min⁽²⁾=%.2f A", ilnMin2))

                // Виводимо результати
                resultsView.text = sb.toString()
                resultsView.visibility = View.VISIBLE
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

    // Метод, що читає дані про кабеля з JSON
    // та повертає відповідне значення економічної густини струму
    private fun getJek(index: Int, tm: Double): Double {
        val jsonString = assets.open("prac_4_cabels_data.json").bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)

        val category = when {
            tm in 1000.0..3000.0 -> "1000-3000"
            tm in 3000.0..5000.0 -> "3000-5000"
            tm > 5000.0 -> "5000+"
            else -> throw IllegalArgumentException("Invalid Tm value")
        }

        val array = jsonObject.getJSONArray(category)
        return array.getDouble(index)
    }

    // Метод, що "заокруглює" значення перерізу кабеля
    private fun getCrossSection(value: Double): Double {
        val crossSections = listOf(10.0, 16.0, 25.0, 35.0, 50.0, 70.0, 95.0, 120.0, 150.0, 185.0, 240.0)
        return crossSections.minByOrNull { kotlin.math.abs(it - value) } ?: 10.0
    }
}