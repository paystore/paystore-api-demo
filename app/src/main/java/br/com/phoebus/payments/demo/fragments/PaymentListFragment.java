package br.com.phoebus.payments.demo.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.phoebus.android.payments.api.Payment;
import br.com.phoebus.payments.demo.R;
import br.com.phoebus.payments.demo.utils.DividerItemDecoration;

public class PaymentListFragment extends Fragment implements PaymentAdapter.OnPaymentListClickListener {
    private List<Payment> paymentList;
    private RecyclerView mRecyclerView;
    private OnPaymentSelectedClickListener mOnPaymentSelectedClickListener;

    public static PaymentListFragment newInstance(@NonNull List<Payment> paymentList, OnPaymentSelectedClickListener onPaymentSelectedClickListener) {
        PaymentListFragment fragment = new PaymentListFragment();
        fragment.paymentList = paymentList;
        fragment.mOnPaymentSelectedClickListener = onPaymentSelectedClickListener;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.paymentsList_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment_list, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_payments_list);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new PaymentAdapter(paymentList, this));

        return view;
    }

    @Override
    public void onPaymentListClick(int position) {
        Payment paySelected = paymentList.get(position);
        log("acquirer", paySelected.getAcquirer());
        log("paymentId", paySelected.getPaymentId());
        log("card bin", paySelected.getCard().getBin());
        log("card pan last 4 digits", paySelected.getCard().getPanLast4Digits());
        log("capture type", paySelected.getCaptureType().name());
        log("payment status", paySelected.getPaymentStatus().name());
        log("payment date", paySelected.getPaymentDate().toString());
        log("acquirer Id", paySelected.getAcquirerId());
        log("acquirer response code", paySelected.getAcquirerResponseCode());
        log("acquirer response date", paySelected.getAcquirerResponseDate().toString());
        log("acquirer auth number", paySelected.getAcquirerAuthorizationNumber());
        log("merchant receipt", paySelected.getReceipt() == null ? "" : paySelected.getReceipt().getMerchantVia());
        log("client receipt", paySelected.getReceipt() == null ? "" : paySelected.getReceipt().getClientVia());
        log("additional value", paySelected.getAdditionalValue() == null ? "" : paySelected.getAdditionalValue().toString());
        log("account type", paySelected.getAccountTypeId());
        log("plan id", paySelected.getPlanId());
        log("product short name", paySelected.getProductShortName());
        log("batch number", paySelected.getBatchNumber());
        log("nsu terminal", paySelected.getNsuTerminal());
        log("é última transação?", paySelected.isLastTrx() ? getString(R.string.yes) : getString(R.string.no));
        log("ticket number", paySelected.getTicketNumber());
        log("card holder name", paySelected.getCardHolderName());
        log("terminal id", paySelected.getTerminalId());
        if (mOnPaymentSelectedClickListener != null){
            mOnPaymentSelectedClickListener.onPaymentSelectedClick(paySelected);
        }
    }

    private void log(String tag, String value) {
        Log.i("payment consult result " + tag, TextUtils.isEmpty(value) ? " " : value);
    }

}

