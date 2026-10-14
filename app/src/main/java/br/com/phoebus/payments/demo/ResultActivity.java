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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import br.com.phoebus.android.payments.api.OpenBatchResponse;
import br.com.phoebus.android.payments.api.Payment;
import br.com.phoebus.android.payments.api.PaymentV2;
import br.com.phoebus.android.payments.api.Refund;
import br.com.phoebus.android.payments.api.ReversePayment;
import br.com.phoebus.android.payments.api.ReversePaymentV2;
import br.com.phoebus.android.payments.api.SettleRequestResponse;
import br.com.phoebus.android.payments.api.SettleRequestResponseV2;
import br.com.phoebus.android.payments.api.provider.response.ProviderResponse;
import br.com.phoebus.android.payments.api.provider.response.RefundProviderResponse;
import br.com.phoebus.android.payments.api.provider.response.TransactionProviderResponse;
import br.com.phoebus.payments.demo.utils.DataTypeUtils;

public class ResultActivity extends AppCompatActivity {

    public static final String CLIENT_RECEIPT = "clientReceipt";
    public static final String MERCHANT_RECEIPT = "merchantReceipt";
    public static final String RESPONSE_DATA = "responseData";
    public static final String RESPONSE_DATA_ORDER = "responseDataOrder";
    public static final String SHOW_BUTTON_CONFIRM = "showButtonConfirm";

    private static final Object lock = new Object();

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

        ArrayList<String> order = getIntent().getStringArrayListExtra(RESPONSE_DATA_ORDER);

        createResponseDataView(data, order != null ? order : new ArrayList<>(data.keySet()), dataTable);

    }

    private void createResponseDataView(HashMap<String, String> data, List<String> keys, TableLayout dataTable) {

        boolean white = false;

        for (String key : keys) {
            TableRow tr = (TableRow) this.getLayoutInflater().inflate(R.layout.result_response_data_row, null);

            if (white = !white)
                tr.setBackgroundColor(Color.WHITE);

            TextView tv = (TextView) tr.findViewById(R.id.title_response_data);
            tv.setPaintFlags(tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tv.setText(key);

            tv = (TextView) tr.findViewById(R.id.value_response_data);
            tv.setText(data.get(key));

            if (key.equals(getApplicationContext().getString(R.string.result_activity_payment_id))) {
                setClipboard(tv);
            }

            dataTable.addView(tr);
        }
    }

    public static void callResultIntent(PaymentV2 data, Context context, int activityFlags, Map<String, String> options, boolean isConsulting) {
        Intent intentResult = new Intent(context, ResultActivity.class);
        intentResult.putExtra(ResultActivity.CLIENT_RECEIPT, data.getReceipt().getClientVia());
        intentResult.putExtra(ResultActivity.MERCHANT_RECEIPT, data.getReceipt().getMerchantVia());

        HashMap<String, String> dataMap = new LinkedHashMap<String, String>();
        dataMap.put(context.getString(R.string.result_activity_payment_type), DataTypeUtils.getAsString(data.getPaymentType()));
        dataMap.put(context.getString(R.string.result_activity_payment_id), data.getPaymentId());
        dataMap.put(context.getString(R.string.result_activity_status), DataTypeUtils.getAsString(data.getPaymentStatus()));
        dataMap.put(context.getString(R.string.appTransactionId), data.getAppTransactionId());
        dataMap.put(context.getString(R.string.result_activity_acquirer_id), data.getAcquirerId());
        dataMap.put(context.getString(R.string.result_activity_auth), data.getAcquirerAuthorizationNumber());
        dataMap.put(context.getString(R.string.result_activity_acquirer), data.getAcquirer());
        dataMap.put(context.getString(R.string.result_activity_currency_code), data.getCurrencyCode());
        /* Linha de código para exibir o nome da adiquirente (versão aberta do aar do sdk 4.0.0.3)*/
        dataMap.put(context.getString(R.string.result_activity_acquirer_name), data.getAcquirerName());
        dataMap.put(context.getString(R.string.result_activity_acquirer_datetime), DataTypeUtils.getAsString(data.getAcquirerResponseDate()));
        dataMap.put(context.getString(R.string.result_activity_terminal_datetime), DataTypeUtils.getAsString(data.getPaymentDate()));
        dataMap.put(context.getString(R.string.result_activity_resp_code), data.getAcquirerResponseCode());
        dataMap.put(context.getString(R.string.result_activity_product_short_name), data.getProductShortName());
        dataMap.put(context.getString(R.string.result_activity_ticket_number), data.getTicketNumber() != null ? data.getTicketNumber().toString() : "");
        if (data.getAdditionalValueType() != null) {
            dataMap.put(context.getString(R.string.valueTypeHint), data.getAdditionalValueType().name());
        }
        if (data.getAdditionalValue() != null) {
            dataMap.put(context.getString(R.string.result_activity_additional_value), DataTypeUtils.getMoneyAsString(data.getAdditionalValue()));
        }
        if (data.getAccountTypeId() != null) {
            dataMap.put(context.getString(R.string.result_activity_additional_account_type), data.getAccountTypeId());
        }
        if (data.getPlanId() != null) {
            dataMap.put(context.getString(R.string.result_activity_additional_plan_id), data.getPlanId());
        }

        dataMap.put(context.getString(R.string.result_activity_settlement_id), data.getBatchNumber());
        dataMap.put(context.getString(R.string.result_activity_terminal_nsu), data.getNsuTerminal());
        dataMap.put(context.getString(R.string.result_activity_acquirer_additional_msg), data.getAcquirerAdditionalMessage());
        dataMap.put(context.getString(R.string.result_activity_capture_type), DataTypeUtils.getAsString(data.getCaptureType()));
        dataMap.put(context.getString(R.string.result_activity_acquirer_nsu), data.getAcquirerNsu());
        dataMap.put(context.getString(R.string.result_activity_terminal_id), data.getTerminalId());
        dataMap.put(context.getString(R.string.result_activity_payment_date_utc), data.getPaymentDateUtc());

        if (data.getCorrelationId() != null) {
            dataMap.put(context.getString(R.string.result_activity_correlation_id), data.getCorrelationId());
        }
        if (data.getCurrencyCode() != null) {
            dataMap.put(context.getString(R.string.result_activity_currency_code), data.getCurrencyCode());
        }
        if (data.getAcquirerResponseDateUTC() != null) {
            dataMap.put(context.getString(R.string.result_activity_acquirer_response_date_utc), data.getAcquirerResponseDateUTC());
        }
        if (data.getAdditionalData() != null) {
            dataMap.put(context.getString(R.string.result_activity_additional_data), data.getAdditionalData());
        }
        if (data.getPhastPayResponse() != null) {
            putPhastPayData(dataMap, context, data.getPhastPayResponse(), data.getAppTransactionId());
        }

        if (isConsulting) dataMap.put("lastTrx", String.valueOf(data.isLastTrx()));

        // Em transações PhastPay o Payments envia o cartão com "null"/vazio
        if (data.getCard() != null) {
            if (hasValue(data.getCard().getBin())) {
                dataMap.put(context.getString(R.string.result_activity_card), data.getCard().getBin().replaceAll("[^ ]", "*")+ "..." + data.getCard().getPanLast4Digits() + " (" + data.getCard().getBrand() + ")");
                dataMap.put(context.getString(R.string.result_activity_holder_name), data.getCardHolderName());
            }
            if (hasValue(data.getCard().getCardAppName())) {
                dataMap.put(context.getString(R.string.result_activity_card_app_name), data.getCard().getCardAppName());
            }
            if (hasValue(data.getCard().getAid())) {
                dataMap.put(context.getString(R.string.result_activity_card_aid), data.getCard().getAid());
            }
        }

        if (data.getCardToken() != null) {
            dataMap.put(context.getString(R.string.result_activity_card_token), data.getCardToken());
        }

        dataMap.put(context.getString(R.string.result_activity_installments), DataTypeUtils.getAsString(data.getInstallments()));
        dataMap.put(context.getString(R.string.result_activity_dni), data.getDni() != null ? data.getDni() : "");
        dataMap.put(context.getString(R.string.result_activity_notes), data.getNote() != null ? data.getNote() : "");
        dataMap.put(context.getString(R.string.qrId), data.getQrId());
        BigDecimal originalValue = data.getOriginalValue();
        if (originalValue != null && originalValue.compareTo(BigDecimal.ZERO) > 0) {
            dataMap.put(context.getString(R.string.originalValue), DataTypeUtils.getMoneyAsString(originalValue));
            BigDecimal discountedValue = originalValue.subtract(data.getValue());
            dataMap.put(context.getString(R.string.discountedValue), DataTypeUtils.getMoneyAsString(discountedValue));
            dataMap.put(context.getString(R.string.total), DataTypeUtils.getMoneyAsString(data.getValue()));
        } else {
            dataMap.put(context.getString(R.string.result_activity_value), DataTypeUtils.getMoneyAsString(data.getValue()));
        }

        putResponseData(intentResult, dataMap);
        if (options != null) {
            intentResult.putExtra(ResultActivity.SHOW_BUTTON_CONFIRM, options.get(SHOW_BUTTON_CONFIRM) == "T");
        }

        if (activityFlags != 0)
            intentResult.setFlags(activityFlags);
        synchronized (lock) {
            context.startActivity(intentResult);
        }
    }

    // Transações PhastPay: desmembra o JSON devolvido pelo PhastPay e mantém o bruto ao final
    private static void putPhastPayData(HashMap<String, String> dataMap, Context context, String phastPayResponse, String appTransactionId) {
        try {
            JsonObject json = new JsonParser().parse(phastPayResponse).getAsJsonObject();
            String appClientId = jsonString(json, "app_client_id");

            dataMap.put(context.getString(R.string.result_activity_phastpay_service), jsonString(json, "service"));
            dataMap.put(context.getString(R.string.result_activity_phastpay_status), jsonString(json, "status"));
            dataMap.put(context.getString(R.string.result_activity_phastpay_payment_id), jsonString(json, "payment_id"));
            dataMap.put(context.getString(R.string.result_activity_phastpay_app_client_id), appClientId);
            dataMap.put(context.getString(R.string.result_activity_phastpay_app_client_id_match),
                    context.getString(appClientId != null && appClientId.equals(appTransactionId) ? R.string.yes : R.string.no));
            dataMap.put(context.getString(R.string.result_activity_phastpay_value), jsonString(json, "value"));
            dataMap.put(context.getString(R.string.result_activity_phastpay_currency), jsonString(json, "currency"));
            dataMap.put(context.getString(R.string.result_activity_phastpay_datetime), jsonString(json, "date_time"));
            if (json.has("refunds") && json.get("refunds").isJsonArray()) {
                dataMap.put(context.getString(R.string.result_activity_phastpay_refunds), describeRefunds(json.getAsJsonArray("refunds")));
            }
        } catch (RuntimeException e) {
            // Resposta fora do formato esperado: exibe apenas o JSON bruto
        }
        dataMap.put(context.getString(R.string.result_activity_phast_pay_response), phastPayResponse);
    }

    // O LinkedHashMap volta da Intent como HashMap (sem ordem): a ordem das chaves segue em extra separado
    private static void putResponseData(Intent intent, HashMap<String, String> dataMap) {
        intent.putExtra(RESPONSE_DATA, dataMap);
        intent.putStringArrayListExtra(RESPONSE_DATA_ORDER, new ArrayList<>(dataMap.keySet()));
    }

    private static boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty() && !"null".equals(value);
    }

    private static String jsonString(JsonObject json, String key) {
        JsonElement element = json.get(key);

        if (element == null || element.isJsonNull()) return null;

        return element.isJsonPrimitive() ? element.getAsString() : element.toString();
    }

    private static String describeRefunds(JsonArray refunds) {
        StringBuilder result = new StringBuilder(String.valueOf(refunds.size()));

        for (JsonElement refund : refunds) {
            result.append("\n");
            if (refund.isJsonObject()) {
                JsonObject item = refund.getAsJsonObject();
                result.append(jsonString(item, "refund_id"))
                        .append(" (").append(jsonString(item, "status"))
                        .append(", ").append(jsonString(item, "value")).append(")");
            } else {
                result.append(refund.toString());
            }
        }

        return result.toString();
    }

    private void setClipboard(TextView textView) {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText(getString(R.string.result_activity_payment_id), textView.getText().toString());
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getApplicationContext(), getString(R.string.result_activity_copied_payment_id), Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void callResultIntent(ProviderResponse data, Context context, int activityFlags, Map<String, String> options) {
        PaymentV2 paymentV2 = data.isRefund() ? ((RefundProviderResponse) data).getRefund() : ((TransactionProviderResponse) data).getPayment();

        callResultIntent(paymentV2, context, activityFlags, options, true);
    }

    public static void callResultIntent(PaymentV2 data, Context context, int activityFlags, Map<String, String> options) {
        callResultIntent(data, context, activityFlags, options, false);
    }

    public static void callResultIntent(ReversePaymentV2 reversePaymentV2, Context context, int activityFlags, Map<String, String> options) {
        Intent intentResult = new Intent(context, ResultActivity.class);
        intentResult.putExtra(ResultActivity.CLIENT_RECEIPT, reversePaymentV2.getReceipt().getClientVia());
        intentResult.putExtra(ResultActivity.MERCHANT_RECEIPT, reversePaymentV2.getReceipt().getMerchantVia());

        HashMap<String, String> dataMap = new LinkedHashMap<String, String>();
        dataMap.put(context.getString(R.string.result_activity_value), DataTypeUtils.getMoneyAsString(reversePaymentV2.getValue()));
//        dataMap.put(context.getString(R.string.result_activity_payment_type), DataTypeUtils.getAsString(reversePaymentV2.getPaymentType()));
        dataMap.put(context.getString(R.string.result_activity_payment_id), reversePaymentV2.getPaymentId());
        dataMap.put(context.getString(R.string.result_activity_acquirer_id), reversePaymentV2.getAcquirerId());
        dataMap.put(context.getString(R.string.result_activity_auth), reversePaymentV2.getAcquirerAuthorizationNumber());
        dataMap.put(context.getString(R.string.result_activity_acquirer), reversePaymentV2.getAcquirerId());
        dataMap.put(context.getString(R.string.result_activity_acquirer_datetime), DataTypeUtils.getAsString(reversePaymentV2.getAcquirerResponseDate()));
        dataMap.put(context.getString(R.string.result_activity_terminal_datetime), DataTypeUtils.getAsString(reversePaymentV2.getPaymentDate()));
        dataMap.put(context.getString(R.string.result_activity_resp_code), reversePaymentV2.getAcquirerResponseCode());
        dataMap.put(context.getString(R.string.result_activity_capture_type), DataTypeUtils.getAsString(reversePaymentV2.getCaptureType()));
        if (reversePaymentV2.getAcquirerResponseDateUTC() != null) {
            dataMap.put(context.getString(R.string.result_activity_acquirer_response_date_utc), reversePaymentV2.getAcquirerResponseDateUTC());
        }
        if (reversePaymentV2.getCorrelationId() != null) {
            dataMap.put(context.getString(R.string.result_activity_correlation_id), reversePaymentV2.getCorrelationId());
        }
        dataMap.put(context.getString(R.string.qrId), reversePaymentV2.getQrId());
        dataMap.put(context.getString(R.string.appTransactionId), reversePaymentV2.getAppTransactionId());
        dataMap.put(context.getString(R.string.result_activity_status), DataTypeUtils.getAsString(reversePaymentV2.getStatus()));
        dataMap.put(context.getString(R.string.result_activity_settlement_id), reversePaymentV2.getBatchNumber());
        dataMap.put(context.getString(R.string.result_activity_terminal_nsu), reversePaymentV2.getNsuTerminal());
        dataMap.put(context.getString(R.string.result_activity_ticket_number), reversePaymentV2.getTicketNumber() != null ? reversePaymentV2.getTicketNumber().toString() : "");
        dataMap.put(context.getString(R.string.result_activity_terminal_id), reversePaymentV2.getTerminalId());
        dataMap.put(context.getString(R.string.result_activity_acquirer_additional_msg), reversePaymentV2.getAcquirerAdditionalMessage());
        if (reversePaymentV2.getAdditionalValueType() != null) {
            dataMap.put(context.getString(R.string.valueTypeHint), reversePaymentV2.getAdditionalValueType().name());
        }
        if (reversePaymentV2.getAdditionalValue() != null) {
            dataMap.put(context.getString(R.string.result_activity_additional_value), DataTypeUtils.getMoneyAsString(reversePaymentV2.getAdditionalValue()));
        }
        dataMap.put(context.getString(R.string.result_activity_devolution), context.getString(reversePaymentV2.isDevolution() ? R.string.yes : R.string.no));
        if (reversePaymentV2.getErrorData() != null && reversePaymentV2.getErrorData().getErrorMessage() != null) {
            dataMap.put(context.getString(R.string.result_activity_error_message), reversePaymentV2.getErrorData().getErrorMessage());
        }
        // Estorno PhastPay não tem cartão; o SDK não expõe a bandeira no estorno
        if (hasValue(reversePaymentV2.getCardBin())) {
            dataMap.put(context.getString(R.string.result_activity_card), reversePaymentV2.getCardBin().replaceAll("[^ ]", "*") + "..." + reversePaymentV2.getPanLast4Digits());
            dataMap.put(context.getString(R.string.result_activity_holder_name), reversePaymentV2.getCardHolderName());
        }
        dataMap.put("Cancelable", String.valueOf(reversePaymentV2.getCancelable()));
        dataMap.put("lastTrx", String.valueOf(reversePaymentV2.isLastTrx()));

        putResponseData(intentResult, dataMap);
        if (options != null) {
            intentResult.putExtra(ResultActivity.SHOW_BUTTON_CONFIRM, options.get(SHOW_BUTTON_CONFIRM) == "T");
        }

        if (activityFlags != 0)
            intentResult.setFlags(activityFlags);
        synchronized (lock) {
            context.startActivity(intentResult);
        }
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
        //dataMap.put(context.getString(R.string.qrId),data.getQrId());
       // dataMap.put(context.getString(R.string.appTransactionId), data.getAppTransactionId());

        if (data.getCard() != null) {
            dataMap.put(context.getString(R.string.cardAppName), data.getCard().getCardAppName());
            dataMap.put(context.getString(R.string.bin), data.getCard().getBin());
            dataMap.put(context.getString(R.string.panLast4Digits), data.getCard().getPanLast4Digits());
            dataMap.put(context.getString(R.string.aid), data.getCard().getAid());
            dataMap.put(context.getString(R.string.result_activity_card), data.getCard().getBin().replaceAll("[^ ]", "*") + "..." + data.getCard().getPanLast4Digits() + " (" + data.getCard().getBrand() + ")");
           // dataMap.put(context.getString(R.string.result_activity_holder_name), data.getCardHolderName());
        }

        dataMap.put(context.getString(R.string.result_activity_installments), DataTypeUtils.getAsString(data.getInstallments()));


        putResponseData(intentResult, dataMap);
        if (options != null) {
            intentResult.putExtra(ResultActivity.SHOW_BUTTON_CONFIRM, options.get(SHOW_BUTTON_CONFIRM) == "T");
        }

        if (activityFlags != 0)
            intentResult.setFlags(activityFlags);

        synchronized (lock) {
            context.startActivity(intentResult);
        }
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
        dataMap.put(context.getString(R.string.result_activity_cancelable), (data.getCancelable() ? context.getString(R.string.yes) : context.getString(R.string.no)));
        dataMap.put(context.getString(R.string.result_activity_acquirer_additional_msg), data.getAcquirerAdditionalMessage());
        dataMap.put(context.getString(R.string.result_activity_ticket_number), String.valueOf(data.getTicketNumber()));
        dataMap.put(context.getString(R.string.result_activity_settlement_id), data.getBatchNumber());
        dataMap.put(context.getString(R.string.result_activity_terminal_nsu), data.getNsuTerminal());
        dataMap.put(context.getString(R.string.result_activity_holder_name), data.getCardHolderName());
        dataMap.put(context.getString(R.string.result_activity_card), data.getCardBin().replaceAll("[^ ]", "*") + "..." + data.getPanLast4Digits());
        dataMap.put(context.getString(R.string.result_activity_terminal_id), data.getTerminalId());
        dataMap.put(context.getString(R.string.result_activity_status), data.getStatus().name());
        dataMap.put(context.getString(R.string.qrId), data.getQrId());
        if (data.getAcquirerResponseDateUTC() != null) {
            dataMap.put(context.getString(R.string.result_activity_acquirer_response_date_utc), data.getAcquirerResponseDateUTC());
        }
        if (data.getCorrelationId() != null) {
            dataMap.put(context.getString(R.string.result_activity_correlation_id), data.getCorrelationId());
        }

        putResponseData(intentResult, dataMap);
        if (activityFlags != 0)
            intentResult.setFlags(activityFlags);

        synchronized (lock) {
            context.startActivity(intentResult);
        }
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

        putResponseData(intentResult, dataMap);
        if (activityFlags != 0)
            intentResult.setFlags(activityFlags);

        synchronized (lock) {
            context.startActivity(intentResult);
        }
    }

    public static void callResultIntent(SettleRequestResponse data, Context context, int activityFlags) {
        Intent settlementResultIt = new Intent(context, ResultActivity.class);
        settlementResultIt.putExtra(SettlementResultActivity.MERCHANT_RECEIPT, data.getMerchantVia());

        HashMap<String, String> dataMap = new LinkedHashMap<String, String>();
        dataMap.put(context.getString(R.string.settlement_batch_number), data.getBatchNumber());
        dataMap.put(context.getString(R.string.settlement_acquirer_response_code), data.getAcquirerResponseCode());
        dataMap.put(context.getString(R.string.settlement_terminal_id), data.getTerminalId());
        dataMap.put(context.getString(R.string.settlement_message), data.getAcquirerAdditionalMessage());
        //dataMap.put(context.getString(R.string.settlement_date), new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(data.getBatchClosureDate()));

        putResponseData(settlementResultIt, dataMap);

        if (activityFlags != 0) {
            settlementResultIt.setFlags(activityFlags);
        }

        synchronized (lock) {
            context.startActivity(settlementResultIt);
        }
    }

    public static void callResultIntent(OpenBatchResponse data, Context context, int activityFlags) {
        Intent settlementResultIt = new Intent(context, ResultActivity.class);

        HashMap<String, String> dataMap = new LinkedHashMap<String, String>();
        dataMap.put(context.getString(R.string.new_batch_open), data.getBatchNumber());

        putResponseData(settlementResultIt, dataMap);

        if (activityFlags != 0) {
            settlementResultIt.setFlags(activityFlags);
        }

        synchronized (lock) {
            context.startActivity(settlementResultIt);
        }
    }

}
