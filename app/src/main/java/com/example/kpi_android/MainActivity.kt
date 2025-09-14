package com.example.kpi_android

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.example.kpi_android.prac1.Task1Activity
import com.example.kpi_android.prac1.Task2Activity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // На головному екрані є дві кнопки, які переносять
        // на екрани відповідного завдання. Шукаємо їх по id
        val btn1 = findViewById<Button>(R.id.btn_task1)
        val btn2 = findViewById<Button>(R.id.btn_task2)

        // Для того, щоб кнопки переносили на відповідні екрани завдань,
        // встановлюємо обробник натискання для кнопок
        btn1.setOnClickListener {
            startActivity(Intent(this, Task1Activity::class.java))
        }

        btn2.setOnClickListener {
            startActivity(Intent(this, Task2Activity::class.java))
        }
    }
}