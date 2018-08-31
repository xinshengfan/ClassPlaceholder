package me.xp.gradle.classplaceholder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import me.xp.gradle.jarlibrary.JarConfig;
import me.xp.gradle.library.AarConfig;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvContent = findViewById(R.id.tv_content);
        StringBuilder sb = new StringBuilder();
        sb.append("AppConfig -> ")
                .append(new AppConfig().toString())
                .append("\nAarConfig -> ")
                .append(AarConfig.TEST_PUBLIC)
                .append("\nJarConfig -> ")
                .append(JarConfig.TEST_PUBLIC);

        System.out.println("sb = " + sb.toString());
        tvContent.setText(sb.toString());
    }


}
