package br.com.phoebus.payments.demo;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class ResultActivity extends AppCompatActivity {

    public static final String CLIENT_RECEIPT = "clientReceipt";
    public static final String MERCHANT_RECEIPT = "merchantReceipt";
    public static final String RESPONSE_DATA = "responseData";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        this.setTitle(R.string.result_title);

        String clientReceipt = getIntent().getStringExtra(CLIENT_RECEIPT);
        String merchantReceipt = getIntent().getStringExtra(MERCHANT_RECEIPT);

        HashMap<String, String> data =  (HashMap<String, String>) getIntent().getSerializableExtra(RESPONSE_DATA);

        TextView textViewClientReceipt = (TextView) this.findViewById(R.id.webview);
        textViewClientReceipt.setText(clientReceipt);

        TextView textViewMerchant = (TextView) this.findViewById(R.id.webviewMerchant);
        textViewMerchant.setText(merchantReceipt);

        TableLayout dataTable = (TableLayout) this.findViewById(R.id.dataTable);

        createResponseDataView(data, dataTable);

    }

    private void createResponseDataView(HashMap<String, String> data, TableLayout dataTable) {

        boolean white = false;

        for (Map.Entry<String, String> entry: data.entrySet()) {
            TableRow tr = (TableRow) this.getLayoutInflater().inflate(R.layout.result_response_data_row, null);

            if (white = !white)
                tr.setBackgroundColor(Color.WHITE);

            TextView tv = (TextView) tr.findViewById(R.id.title_response_data);
            tv.setPaintFlags(tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tv.setText(entry.getKey());

            tv = (TextView) tr.findViewById(R.id.value_response_data);
            tv.setText(entry.getValue());

            dataTable.addView(tr);
        }
    }
}
