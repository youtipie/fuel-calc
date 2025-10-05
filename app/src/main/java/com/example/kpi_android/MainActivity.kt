package com.example.kpi_android

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.example.kpi_android.prac1.Task1Activity
import com.example.kpi_android.prac1.Task2Activity
import com.example.kpi_android.prac2.Task1Activity as Prac2Task1Activity
import com.example.kpi_android.prac3.Task1Activity as Prac3Task1Activity
import com.example.kpi_android.prac4.Task1Activity as Prac4Task1Activity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Практична робота 1 - кнопки для завдань
        val btnPrac1Task1 = findViewById<Button>(R.id.btn_prac1_task1)
        val btnPrac1Task2 = findViewById<Button>(R.id.btn_prac1_task2)

        // Практична робота 2 - кнопка для завдання
        val btnPrac2Task1 = findViewById<Button>(R.id.btn_prac2_task1)

        // Практична робота 3 - кнопка для завдання
        val btnPrac3Task1 = findViewById<Button>(R.id.btn_prac3_task1)

        // Практична робота 4 - кнопка для завдання
        val btnPrac4Task1 = findViewById<Button>(R.id.btn_prac4_task1)

        // Встановлюємо обробники натискання для кнопок практичної роботи 1
        btnPrac1Task1.setOnClickListener {
            startActivity(Intent(this, Task1Activity::class.java))
        }

        btnPrac1Task2.setOnClickListener {
            startActivity(Intent(this, Task2Activity::class.java))
        }

        // Встановлюємо обробник натискання для кнопки практичної роботи 2
        btnPrac2Task1.setOnClickListener {
            startActivity(Intent(this, Prac2Task1Activity::class.java))
        }

        // Встановлюємо обробник натискання для кнопки практичної роботи 3
        btnPrac3Task1.setOnClickListener {
            startActivity(Intent(this, Prac3Task1Activity::class.java))
        }

        // Встановлюємо обробник натискання для кнопки практичної роботи 4
        btnPrac4Task1.setOnClickListener {
            startActivity(Intent(this, Prac4Task1Activity::class.java))
        }
    }
}