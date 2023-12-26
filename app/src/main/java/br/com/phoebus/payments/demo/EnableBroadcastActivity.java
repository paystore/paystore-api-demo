package br.com.phoebus.payments.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import br.com.phoebus.payments.demo.utils.Helper;

public class EnableBroadcastActivity extends AppCompatActivity {

    private CheckBox chbBroadcastErrors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.broadcast_error_title);
        setContentView(R.layout.activity_enable_broadcast);

        this.chbBroadcastErrors = findViewById(R.id.chb_broadcast_errors);
        chbBroadcastErrors.setChecked(Helper.readPrefsBoolean(this, Helper.BROADCAST_ERROR, Helper.PREF_CONFIG));

        chbBroadcastErrors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.writePrefs(getApplicationContext(), Helper.BROADCAST_ERROR, chbBroadcastErrors.isChecked(), Helper.PREF_CONFIG);
                onBackPressed();
                String toastMsg = chbBroadcastErrors.isChecked() ? getString(R.string.broadcast_enable) : getString(R.string.broadcast_no_enable);
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
