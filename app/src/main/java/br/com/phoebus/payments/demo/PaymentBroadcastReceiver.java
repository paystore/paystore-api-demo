package br.com.phoebus.payments.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.text.SimpleDateFormat;

import br.com.phoebus.android.payments.api.ErrorBroadcastResponse;
import br.com.phoebus.android.payments.api.PaymentV2;
import br.com.phoebus.android.payments.api.ReversePayment;
import br.com.phoebus.android.payments.api.SettlementBroadcastResponseV2;
import br.com.phoebus.android.payments.api.utils.DataUtils;
import br.com.phoebus.android.payments.api.utils.Intents;
import br.com.phoebus.payments.demo.utils.DataTypeUtils;
import br.com.phoebus.payments.demo.utils.Helper;

public class PaymentBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intents.action.ACTION_AFTER_PAYMENT)) {
            PaymentV2 payment = PaymentV2.fromBundle(intent.getBundleExtra(Intents.extra.EXTRA_PAYMENT_RETURN_V2));

            Helper.writeLogCat(this, "onReceive", "Ident.do Pagamento ".concat(payment.getPaymentId()));
            Helper.writeLogCat(this, "onReceive", "Ident. para a Adquirente ".concat(payment.getAcquirerId()));
            Helper.writeLogCat(this, "onReceive", "Número de Autorização ".concat(payment.getAcquirerAuthorizationNumber()));
            Helper.writeLogCat(this, "onReceive", "Código de Resposta ".concat(payment.getAcquirerResponseCode()));
            Helper.writeLogCat(this, "onReceive", "Data/hora Adquirente ".concat(DataTypeUtils.getAsString(payment.getAcquirerResponseDate())));

            //Eh possivel chamar uma activity aqui
//            ResultActivity.callResultIntent(payment, context, Intent.FLAG_ACTIVITY_NEW_TASK);
        } else if (intent.getAction().equals(Intents.action.ACTION_AFTER_REVERSAL)) {
            ReversePayment reversePayment = DataUtils.fromBundle(ReversePayment.class, intent.getExtras(), Intents.extra.EXTRA_PAYMENT_RETURN);

            Helper.writeLogCat(this, "onReceive", "Ident.do Pagamento ".concat(reversePayment.getPaymentId()));
            if (reversePayment.getAcquirerId() != null) {
                Helper.writeLogCat(this, "onReceive", "Ident. para a Adquirente ".concat(reversePayment.getAcquirerId()));
            }
            if (reversePayment.getAcquirerAuthorizationNumber() != null) {
                Helper.writeLogCat(this, "onReceive", "Número de Autorização ".concat(reversePayment.getAcquirerAuthorizationNumber()));
            }
            Helper.writeLogCat(this, "onReceive", "Código de Resposta ".concat(reversePayment.getAcquirerResponseCode() != null ? reversePayment.getAcquirerResponseCode() : ""));
            Helper.writeLogCat(this, "onReceive", "Data/hora Adquirente ".concat(DataTypeUtils.getAsString(reversePayment.getAcquirerResponseDate())));
            Helper.writeLogCat(this, "onReceive", "Pode ser Desfeito ".concat((reversePayment.getCancelable() ? "Sim" : "Não")));
            Helper.writeLogCat(this, "onReceive", "Status ".concat(reversePayment.getStatus().name()));

        } else if (intent.getAction().equals(Intents.action.ACTION_AFTER_SETTLEMENT)) {
            SettlementBroadcastResponseV2 settlementBroadcastResponseV2 = SettlementBroadcastResponseV2.fromBundle(intent.getBundleExtra(Intents.extra.EXTRA_SETTLEMENT_RETURN));

            if (settlementBroadcastResponseV2.getSucceeded() != null) {
                Helper.writeLogCat(this, "onReceive", "Lote cerrado con éxito: ".concat(String.valueOf(settlementBroadcastResponseV2.getSucceeded())));
            }

            if (settlementBroadcastResponseV2.getResponseV2() != null) {

                if (settlementBroadcastResponseV2.getResponseV2().getMerchantVia() != null) {
                    Helper.writeLogCat(this, "onReceive", "Recibo: \n".concat(settlementBroadcastResponseV2.getResponseV2().getMerchantVia()));
                }

                if (settlementBroadcastResponseV2.getResponseV2().getBatchNumber() != null) {
                    Helper.writeLogCat(this, "onReceive", "Número de lote: ".concat(settlementBroadcastResponseV2.getResponseV2().getBatchNumber()));
                }

                if (settlementBroadcastResponseV2.getResponseV2().getAcquirerResponseCode() != null) {
                    Helper.writeLogCat(this, "onReceive", "Código de respuesta: ".concat(settlementBroadcastResponseV2.getResponseV2().getAcquirerResponseCode()));
                }

                if (settlementBroadcastResponseV2.getResponseV2().getTerminalId() != null) {
                    Helper.writeLogCat(this, "onReceive", "ID del terminal: ".concat(settlementBroadcastResponseV2.getResponseV2().getTerminalId()));
                }

                if (settlementBroadcastResponseV2.getResponseV2().getAcquirerAdditionalMessage() != null) {
                    Helper.writeLogCat(this, "onReceive", "Mensaje adicional: ".concat(settlementBroadcastResponseV2.getResponseV2().getAcquirerAdditionalMessage()));
                }

                if (settlementBroadcastResponseV2.getResponseV2().getBatchClosureDate() != null) {
                    Helper.writeLogCat(this, "onReceive", "Fecha y hora: ".concat(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(settlementBroadcastResponseV2.getResponseV2().getBatchClosureDate())));
                }

            } else if (settlementBroadcastResponseV2.getErrorData() != null) {

                if (settlementBroadcastResponseV2.getErrorData().getPaymentsResponseCode() != null) {
                    Helper.writeLogCat(this, "onReceive", "Código de respuesta de la aplicación de Pagos: ".concat(settlementBroadcastResponseV2.getErrorData().getPaymentsResponseCode()));
                }

                if (settlementBroadcastResponseV2.getErrorData().getAcquirerResponseCode() != null) {
                    Helper.writeLogCat(this, "onReceive", "Código de Respuesta del Adquirente: ".concat(settlementBroadcastResponseV2.getErrorData().getAcquirerResponseCode()));
                }

                if (settlementBroadcastResponseV2.getErrorData().getResponseMessage() != null) {
                    Helper.writeLogCat(this, "onReceive", "Mensaje de respuesta: ".concat(settlementBroadcastResponseV2.getErrorData().getResponseMessage()));
                }
            }
        } else if (intent.getAction().equals(Intents.action.ACTION_ERROR_REPORT) && Helper.readPrefsBoolean(context, Helper.BROADCAST_ERROR, Helper.PREF_CONFIG)) {
            ErrorBroadcastResponse errorBroadcastResponse = DataUtils.fromBundle(ErrorBroadcastResponse.class, intent.getExtras(), Intents.extra.EXTRA_ERROR_REPORT);
            Intent intentErrorBroadcast = new Intent(context, ErrorBroadcastActivity.class);
            intentErrorBroadcast.putExtra("error", errorBroadcastResponse.toString());
//            intentErrorBroadcast.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intentErrorBroadcast.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            context.startActivity(intentErrorBroadcast);

            if(errorBroadcastResponse.getPaymentsAppVersion() != null) {
                Helper.writeLogCat(this, "onReceive", "Payment App Version: ".concat(errorBroadcastResponse.getPaymentsAppVersion()));
            }

            if(errorBroadcastResponse.getSubAcquirerId() != null){
                Helper.writeLogCat(this, "onReceive", "SubAcquirer ID: ".concat(errorBroadcastResponse.getSubAcquirerId()));
            }

            if(errorBroadcastResponse.getMerchantPaystore() != null) {
                Helper.writeLogCat(this, "onReceive", "Merchant Paystore: ".concat(errorBroadcastResponse.getMerchantPaystore()));
            }

            if(errorBroadcastResponse.getTerminalID() != null){
                Helper.writeLogCat(this, "onReceive", "Terminal ID: ".concat(errorBroadcastResponse.getTerminalID()));
            }

            if(errorBroadcastResponse.getTimestamp() != null) {
                Helper.writeLogCat(this, "onReceive", "Error Timestamp: ".concat(String.valueOf(errorBroadcastResponse.getTimestamp())));
            }

            if(errorBroadcastResponse.getErrorType() != null) {
                Helper.writeLogCat(this, "onReceive", "Error Type: ".concat(String.valueOf(errorBroadcastResponse.getErrorType())));
            }

            if(errorBroadcastResponse.getErrorMessage() != null) {
                Helper.writeLogCat(this, "onReceive", "Error Message: ".concat(errorBroadcastResponse.getErrorMessage()));
            }

            if(errorBroadcastResponse.getErrorCode() != null) {
                Helper.writeLogCat(this, "onReceive", "Error Code: ".concat(errorBroadcastResponse.getErrorCode()));
            }

            if(errorBroadcastResponse.getStackTrace() != null) {
                Helper.writeLogCat(this, "onReceive", "StackTrace: ".concat(errorBroadcastResponse.getStackTrace()));
            }

            if(errorBroadcastResponse.getbCReturnCode() != null) {
                Helper.writeLogCat(this, "onReceive", "bC Return Code: ".concat(errorBroadcastResponse.getbCReturnCode()));
            }

            if(errorBroadcastResponse.getBin() != null) {
                Helper.writeLogCat(this, "onReceive", "BIN: ".concat(errorBroadcastResponse.getBin()));
            }

            if(errorBroadcastResponse.getAid() != null) {
                Helper.writeLogCat(this, "onReceive", "AID: ".concat(errorBroadcastResponse.getAid()));
            }

            if(errorBroadcastResponse.getValue() != null) {
                Helper.writeLogCat(this, "onReceive", "Value: ".concat(String.valueOf(errorBroadcastResponse.getValue())));
            }

            if(errorBroadcastResponse.getInstallments() != null) {
                Helper.writeLogCat(this, "onReceive", "Installments: ".concat(String.valueOf(errorBroadcastResponse.getInstallments())));
            }

            if(errorBroadcastResponse.getCaptureType() != null) {
                Helper.writeLogCat(this, "onReceive", "Capture Type: ".concat(errorBroadcastResponse.getCaptureType().name()));
            }

            if(errorBroadcastResponse.getCardServiceCode() != null) {
                Helper.writeLogCat(this, "onReceive", "Card Service Code: ".concat(errorBroadcastResponse.getCardServiceCode()));
            }

            if(errorBroadcastResponse.getProductId() != null) {
                Helper.writeLogCat(this, "onReceive", "Product Id: ".concat(String.valueOf(errorBroadcastResponse.getProductId())));
            }

            if(errorBroadcastResponse.getPaymentType() != null) {
                Helper.writeLogCat(this, "onReceive", "Payment Type: ".concat(errorBroadcastResponse.getPaymentType().name()));
            }

            if(errorBroadcastResponse.getTicketNumber() != null) {
                Helper.writeLogCat(this, "onReceive", "Ticket Number: ".concat(errorBroadcastResponse.getTicketNumber()));
            }

            if(errorBroadcastResponse.getBatchNumber() != null) {
                Helper.writeLogCat(this, "onReceive", "Batch Number: ".concat(errorBroadcastResponse.getBatchNumber()));
            }

            if(errorBroadcastResponse.getTransactionUuid() != null) {
                Helper.writeLogCat(this, "onReceive", "Transaction Uuid: ".concat(errorBroadcastResponse.getTransactionUuid()));
            }

            if(errorBroadcastResponse.getEndpoint() != null) {
                Helper.writeLogCat(this, "onReceive", "Endpoint: ".concat(errorBroadcastResponse.getEndpoint()));
            }

            if(errorBroadcastResponse.getHttpStatusCode() != null) {
                Helper.writeLogCat(this, "onReceive", "Código de erro HTTP: ".concat(errorBroadcastResponse.getHttpStatusCode()));
            }

            if(errorBroadcastResponse.getBreadcrumbs() != null) {
                Helper.writeLogCat(this, "onReceive", "Breadcrumbs: ".concat(errorBroadcastResponse.getBreadcrumbs()));
            }

            if(errorBroadcastResponse.getSdkMethod() != null) {
                Helper.writeLogCat(this, "onReceive", "SDK Method: ".concat(errorBroadcastResponse.getSdkMethod()));
            }

            if(errorBroadcastResponse.getApplicationName() != null) {
                Helper.writeLogCat(this, "onReceive", "Application Name: ".concat(errorBroadcastResponse.getApplicationName()));
            }
        }
    }
}
