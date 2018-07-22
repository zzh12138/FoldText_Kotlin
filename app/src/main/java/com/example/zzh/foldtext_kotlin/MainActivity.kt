package com.example.zzh.foldtext_kotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        text.text = "111111123阿斯顿发阿斯顿发送到大。厦法定阿萨【德法师打发斯蒂芬撒地】方阿萨德法师打发斯问问蒂芬撒地方阿萨德法师打发斯蒂。芬撒地方发送到发送到发送到发送到发送到发送，到发送到发送到发送到，发送111111123阿斯顿发阿斯顿发送到大。厦法定阿萨【德法师打发斯蒂芬撒地】方阿萨德法师打发斯问问蒂芬撒地方阿萨德法师打发斯蒂。芬撒地方发送到发送到发送到发送到发送到发送，到发送到发送到发送到，发送"
        text.setOnClickListener { Toast.makeText(this@MainActivity, "textView点击事件", Toast.LENGTH_SHORT).show() }
        text1.text = "111111123阿斯顿发阿斯顿发送到大。厦法定阿萨【德法师打发斯蒂芬撒地】方阿萨德法师打发斯问问蒂芬撒地方阿萨德法师打发斯蒂。芬撒地方发送到发送到发送到发送到发送到发送，到发送到发送到发送到，发送111111123阿斯顿发阿斯顿发送到大。厦法定阿萨【德法师打发斯蒂芬撒地】方阿萨德法师打发斯问问蒂芬撒地方阿萨德法师打发斯蒂。芬撒地方发送到发送到发送到发送到发送到发送，到发送到发送到发送到，发送"
        parent1.setOnClickListener { Toast.makeText(this@MainActivity, "父View点击事件", Toast.LENGTH_SHORT).show() }
        text2.text = "111111123阿斯顿发阿斯顿发送到大。厦法定阿萨【德法师打发斯蒂芬撒地】方阿萨德法师打发斯问问蒂芬撒地方阿萨德法师打发斯蒂。芬撒地方发送到发送到发送到发送到发送到发送，到发送到发送到发送到，发送111111123阿斯顿发阿斯顿发送到大。厦法定阿萨【德法师打发斯蒂芬撒地】方阿萨德法师打发斯问问蒂芬撒地方阿萨德法师打发斯蒂。芬撒地方发送到发送到发送到发送到发送到发送，到发送到发送到发送到，发送"
        text2.setOnClickListener { Toast.makeText(this@MainActivity, "textView点击事件", Toast.LENGTH_SHORT).show() }
        text3.text = "111111123阿斯顿发阿斯顿发送到大。厦法定阿萨【德法师打发斯蒂芬撒地】方阿萨德法师打发斯问问蒂芬撒地方阿萨德法师打发斯蒂。芬撒地方发送到发送到发送到发送到发送到发送，到发送到发送到发送到，发送111111123阿斯顿发阿斯顿发送到大。厦法定阿萨【德法师打发斯蒂芬撒地】方阿萨德法师打发斯问问蒂芬撒地方阿萨德法师打发斯蒂。芬撒地方发送到发送到发送到发送到发送到发送，到发送到发送到发送到，发送"
        parent3.setOnClickListener { Toast.makeText(this@MainActivity, "父View点击事件", Toast.LENGTH_SHORT).show() }
    }
}
