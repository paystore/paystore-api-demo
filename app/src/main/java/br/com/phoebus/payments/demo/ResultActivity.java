package br.com.phoebus.payments.demo;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import br.com.phoebus.android.payments.api.Payment;
import br.com.phoebus.android.payments.api.PaymentV2;
import br.com.phoebus.android.payments.api.Refund;
import br.com.phoebus.android.payments.api.ReversePayment;
import br.com.phoebus.android.payments.api.SettleRequestResponse;
import br.com.phoebus.payments.demo.utils.DataTypeUtils;

public class ResultActivity extends AppCompatActivity {

    public static final String CLIENT_RECEIPT = "clientReceipt";
    public static final String MERCHANT_RECEIPT = "merchantReceipt";
    public static final String RESPONSE_DATA = "responseData";
    public static final String SHOW_BUTTON_CONFIRM = "showButtonConfirm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        this.setTitle(R.string.result_title);

        String clientReceipt = getIntent().getStringExtra(CLIENT_RECEIPT);
        String merchantReceipt = getIntent().getStringExtra(MERCHANT_RECEIPT);

        HashMap<String, String> data = (HashMap<String, String>) getIntent().getSerializableExtra(RESPONSE_DATA);
        boolean isShowButtonConfirm = getIntent().getBooleanExtra(SHOW_BUTTON_CONFIRM, true);

        TextView tf_receiptClient = (TextView) this.findViewById(R.id.tf_receipt_client);
        TextView tf_receiptMerchant = (TextView) this.findViewById(R.id.tf_receipt_merchant);

        TextView textViewClientReceipt = (TextView) this.findViewById(R.id.webview);
        textViewClientReceipt.setText(clientReceipt);

        TextView textViewMerchant = (TextView) this.findViewById(R.id.webviewMerchant);
        textViewMerchant.setText(merchantReceipt);

        if (clientReceipt == null || clientReceipt.isEmpty()) {
            textViewClientReceipt.setVisibility(View.INVISIBLE);
            tf_receiptClient.setVisibility(View.INVISIBLE);
        }

        if (merchantReceipt == null || merchantReceipt.isEmpty()) {
            textViewMerchant.setVisibility(View.INVISIBLE);
            tf_receiptMerchant.setVisibility(View.INVISIBLE);
        }

        Button btConfirm = (Button) this.findViewById(R.id.doConfirm);

        if (!isShowButtonConfirm) {
            btConfirm.setVisibility(View.INVISIBLE);
        }

        TableLayout dataTable = (TableLayout) this.findViewById(R.id.dataTable);

        createResponseDataView(data, dataTable);

    }

    private void createResponseDataView(HashMap<String, String> data, TableLayout dataTable) {

        boolean white = false;

        for (Map.Entry<String, String> entry : data.entrySet()) {
            TableRow tr = (TableRow) this.getLayoutInflater().inflate(R.layout.result_response_data_row, null);

            if (white = !white)
                tr.setBackgroundColor(Color.WHITE);

            TextView tv = (TextView) tr.findViewById(R.id.title_response_data);
            tv.setPaintFlags(tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tv.setText(entry.getKey());

            tv = (TextView) tr.findViewById(R.id.value_response_data);
            tv.setText(entry.getValue());

            if (entry.getKey().equals(getApplicationContext().getString(R.string.result_activity_payment_id))) {
                setClipboard(tv);
            }

            dataTable.addView(tr);
        }
    }

    public static void callResultIntent(PaymentV2 data, Context context, int activityFlags, Map<String, String> options) {
        Intent intentResult = new Intent(context, ResultActivity.class);
        intentResult.putExtra(ResultActivity.CLIENT_RECEIPT, data.getReceipt().getClientVia());
        intentResult.putExtra(ResultActivity.MERCHANT_RECEIPT, data.getReceipt().getMerchantVia());

        HashMap<String, String> dataMap = new LinkedHashMap<String, String>();
        dataMap.put(context.getString(R.string.result_activity_value), DataTypeUtils.getMoneyAsString(data.getValue()));
        dataMap.put(context.getString(R.string.result_activity_payment_type), DataTypeUtils.getAsString(data.getPaymentType()));
        dataMap.put(context.getString(R.string.result_activity_payment_id), data.getPaymentId());
        dataMap.put(context.getString(R.string.result_activity_acquirer_id), data.getAcquirerId());
        dataMap.put(context.getString(R.string.result_activity_auth), data.getAcquirerAuthorizationNumber());
        dataMap.put(context.getString(R.string.result_activity_acquirer), data.getAcquirer());
        dataMap.put(context.getString(R.string.result_activity_acquirer_datetime), DataTypeUtils.getAsString(data.getAcquirerResponseDate()));
        dataMap.put(context.getString(R.string.result_activity_terminal_datetime), DataTypeUtils.getAsString(data.getPaymentDate()));
        dataMap.put(context.getString(R.string.result_activity_resp_code), data.getAcquirerResponseCode());
        dataMap.put(context.getString(R.string.result_activity_product_short_name), data.getProductShortName());
        dataMap.put(context.getString(R.string.result_activity_ticket_number), data.getTicketNumber() != null ? data.getTicketNumber().toString() : "");
        if (data.getAdditionalValueType() != null) {
            dataMap.put(context.getString(R.string.result_activity_additional_value_type), data.getAdditionalValueType().name());
        }
        if (data.getAdditionalValue() != null) {
            dataMap.put(context.getString(R.string.result_activity_additional_value), DataTypeUtils.getMoneyAsString(data.getValue()));
        }
        dataMap.put(context.getString(R.string.result_activity_additional_account_type), data.getAccountTypeId());
        dataMap.put(context.getString(R.string.result_activity_additional_plan_id), data.getPlanId());
        dataMap.put(context.getString(R.string.result_activity_settlement_id), data.getBatchNumber());
        dataMap.put(context.getString(R.string.result_activity_terminal_nsu), data.getNsuTerminal());
        dataMap.put(context.getString(R.string.result_activity_acquirer_additional_msg), data.getAcquirerAdditionalMessage());

        if (data.getCard() != null)
            dataMap.put(context.getString(R.string.result_activity_card), data.getCard().getBin() + "..." + data.getCard().getPanLast4Digits() + " (" + data.getCard().getBrand() + ")");

        if (data.getCardToken() != null) {
            dataMap.put(context.getString(R.string.result_activity_card_token), data.getCardToken());
        }

        dataMap.put(context.getString(R.string.result_activity_installments), DataTypeUtils.getAsString(data.getInstallments()));
        dataMap.put(context.getString(R.string.result_activity_dni), data.getDni() != null ? data.getDni() : "");
        dataMap.put(context.getString(R.string.result_activity_notes), data.getNote() != null ? data.getNote() : "");

        intentResult.putExtra(ResultActivity.RESPONSE_DATA, dataMap);
        if (options != null) {
            intentResult.putExtra(ResultActivity.SHOW_BUTTON_CONFIRM, options.get(SHOW_BUTTON_CONFIRM) == "T");
        }

        if (activityFlags != 0)
            intentResult.setFlags(activityFlags);

        context.startActivity(intentResult);
    }

    private void setClipboard(TextView textView) {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("payment id", textView.getText().toString());
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getApplicationContext(), "paymentId copiado!", Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void callResultIntent(Payment data, Context context, int activityFlags, Map<String, String> options) {
        Intent intentResult = new Intent(context, ResultActivity.class);
        intentResult.putExtra(ResultActivity.CLIENT_RECEIPT, data.getReceipt().getClientVia());
        intentResult.putExtra(ResultActivity.MERCHANT_RECEIPT, data.getReceipt().getMerchantVia());

        HashMap<String, String> dataMap = new LinkedHashMap<String, String>();
        dataMap.put(context.getString(R.string.result_activity_value), DataTypeUtils.getMoneyAsString(data.getValue()));
        dataMap.put(context.getString(R.string.result_activity_payment_type), DataTypeUtils.getAsString(data.getPaymentType()));
        dataMap.put(context.getString(R.string.result_activity_payment_id), data.getPaymentId());
        dataMap.put(context.getString(R.string.result_activity_acquirer_id), data.getAcquirerId());
        dataMap.put(context.getString(R.string.result_activity_auth), data.getAcquirerAuthorizationNumber());
        dataMap.put(context.getString(R.string.result_activity_acquirer), data.getAcquirer());
        dataMap.put(context.getString(R.string.result_activity_acquirer_datetime), DataTypeUtils.getAsString(data.getAcquirerResponseDate()));
        dataMap.put(context.getString(R.string.result_activity_terminal_datetime), DataTypeUtils.getAsString(data.getPaymentDate()));
        dataMap.put(context.getString(R.string.result_activity_resp_code), data.getAcquirerResponseCode());
        dataMap.put(context.getString(R.string.result_activity_capture_type), DataTypeUtils.getAsString(data.getCaptureType()));

        if (data.getCard() != null)
            dataMap.put(context.getString(R.string.result_activity_card), data.getCard().getBin() + "..." + data.getCard().getPanLast4Digits() + " (" + data.getCard().getBrand() + ")");

        dataMap.put(context.getString(R.string.result_activity_installments), DataTypeUtils.getAsString(data.getInstallments()));


        intentResult.putExtra(ResultActivity.RESPONSE_DATA, dataMap);
        if (options != null) {
            intentResult.putExtra(ResultActivity.SHOW_BUTTON_CONFIRM, options.get(SHOW_BUTTON_CONFIRM) == "T");
        }

        if (activityFlags != 0)
            intentResult.setFlags(activityFlags);

        context.startActivity(intentResult);
    }

    public static void callResultIntent(ReversePayment data, Context context, int activityFlags) {
        Intent intentResult = new Intent(context, ResultActivity.class);
        intentResult.putExtra(ResultActivity.CLIENT_RECEIPT, data.getReceipt().getClientVia());
        intentResult.putExtra(ResultActivity.MERCHANT_RECEIPT, data.getReceipt().getMerchantVia());

        HashMap<String, String> dataMap = new HashMap<String, String>();
        dataMap.put(context.getString(R.string.result_activity_payment_id), data.getPaymentId());
        dataMap.put(context.getString(R.string.result_activity_acquirer_id), data.getAcquirerId());
        dataMap.put(context.getString(R.string.result_activity_auth), data.getAcquirerAuthorizationNumber());
        dataMap.put(context.getString(R.string.result_activity_resp_code), data.getAcquirerResponseCode());
        dataMap.put(context.getString(R.string.result_activity_acquirer_datetime), DataTypeUtils.getAsString(data.getAcquirerResponseDate()));
        dataMap.put(context.getString(R.string.result_activity_cancelable), (data.getCancelable() ? "Sim" : "Não"));
        dataMap.put(context.getString(R.string.result_activity_acquirer_additional_msg), data.getAcquirerAdditionalMessage());
        dataMap.put(context.getString(R.string.result_activity_ticket_number), String.valueOf(data.getTicketNumber()));
        dataMap.put(context.getString(R.string.result_activity_settlement_id), data.getBatchNumber());
        dataMap.put(context.getString(R.string.result_activity_terminal_nsu), data.getNsuTerminal());
        dataMap.put(context.getString(R.string.result_activity_holder_name), data.getCardHolderName());
        dataMap.put(context.getString(R.string.result_activity_card), data.getCardBin() + "..." + data.getPanLast4Digits());
        dataMap.put(context.getString(R.string.result_activity_terminal_id), data.getTerminalId());

        intentResult.putExtra(ResultActivity.RESPONSE_DATA, dataMap);
        if (activityFlags != 0)
            intentResult.setFlags(activityFlags);

        context.startActivity(intentResult);
    }

    public static void callResultIntent(Refund data, Context context, int activityFlags) {
        Intent intentResult = new Intent(context, ResultActivity.class);
        intentResult.putExtra(ResultActivity.CLIENT_RECEIPT, data.getReceipt().getClientVia());
        intentResult.putExtra(ResultActivity.MERCHANT_RECEIPT, data.getReceipt().getMerchantVia());

        HashMap<String, String> dataMap = new HashMap<String, String>();
        dataMap.put(context.getString(R.string.result_activity_payment_id), data.getRefundId());
        dataMap.put(context.getString(R.string.result_activity_acquirer_id), data.getAcquirerId());
        dataMap.put(context.getString(R.string.result_activity_auth), data.getAcquirerAuthorizationNumber());
        dataMap.put(context.getString(R.string.result_activity_resp_code), data.getAcquirerResponseCode());
        dataMap.put(context.getString(R.string.result_activity_acquirer_datetime), DataTypeUtils.getAsString(data.getAcquirerResponseDate()));
        dataMap.put(context.getString(R.string.result_activity_acquirer_additional_msg), data.getAcquirerAdditionalMessage());
        dataMap.put(context.getString(R.string.result_activity_product_short_name), data.getProductShortName());
        dataMap.put(context.getString(R.string.result_activity_settlement_id), data.getBatchNumber());
        dataMap.put(context.getString(R.string.result_activity_terminal_nsu), data.getNsuTerminal());

        intentResult.putExtra(ResultActivity.RESPONSE_DATA, dataMap);
        if (activityFlags != 0)
            intentResult.setFlags(activityFlags);

        context.startActivity(intentResult);
    }

    public static void callResultIntent(SettleRequestResponse data, Context context, int activityFlags) {
        Intent settlementResultIt = new Intent(context, ResultActivity.class);
        settlementResultIt.putExtra(SettlementResultActivity.MERCHANT_RECEIPT, data.getMerchantVia());

        HashMap<String, String> dataMap = new LinkedHashMap<String, String>();
        dataMap.put(context.getString(R.string.settlement_batch_number), data.getBatchNumber());
        dataMap.put(context.getString(R.string.settlement_acquirer_response_code), data.getAcquirerResponseCode());
        dataMap.put(context.getString(R.string.settlement_terminal_id), data.getTerminalId());
        dataMap.put(context.getString(R.string.settlement_message), data.getAcquirerAdditionalMessage());

        settlementResultIt.putExtra(ResultActivity.RESPONSE_DATA, dataMap);

        if (activityFlags != 0) {
            settlementResultIt.setFlags(activityFlags);
        }

        context.startActivity(settlementResultIt);
    }

}
