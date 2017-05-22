package br.com.phoebus.payments.demo.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import br.com.phoebus.android.payments.api.Payment;
import br.com.phoebus.payments.demo.R;
import br.com.phoebus.payments.demo.utils.DividerItemDecoration;

public class PaymentListFragment extends Fragment {
    private List<Payment> paymentList;
    private RecyclerView mRecyclerView;

    public static PaymentListFragment newInstance(@NonNull List<Payment> paymentList) {
        PaymentListFragment fragment = new PaymentListFragment();
        fragment.paymentList = paymentList;
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
        mRecyclerView.setAdapter(new PaymentAdapter(paymentList));

        return view;
    }

    public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {
        private List<Payment> paymentList;

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView mBrand;
            public TextView mStatus;
            public TextView mFourDigits;
            public TextView mValue;
            public TextView mDate;

            public ViewHolder(View v) {
                super(v);
                mBrand = (TextView) itemView.findViewById(R.id.brand);
                mStatus = (TextView) itemView.findViewById(R.id.status);
                mFourDigits = (TextView) itemView.findViewById(R.id.four_digits);
                mValue = (TextView) itemView.findViewById(R.id.value);
                mDate = (TextView) itemView.findViewById(R.id.date);
            }
        }

        public PaymentAdapter(List<Payment> paymentList) {
            this.paymentList = paymentList;
        }

        @Override
        public PaymentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_payment, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Payment payment = paymentList.get(position);
            holder.mBrand.setText(payment.getCard().getBrand());
            holder.mStatus.setText(payment.getPaymentStatus().name());
            holder.mFourDigits.setText(String.format("BIN **** %s", payment.getCard().getPanLast4Digits()));
            holder.mDate.setText(new SimpleDateFormat("dd/MM hh'h'mm").format(payment.getPaymentDate()));

            NumberFormat currFormat = NumberFormat.getCurrencyInstance();
            holder.mValue.setText(currFormat.format(payment.getValue().doubleValue()));
        }

        @Override
        public int getItemCount() {
            return (paymentList != null) ? paymentList.size() : 0;
        }
    }

}

