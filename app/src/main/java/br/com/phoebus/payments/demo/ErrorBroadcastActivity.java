package br.com.phoebus.payments.demo;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ErrorBroadcastActivity extends AppCompatActivity {
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.broadcast_error_title);
        setContentView(R.layout.activity_error_broadcast);

        textView = findViewById(R.id.error_broadcast);
        textView.setText((String) getIntent().getSerializableExtra("error"));
    }
}
