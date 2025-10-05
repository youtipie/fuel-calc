package com.example.kpi_android.prac5

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.kpi_android.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.util.*

class Task1Activity : AppCompatActivity() {
    private var data: Map<String, List<Double>> = emptyMap()
    private var elementCount = 0
    private lateinit var dynamicInputs: LinearLayout
    private lateinit var btnCalc: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prac5_task1)

        dynamicInputs = findViewById(R.id.dynamic_inputs)
        btnCalc = findViewById(R.id.btn_calc)
        val btnAddElement = findViewById<Button>(R.id.btn_add_element)
        val inpZpera = findViewById<EditText>(R.id.input_zpera)
        val inpZperp = findViewById<EditText>(R.id.input_zperp)
        val resultsView = findViewById<TextView>(R.id.results)

        // Завантажуємо дані з JSON файлу
        loadData()

        // Обробник додавання елемента
        btnAddElement.setOnClickListener {
            addElementView()
        }

        // Обробник розрахунку
        btnCalc.setOnClickListener {
            try {
                // Збираємо дані з динамічних елементів
                val quantities = mutableListOf<Int>()
                val elements = mutableListOf<String>()

                for (i in 0 until dynamicInputs.childCount) {
                    val itemView = dynamicInputs.getChildAt(i)
                    val quantityInput = itemView.findViewById<EditText>(R.id.item_quantity)
                    val elementSpinner = itemView.findViewById<Spinner>(R.id.item_element)

                    val quantity = quantityInput.text.toString().toIntOrNull() ?: 1
                    val element = elementSpinner.selectedItem?.toString() ?: ""

                    if (element.isEmpty() || element == "Оберіть елемент") {
                        Toast.makeText(this, "Оберіть усі елементи", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    quantities.add(quantity)
                    elements.add(element)
                }

                if (quantities.isEmpty()) {
                    Toast.makeText(this, "Додайте хоча б один елемент", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Розрахуємо частоту відмов одноколової системи
                var woc = 0.0
                for (i in quantities.indices) {
                    woc += quantities[i] * data[elements[i]]!![0]
                }

                // Розрахуємо середню тривалість відновлення
                var tvocSum = 0.0
                for (i in quantities.indices) {
                    tvocSum += quantities[i] * data[elements[i]]!![0] * data[elements[i]]!![1]
                }
                val tvoc = tvocSum / woc

                // Коефіцієнт аварійного простою одноколової системи
                val kaoc = (woc * tvoc) / 8760

                // Коефіцієнт планового простою одноколової системи
                val maxPlannedDowntime = elements.maxOf { data[it]!![2] }
                val kpoc = 1.2 * maxPlannedDowntime / 8760

                // Тоді частота відмов одночасно двох кіл двоколової системи
                val wdk = 2 * woc * (kaoc + kpoc)

                // Отже, частота відмов двоколової системи з урахуванням секційного вимикача
                val wdc = wdk + 0.02

                // Розрахуємо коефіцієнт
                val koef = woc / wdc

                // Отримання користувацького вводу для другого пункту завдання
                val zpera = parseInput(inpZpera)
                val zperp = parseInput(inpZperp)

                // Константи
                val w = 0.01
                val tv = 45 * 1e-3
                val pm = 5.12 * 1e3
                val tm = 6451.0
                val kp = 4 * 1e-3

                // Розрахуємо математичне сподівання аварійного
                // та планового недовідпущення електроенергії
                val m1 = w * tv * pm * tm
                val m2 = kp * pm * tm

                // Тоді можемо розрахувати математичне сподівання збитків
                // від переривання електропостачання
                val m = zpera * m1 + zperp * m2

                // Форматування результатів
                val sb = StringBuilder()
                sb.append("Результати:\n\n")
                sb.append(
                    String.format(
                        Locale.US,
                        "1.1 Частота відмов одноколової системи: ω‎oc = %.4f рік⁻¹\n\n",
                        woc
                    )
                )
                sb.append(
                    String.format(
                        Locale.US,
                        "1.2 Частота відмов двоколової системи: ω‎дc = %.4f рік⁻¹\n\n",
                        wdc
                    )
                )
                if (koef > 1) {
                    sb.append("1.3 Надійність двоколової системи електропередачі є значно вищою ніж одноколової.\n\n")
                } else {
                    sb.append("1.3 Надійність одноколової системи електропередачі є значно вищою ніж двоколової.\n\n")
                }
                sb.append(
                    String.format(
                        Locale.US,
                        "2. Математичне сподівання збитків від переривання електропостачання: M(Зпер) = %.0f",
                        m
                    )
                )

                // Виводимо результати
                resultsView.text = sb.toString()
                resultsView.visibility = TextView.VISIBLE

            } catch (e: Exception) {
                Toast.makeText(this, "Помилка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Допоміжний метод, для завантаження данних елементів ЕПС
    private fun loadData() {
        try {
            val inputStream = assets.open("prac_5_data.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<Map<String, List<Double>>>() {}.type
            data = Gson().fromJson(reader, type)
            reader.close()
        } catch (e: Exception) {
            Toast.makeText(this, "Помилка завантаження даних: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun addElementView() {
        val inflater = LayoutInflater.from(this)
        val itemView = inflater.inflate(R.layout.prac5_element_item, dynamicInputs, false)

        val spinner = itemView.findViewById<Spinner>(R.id.item_element)
        val deleteBtn = itemView.findViewById<ImageButton>(R.id.btn_delete)

        // Заповнюємо spinner
        val elementNames = listOf("Оберіть елемент") + data.keys.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, elementNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Обробник видалення
        deleteBtn.setOnClickListener {
            dynamicInputs.removeView(itemView)
            elementCount--
            updateButtonState()
        }

        dynamicInputs.addView(itemView)
        elementCount++
        updateButtonState()
    }

    private fun updateButtonState() {
        btnCalc.isEnabled = elementCount > 0
    }

    private fun parseInput(editText: EditText): Double {
        val raw = editText.text.toString().trim().replace(',', '.')
        if (raw.isEmpty()) throw IllegalArgumentException("Порожнє поле")
        return raw.toDouble()
    }
}