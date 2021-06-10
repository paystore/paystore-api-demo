package br.com.phoebus.payments.demo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import br.com.phoebus.android.payments.api.Payment;
import br.com.phoebus.android.payments.api.PaymentV2;
import br.com.phoebus.android.payments.api.ReversePayment;
import br.com.phoebus.android.payments.api.SettleRequestResponse;
import br.com.phoebus.payments.demo.utils.DataTypeUtils;

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

    public static void callResultIntent(PaymentV2 data, Context context, int activityFlags) {
        Intent intentResult = new Intent(context, ResultActivity.class);
        intentResult.putExtra(ResultActivity.CLIENT_RECEIPT, data.getReceipt().getClientVia());
        intentResult.putExtra(ResultActivity.MERCHANT_RECEIPT, data.getReceipt().getMerchantVia());

        HashMap<String, String> dataMap = new LinkedHashMap<String, String>();
        dataMap.put("Valor", DataTypeUtils.getMoneyAsString(data.getValue()));
        dataMap.put("Tipo de Pagamento", DataTypeUtils.getAsString(data.getPaymentType()));
        dataMap.put("Ident.do Pagamento", data.getPaymentId());
        dataMap.put("Ident. Adquirente", data.getAcquirerId());
        dataMap.put("Número de Aut.", data.getAcquirerAuthorizationNumber());
        dataMap.put("Adquirente", data.getAcquirer());
        dataMap.put("Data/hora Adquirente", DataTypeUtils.getAsString(data.getAcquirerResponseDate()));
        dataMap.put("Data/hora Terminal", DataTypeUtils.getAsString(data.getPaymentDate()));
        dataMap.put("Código de Resposta", data.getAcquirerResponseCode());
        dataMap.put("Abreviação do Produto", data.getProductShortName());
        if (data.getAdditionalValueType() != null) {
            dataMap.put("Tipo do Valor Adicional", data.getAdditionalValueType().name());
        }
        if (data.getAdditionalValue() != null) {
            dataMap.put("Valor Adicional", DataTypeUtils.getMoneyAsString(data.getValue()));
        }
        dataMap.put("Tipo da Conta", data.getAccountTypeId());
        dataMap.put("Plan ID", data.getPlanId());
        dataMap.put("Número do Lote", data.getBatchNumber());
        dataMap.put("NSU Terminal", data.getNsuTerminal());

        if (data.getCard() != null)
            dataMap.put("Cartão", data.getCard().getBin() + "..." + data.getCard().getPanLast4Digits() + " (" + data.getCard().getBrand() + ")");

        if(data.getCardToken() != null ){
            dataMap.put("Token do Cartão", data.getCardToken());
        }

        dataMap.put("Parcelas", DataTypeUtils.getAsString(data.getInstallments()));

        intentResult.putExtra(ResultActivity.RESPONSE_DATA, dataMap);

        if (activityFlags != 0)
            intentResult.setFlags(activityFlags);

        context.startActivity(intentResult);
    }

    public static void callResultIntent(Payment data, Context context, int activityFlags) {
        Intent intentResult = new Intent(context, ResultActivity.class);
        intentResult.putExtra(ResultActivity.CLIENT_RECEIPT, data.getReceipt().getClientVia());
        intentResult.putExtra(ResultActivity.MERCHANT_RECEIPT, data.getReceipt().getMerchantVia());

        HashMap<String, String> dataMap = new LinkedHashMap<String, String>();
        dataMap.put("Valor", DataTypeUtils.getMoneyAsString(data.getValue()));
        dataMap.put("Tipo de Pagamento", DataTypeUtils.getAsString(data.getPaymentType()));
        dataMap.put("Ident.do Pagamento", data.getPaymentId());
        dataMap.put("Ident. Adquirente", data.getAcquirerId());
        dataMap.put("Número de Aut.", data.getAcquirerAuthorizationNumber());
        dataMap.put("Adquirente", data.getAcquirer());
        dataMap.put("Data/hora Adquirente", DataTypeUtils.getAsString(data.getAcquirerResponseDate()));
        dataMap.put("Data/hora Terminal", DataTypeUtils.getAsString(data.getPaymentDate()));
        dataMap.put("Código de Resposta", data.getAcquirerResponseCode());
        dataMap.put("Forma de Captura", DataTypeUtils.getAsString(data.getCaptureType()));

        if (data.getCard() != null)
            dataMap.put("Cartão", data.getCard().getBin() + "..." + data.getCard().getPanLast4Digits() + " (" + data.getCard().getBrand() + ")");

        dataMap.put("Parcelas", DataTypeUtils.getAsString(data.getInstallments()));

        intentResult.putExtra(ResultActivity.RESPONSE_DATA, dataMap);

        if (activityFlags != 0)
            intentResult.setFlags(activityFlags);

        context.startActivity(intentResult);
    }

    public static void callResultIntent(ReversePayment data, Context context, int activityFlags) {
        Intent intentResult = new Intent(context, ResultActivity.class);
        intentResult.putExtra(ResultActivity.CLIENT_RECEIPT, data.getReceipt().getClientVia());
        intentResult.putExtra(ResultActivity.MERCHANT_RECEIPT, data.getReceipt().getMerchantVia());

        HashMap<String, String> dataMap = new HashMap<String, String>();
        dataMap.put("Ident.do Pagamento", data.getPaymentId());
        dataMap.put("Ident. para a Adquirente", data.getAcquirerId());
        dataMap.put("Número de Autorização", data.getAcquirerAuthorizationNumber());
        dataMap.put("Código de Resposta", data.getAcquirerResponseCode());
        dataMap.put("Data/hora Adquirente", DataTypeUtils.getAsString(data.getAcquirerResponseDate()));
        dataMap.put("Pode ser Desfeito", (data.getCancelable() ? "Sim" : "Não"));

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
