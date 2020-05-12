package br.com.phoebus.payments.demo.fragments;

import android.support.annotation.NonNull;
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


public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {

    private List<Payment> paymentList;
    private OnPaymentListClickListener mOnPaymentListClickListener;

    public PaymentAdapter(List<Payment> paymentList, OnPaymentListClickListener onPaymentListClickListener) {
        this.paymentList = paymentList;
        this.mOnPaymentListClickListener = onPaymentListClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mBrand;
        public TextView mStatus;
        public TextView mFourDigits;
        public TextView mValue;
        public TextView mDate;
        private OnPaymentListClickListener onPaymentListClickListener;

        public ViewHolder(@NonNull View v, OnPaymentListClickListener onPaymentListClickListener) {
            super(v);
            mBrand = (TextView) itemView.findViewById(R.id.brand);
            mStatus = (TextView) itemView.findViewById(R.id.status);
            mFourDigits = (TextView) itemView.findViewById(R.id.four_digits);
            mValue = (TextView) itemView.findViewById(R.id.value);
            mDate = (TextView) itemView.findViewById(R.id.date);
            this.onPaymentListClickListener = onPaymentListClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onPaymentListClickListener.onPaymentListClick(getAdapterPosition());
        }
    }


    @Override
    public PaymentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_payment, parent, false);
        return new PaymentAdapter.ViewHolder(v, mOnPaymentListClickListener);
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

    public interface OnPaymentListClickListener {
        void onPaymentListClick(int position);
    }

}