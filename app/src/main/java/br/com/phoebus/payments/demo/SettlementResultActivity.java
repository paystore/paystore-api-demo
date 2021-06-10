package br.com.phoebus.payments.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class SettlementResultActivity extends AppCompatActivity {

    public static final String MERCHANT_RECEIPT = "merchantReceipt";
    public static final String RESPONSE_DATA = "responseData";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settlement_result);

        this.setTitle(R.string.settlement_result_title);

        String merchantReceipt = getIntent().getStringExtra(MERCHANT_RECEIPT);

        HashMap<String, String> data =  (HashMap<String, String>) getIntent().getSerializableExtra(RESPONSE_DATA);

        TextView textViewMerchant = (TextView) this.findViewById(R.id.tv_merchant);
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