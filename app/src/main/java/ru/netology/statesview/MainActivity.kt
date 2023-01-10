package ru.netology.statesview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.netology.statesview.ui.StatesView
import ru.netology.statesview.util.StatesViewKey

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val view = findViewById<StatesView>(R.id.statesView)
        view.postDelayed( {
            view.data =
                mapOf(
                    Pair(StatesViewKey.FULL_AMOUNT, listOf(4000F)),
                    Pair(
                        StatesViewKey.DATA,
                        listOf(
                            1000F,
                            1000F,
                            1000F,
                            1000F
                        )
                    )
                )

        }, 300)
    }
}