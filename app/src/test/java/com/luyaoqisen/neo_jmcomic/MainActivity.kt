package com.luyaoqisen.neo_jmcomic

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // 声明View Binding变量
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        
        // 设置内容视图为binding的根视图
        setContentView(binding.root)
        
        // 设置按钮点击事件监听器
        binding.submitButton.setOnClickListener {
            // 获取输入框中的文本
            val name = binding.nameEditText.text.toString().trim()
            
            // 检查输入是否为空
            if (name.isNotEmpty()) {
                // 更新TextView显示欢迎信息
                binding.titleTextView.text = "你好, $name!"
                
                // 显示Toast提示
                Toast.makeText(this, "欢迎, $name!", Toast.LENGTH_SHORT).show()
                
                // 清空输入框以便下次输入
                binding.nameEditText.text.clear()
            } else {
                // 如果输入为空，显示错误提示
                Toast.makeText(this, "请输入您的姓名", Toast.LENGTH_SHORT).show()
            }
        }
        
        // 可选：设置输入框的文本变化监听器，实时更新标题
        binding.nameEditText.setOnClickListener {
            val currentText = binding.nameEditText.text.toString()
            if (currentText.isNotEmpty()) {
                binding.titleTextView.text = "正在输入: $currentText"
            }
        }
    }
}