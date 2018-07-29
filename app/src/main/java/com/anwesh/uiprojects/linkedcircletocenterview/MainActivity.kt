package com.anwesh.uiprojects.linkedcircletocenterview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.anwesh.uiprojects.circletocenterview.CircleToCenterView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view : CircleToCenterView = CircleToCenterView.create(this)
        fullScreen()
        view.addAnimationListener({
            Toast.makeText(this, "${it} animation is complete", Toast.LENGTH_SHORT).show()
        }, {
            Toast.makeText(this, "${it} animation is reset", Toast.LENGTH_SHORT).show()
        })
    }
}

fun MainActivity.fullScreen() {
    supportActionBar?.hide()
    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
}