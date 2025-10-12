package com.example.kpi_android.prac6

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt
import java.util.Locale

class Task1Activity : AppCompatActivity() {

    private lateinit var inputPhShlif: EditText    // Рн (Шліфувальний верстат), кВт
    private lateinit var inputKBPol: EditText      // КВ (Полірувальний верстат)
    private lateinit var inputTg: EditText         // tgφ (Циркулярна пила (13))
    private lateinit var btnCalc: Button
    private lateinit var tvResults: TextView

    private lateinit var tableDefaults: JSONObject
    private lateinit var dataKp1: JSONObject
    private lateinit var dataKp2: JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.kpi_android.R.layout.prac6_task1)

        inputPhShlif = findViewById(com.example.kpi_android.R.id.input_ph_shlif)
        inputKBPol = findViewById(com.example.kpi_android.R.id.input_kb_pol)
        inputTg = findViewById(com.example.kpi_android.R.id.input_tg)
        btnCalc = findViewById(com.example.kpi_android.R.id.btn_calc)
        tvResults = findViewById(com.example.kpi_android.R.id.results)

        // Завантажуємо JSON assets
        try {
            tableDefaults = JSONObject(readAssetText("prac_6_table_default_data.json"))
            dataKp1 = JSONObject(readAssetText("prac_6_data_1.json"))
            dataKp2 = JSONObject(readAssetText("prac_6_data_2.json"))
        } catch (ex: Exception) {
            Toast.makeText(this, "Не вдалося завантажити assets: ${ex.message}", Toast.LENGTH_LONG).show()
            return
        }

        prefillInputsFromDefaults()

        btnCalc.setOnClickListener {
            try {
                val phShlif = parseInput(inputPhShlif)
                val kbPol = parseInput(inputKBPol)
                val tgCirc = parseInput(inputTg)

                // Зчитуємо масиви з default JSON (блок "normal")
                val normal = tableDefaults.getJSONObject("normal")
                val namingArr = jsonArrayToStringList(normal.getJSONArray("naming"))
                val nuList = jsonArrayToDoubleList(normal.getJSONArray("nu[]"))
                val cosList = jsonArrayToDoubleList(normal.getJSONArray("cos[]"))
                val UhList = jsonArrayToDoubleList(normal.getJSONArray("Uh[]"))
                val nList = jsonArrayToDoubleList(normal.getJSONArray("n[]"))
                val PhListDefaults = jsonArrayToDoubleList(normal.getJSONArray("Ph[]"))
                val KBListDefaults = jsonArrayToDoubleList(normal.getJSONArray("KB[]"))
                val tgListDefaults = jsonArrayToDoubleList(normal.getJSONArray("tg[]"))

                // Знаходимо індекси потрібних машин за назвою (при можливості)
                val idxShlif = namingArr.indexOfFirst { it.contains("Шліфувальний") }.let { if (it >= 0) it else 0 }
                val idxCirc = namingArr.indexOfFirst { it.contains("Циркулярна пила") }.let { if (it >= 0) it else 2 }
                val idxPol = namingArr.indexOfFirst { it.contains("Полірувальний") }.let { if (it >= 0) it else 5 }

                // Підготовка робочих списків і заміна значень трьома введеними параметрами
                val phList = PhListDefaults.toMutableList()
                val kbList = KBListDefaults.toMutableList()
                val tgList = tgListDefaults.toMutableList()

                phList[idxShlif] = phShlif
                kbList[idxPol] = kbPol
                tgList[idxCirc] = tgCirc

                // Обчислюємо проміжні списки (зберігаємо повну точність для важливих сум)
                val nPhList = mutableListOf<Double>()
                val IpList = mutableListOf<Double>()
                val nPhKBList = mutableListOf<Double>()
                val nPhSquareList = mutableListOf<Double>()
                // Збираємо точну суму n·Pн·КВ·tg (без попереднього округлення)
                var nPhKBtgSumExact = 0.0

                for (i in phList.indices) {
                    val n = nList.getOrElse(i) { 1.0 }
                    val ph = phList[i]
                    val kb = kbList.getOrElse(i) { 0.0 }
                    val tg = tgList.getOrElse(i) { 0.0 }
                    val nu = nuList.getOrElse(i) { 1.0 }
                    val cos = cosList.getOrElse(i) { 1.0 }
                    val uh = UhList.getOrElse(i) { 0.38 }

                    val nPh = n * ph
                    nPhList.add(nPh)

                    val nPhKB = n * ph * kb
                    nPhKBList.add(nPhKB)

                    nPhSquareList.add(n * ph.pow(2))

                    nPhKBtgSumExact += (n * ph * kb * tg)

                    val denom = kotlin.math.sqrt(3.0) * uh * cos * nu
                    val ip = if (denom != 0.0) (nPh / denom) else 0.0
                    IpList.add(ip)
                }

                val sumNPh = nPhList.sum()
                val sumNPhKB = nPhKBList.sum()
                val sumNPhSquare = nPhSquareList.sum()

                // Груповий коефіцієнт використання
                val groupUseCoff = if (sumNPh != 0.0) (sumNPhKB / sumNPh) else 0.0

                // Ефективна кількість ЕП (ne)
                val ne = if (sumNPhSquare != 0.0) ceil(sumNPh.pow(2) / sumNPhSquare).toInt() else 1

                // Отримуємо Kp з dataKp1 (prac_6_data_1.json)
                val kp = getKp1(ne, groupUseCoff, dataKp1)

                // Pp := Kp * sum(nPhKB)
                val Pp = ((kp ?: 1.0) * sumNPhKB)
                // Qp — згідно з очікуваним результатом: сума n⋅Pн⋅КВ⋅tg без множення на Kp
                val Qp = nPhKBtgSumExact

                // Повна потужність Sp
                val Sp = sqrt(Pp.pow(2) + Qp.pow(2))

                // Груповий струм: беремо середнє Uн
                val meanUh = if (UhList.isNotEmpty()) UhList.average() else 0.38
                val IpGroup = if (meanUh != 0.0) Pp / meanUh else 0.0

                // Значення для всього цеху беремо з блоку "all" у defaults
                val all = tableDefaults.optJSONObject("all")
                val nPh_all = all?.optDouble("nPh", sumNPh) ?: sumNPh
                val nPhKB_all = all?.optDouble("nPhKB", sumNPhKB) ?: sumNPhKB
                val nPhKBtg_all = all?.optDouble("nPhKBtg", nPhKBtgSumExact) ?: nPhKBtgSumExact
                val nPhSquare_all = all?.optDouble("nPh_square", sumNPhSquare) ?: sumNPhSquare

                // Коефіцієнт використання цеху в цілому
                val groupUseCoffAll = if (nPh_all != 0.0) String.format(Locale.US, "%.2f", (nPhKB_all / nPh_all)).toDouble() else 0.0

                // ne_all як у Python: round(nPh_all ** 2 / nPhSquare_all)
                val neAll = kotlin.math.round(nPh_all.pow(2) / nPhSquare_all).toInt()

                val kpAll = getKp2(neAll, groupUseCoffAll, dataKp2) ?: (kp ?: 1.0)
                val PpAll = kpAll * nPhKB_all
                val QpAll = kpAll * nPhKBtg_all
                val SpAll = sqrt(PpAll.pow(2) + QpAll.pow(2))
                val IpAll = if (meanUh != 0.0) PpAll / meanUh else 0.0

                // Формуємо вивід у потрібному форматі та точностях
                val sb = StringBuilder()
                sb.append("1. Для заданого складу ЕП та їх характеристик цехової мережі силове навантаження  становитиме:\n")
                sb.append(String.format(Locale.forLanguageTag("uk"), "1.1. Груповий коефіцієнт використання для ШР1=ШР2=ШР3: %.4f;%n", groupUseCoff))
                sb.append(String.format(Locale.forLanguageTag("uk"), "1.2. Ефективна кількість ЕП для ШР1=ШР2=ШР3: %d;%n", ne))
                sb.append(String.format(Locale.forLanguageTag("uk"), "1.3. Розрахунковий коефіцієнт активної потужності для ШР1=ШР2=ШР3: %.2f:%n", kp ?: Double.NaN))
                sb.append(String.format(Locale.forLanguageTag("uk"), "1.4. Розрахункове активне навантаження для ШР1=ШР2=ШР3: %.2f кВт;%n", roundTo(Pp, 2)))
                sb.append(String.format(Locale.forLanguageTag("uk"), "1.5. Розрахункове реактивне навантаження для ШР1=ШР2=ШР3: %.3f квар.;%n", roundTo(Qp, 3)))
                sb.append(String.format(Locale.forLanguageTag("uk"), "1.6. Повна потужність для ШР1=ШР2=ШР3: %.4f кВ*А;%n", roundTo(Sp, 4)))
                sb.append(String.format(Locale.forLanguageTag("uk"), "1.7. Розрахунковий груповий струм для ШР1=ШР2=ШР3: %.2f А;%n", roundTo(IpGroup, 2)))
                sb.append(String.format(Locale.forLanguageTag("uk"), "1.8. Коефіцієнти використання цеху в цілому: %.2f;%n", groupUseCoffAll))
                sb.append(String.format(Locale.forLanguageTag("uk"), "1.9. Ефективна кількість ЕП цеху в цілому: %d;%n", neAll))
                sb.append(String.format(Locale.forLanguageTag("uk"), "1.10. Розрахунковий коефіцієнт активної потужності цеху в цілому: %.2f;%n", kpAll))
                sb.append(String.format(Locale.forLanguageTag("uk"), "1.11. Розрахункове активне навантаження на шинах 0,38 кВ ТП: %.1f кВт;%n", roundTo(PpAll, 1)))
                sb.append(String.format(Locale.forLanguageTag("uk"), "1.12. Розрахункове реактивне навантаження на шинах 0,38 кВ ТП: %.1f квар;%n", roundTo(QpAll, 1)))
                sb.append(String.format(Locale.forLanguageTag("uk"), "1.13. Повна потужність на шинах 0,38 кВ ТП: %.0f кВ*А;%n", roundTo(SpAll, 0)))
                sb.append(String.format(Locale.forLanguageTag("uk"), "1.14. Розрахунковий груповий струм на шинах 0,38 кВ ТП: %.3f А.%n", roundTo(IpAll, 3)))

                tvResults.text = sb.toString()
                tvResults.visibility = View.VISIBLE

            } catch (ex: Exception) {
                Toast.makeText(this, "Помилка: перевірте введені значення: ${ex.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- допоміжні методи

    // Автозаповнення трьома полями з defaults для зручності
    private fun prefillInputsFromDefaults() {
        try {
            val normal = tableDefaults.getJSONObject("normal")
            val namingArr = jsonArrayToStringList(normal.getJSONArray("naming"))
            val PhListDefaults = jsonArrayToDoubleList(normal.getJSONArray("Ph[]"))
            val KBListDefaults = jsonArrayToDoubleList(normal.getJSONArray("KB[]"))
            val tgListDefaults = jsonArrayToDoubleList(normal.getJSONArray("tg[]"))

            val idxShlif = namingArr.indexOfFirst { it.contains("Шліфувальний") }.let { if (it >= 0) it else 0 }
            val idxCirc = namingArr.indexOfFirst { it.contains("Циркулярна пила") }.let { if (it >= 0) it else 2 }
            val idxPol = namingArr.indexOfFirst { it.contains("Полірувальний") }.let { if (it >= 0) it else 5 }

            inputPhShlif.setText(String.format(Locale.US, "%.0f", PhListDefaults[idxShlif]))
            inputKBPol.setText(String.format(Locale.US, "%.2f", KBListDefaults[idxPol]))
            inputTg.setText(String.format(Locale.US, "%.2f", tgListDefaults[idxCirc]))
        } catch (_: Exception) {
            // ігноруємо автозаповнення, якщо чогось не вистачає
        }
    }

    // Зчитати текст з assets
    private fun readAssetText(filename: String): String =
        assets.open(filename).bufferedReader(Charsets.UTF_8).use { it.readText() }

    // Конвертація JSONArray -> List<Double> (витримує "-" та рядки)
    private fun jsonArrayToDoubleList(arr: JSONArray): MutableList<Double> {
        val out = mutableListOf<Double>()
        for (i in 0 until arr.length()) {
            val v = arr.opt(i)
            when (v) {
                is Number -> out.add(v.toDouble())
                is String -> {
                    val s = v.trim()
                    if (s == "-" || s.isEmpty()) out.add(0.0) else out.add(s.replace(',', '.').toDouble())
                }
                else -> out.add(0.0)
            }
        }
        return out
    }

    // Конвертація JSONArray -> List<String>
    private fun jsonArrayToStringList(arr: JSONArray): MutableList<String> {
        val out = mutableListOf<String>()
        for (i in 0 until arr.length()) out.add(arr.optString(i))
        return out
    }

    // Безпечне перетворення JSONObject.keys() в List<String> (щоб уникнути одноразового ітератора)
    private fun jsonObjectKeysList(obj: JSONObject): List<String> {
        val keys = mutableListOf<String>()
        val iter = obj.keys()
        while (iter.hasNext()) keys.add(iter.next())
        return keys
    }

    // Метод для пошуку значення розрахункових коефіцієнтів Кр для мереж живлення напругою до 1000 В (Т0 = 10 хв.)
    private fun getKp1(ne: Int, groupUse: Double, data: JSONObject): Double? {
        val coffKey = findClosestCoffKeyInJson(data, groupUse) ?: return null
        if (data.has(ne.toString())) {
            val row = data.getJSONObject(ne.toString())
            if (!row.isNull(coffKey)) return row.getDouble(coffKey)
            return null
        } else {
            val neKeys = jsonObjectKeysList(data).mapNotNull { it.toIntOrNull() }.sorted()
            val lower = neKeys.filter { it < ne }.maxOrNull()
            val higher = neKeys.filter { it > ne }.minOrNull()
            if (lower != null && higher != null) {
                val rowL = data.getJSONObject(lower.toString())
                val rowH = data.getJSONObject(higher.toString())
                if (!rowL.isNull(coffKey) && !rowH.isNull(coffKey)) {
                    val vL = rowL.getDouble(coffKey)
                    val vH = rowH.getDouble(coffKey)
                    val slope = (vH - vL) / (higher - lower)
                    return vL + slope * (ne - lower)
                }
            }
        }
        return null
    }

    // Знайти відповідний ключ коефіцієнта (max <= groupUse) у JSON
    private fun findClosestCoffKeyInJson(data: JSONObject, groupUse: Double): String? {
        val keys = jsonObjectKeysList(data)
        if (keys.isEmpty()) return null
        val sample = data.getJSONObject(keys.first())
        val coffs = jsonObjectKeysList(sample).mapNotNull { it.toDoubleOrNull() }.sorted()
        if (coffs.isEmpty()) return null
        val chosen = coffs.filter { it <= groupUse }.maxOrNull() ?: coffs.firstOrNull()
        return chosen?.toString()
    }

    // getKp2: знаходить інтервал "a;b", що містить ne, і повертає значення для найближчого coff <= groupUse
    private fun getKp2(ne: Int, groupUse: Double, data: JSONObject): Double? {
        val rangeKeys = jsonObjectKeysList(data)
        for (rng in rangeKeys) {
            val parts = rng.split(";")
            if (parts.size == 2) {
                val a = parts[0].toIntOrNull() ?: continue
                val b = parts[1].toIntOrNull() ?: continue
                if (ne in a..b) {
                    val row = data.getJSONObject(rng)
                    val coffs = jsonObjectKeysList(row).mapNotNull { it.toDoubleOrNull() }.sorted()
                    if (coffs.isEmpty()) return null
                    val chosen = coffs.filter { it <= groupUse }.maxOrNull() ?: coffs.firstOrNull()
                    return chosen?.let { row.optDouble(it.toString()) }
                }
            } else {
                // Обробка одиночних ключів, наприклад "1"
                val maybe = rng.toIntOrNull()
                if (maybe != null && ne == maybe) {
                    val row = data.getJSONObject(rng)
                    val coffs = jsonObjectKeysList(row).mapNotNull { it.toDoubleOrNull() }.sorted()
                    val chosen = coffs.filter { it <= groupUse }.maxOrNull() ?: coffs.firstOrNull()
                    return chosen?.let { row.optDouble(it.toString()) }
                }
            }
        }
        return null
    }

    // Допоміжний метод для округлення до вказаної кількості знаків після коми
    private fun roundTo(value: Double, digits: Int): Double {
        if (digits <= 0) return kotlin.math.round(value)
        val factor = 10.0.pow(digits)
        return kotlin.math.round(value * factor) / factor
    }

    // Парсинг введеного значення
    private fun parseInput(editText: EditText): Double {
        val raw = editText.text.toString().trim().replace(',', '.')
        if (raw.isEmpty()) throw IllegalArgumentException("Empty input")
        return raw.toDouble()
    }
}
