package br.com.phoebus.payments.demo.fragments;

import android.os.Bundle;
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
        if (mOnPaymentSelectedClickListener != null){
            mOnPaymentSelectedClickListener.onPaymentSelectedClick(paySelected);
        }
    }

}

