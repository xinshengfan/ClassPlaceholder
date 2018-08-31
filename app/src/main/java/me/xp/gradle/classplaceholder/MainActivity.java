package me.xp.gradle.classplaceholder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.shishike.mobile.commonlib.config.Urls;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvContent = findViewById(R.id.tv_content);
        StringBuilder sb = new StringBuilder();
        sb.append(Urls.URL_CLASS_NAME)
                .append("\n")
                .append(Urls.URL_PROPERTIES_FILE)
                .append("\n")
                .append(G.URL_JSON_FILE);

        System.out.println("sb = " + sb.toString());
        tvContent.setText(sb.toString());
    }

    public void click(View view) {
        switch (view.getId()) {
            case R.id.btn_click:
                Toast.makeText(this, "G>>" + G.URL_JSON_FILE, Toast.LENGTH_LONG).show();
                break;
        }
    }
}
