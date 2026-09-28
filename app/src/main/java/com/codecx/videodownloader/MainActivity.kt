package com.codecx.videodownloader

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.codecx.videodownloader.data.utils.AppUpdateChecker
import com.codecx.videodownloader.databinding.ActivityMainBinding
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    private val appUpdateChecker: AppUpdateChecker by inject()
    private lateinit var binding: ActivityMainBinding

    private lateinit var navHost: NavHostFragment
    private lateinit var navController: NavController
    var isComingFromBackground = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appUpdateChecker.registerActivity(this)
        navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHost.navController
    }


    override fun onStop() {
        super.onStop()
        isComingFromBackground = true
    }

}