package com.kod.assurancecontracthandler

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kod.assurancecontracthandler.databinding.ActivityBaseBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
    }
}