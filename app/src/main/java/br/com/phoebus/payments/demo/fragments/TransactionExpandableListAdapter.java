package br.com.phoebus.payments.demo.fragments;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import br.com.phoebus.android.payments.api.Payment;
import br.com.phoebus.android.payments.api.ReversePaymentV2;
import br.com.phoebus.android.payments.api.provider.response.ProviderResponse;
import br.com.phoebus.android.payments.api.provider.response.TransactionProviderResponse;
import br.com.phoebus.payments.demo.R;

public class TransactionExpandableListAdapter extends BaseExpandableListAdapter {

    private List<ProviderResponse> transactionList;
    private OnPaymentListClickListener mOnPaymentListClickListener;
    private Context context;

    public TextView mBrand;
    public TextView mStatus;
    public TextView mFourDigits;
    public TextView mValue;
    public TextView mDate;
    public TextView mLastTrx;
    public ImageView mShowReverse;
    public LinearLayout containerAdapter;
    public TextView mTypePayment;

    public TransactionExpandableListAdapter(Context context, List<ProviderResponse> transactionList, OnPaymentListClickListener onPaymentListClickListener) {
        this.transactionList = transactionList;
        this.mOnPaymentListClickListener = onPaymentListClickListener;
        this.context = context;
    }

    @Override
    public int getGroupCount() {
        return transactionList.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return transactionList.get(i).isRefund() ? 0 : ((TransactionProviderResponse) transactionList.get(i)).getReversals().size();
    }

    @Override
    public Object getGroup(int i) {
        return transactionList.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return transactionList.get(i).isRefund() ? null : ((TransactionProviderResponse) transactionList.get(i)).getReversals().get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.rv_payment, viewGroup, false);
        }

        mBrand = (TextView) view.findViewById(R.id.brand);
        mStatus = (TextView) view.findViewById(R.id.status);
        mFourDigits = (TextView) view.findViewById(R.id.four_digits);
        mValue = (TextView) view.findViewById(R.id.value);
        mDate = (TextView) view.findViewById(R.id.date);
        mLastTrx = (TextView) view.findViewById(R.id.last_trx);
        containerAdapter = view.findViewById(R.id.container_adapter);
        mShowReverse = view.findViewById(R.id.show_reverse);
        mTypePayment = view.findViewById(R.id.type_payment);

        setFields(groupPosition, viewShow -> {

            if (isExpanded) {
                ((ExpandableListView) viewGroup).collapseGroup(groupPosition);
            } else {
                ((ExpandableListView) viewGroup).expandGroup(groupPosition);
            }
        });

        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean isExpanded, View convertView, ViewGroup viewGroup) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.rv_reverse, viewGroup, false);
        }

        LinearLayout containerReverse = convertView.findViewById(R.id.container_reverse);

        ReversePaymentV2 reversePaymentV2 = (ReversePaymentV2) getChild(i, i1);

        TextView mStatus = (TextView) convertView.findViewById(R.id.status);
        TextView mFourDigits = (TextView) convertView.findViewById(R.id.four_digits);
        TextView mValue = (TextView) convertView.findViewById(R.id.value);
        TextView mDate = (TextView) convertView.findViewById(R.id.date);
        TextView mLastTrx = convertView.findViewById(R.id.last_trx);
        TextView mSDKCode = convertView.findViewById(R.id.sdk_code);
        TextView mSDKMsg = convertView.findViewById(R.id.sdk_msg);
        LinearLayout mSDKResponse = convertView.findViewById(R.id.sdk_response);
        TextView mType = (TextView) convertView.findViewById(R.id.type_reversal);

        mLastTrx.setText("lastTrx: " + reversePaymentV2.isLastTrx());
        if (reversePaymentV2.getErrorData().getPaymentsResponseCode() != null && !reversePaymentV2.getErrorData().getPaymentsResponseCode().isEmpty()
                && reversePaymentV2.getErrorData().getResponseMessage() != null && !reversePaymentV2.getErrorData().getResponseMessage().isEmpty()) {
            mSDKCode.setText("SDKCode: " + reversePaymentV2.getErrorData().getPaymentsResponseCode());
            mSDKMsg.setText("SDKMsg: " + reversePaymentV2.getErrorData().getResponseMessage());
            mSDKResponse.setVisibility(View.VISIBLE);
        }

        mType.setText(context.getString(R.string.title_void));
        mType.setVisibility(View.VISIBLE);

        mStatus.setText(reversePaymentV2.getStatus().name());
        mFourDigits.setText(String.format("BIN **** %s", reversePaymentV2.getPanLast4Digits()));
        mDate.setText(new SimpleDateFormat("dd/MM HH'h'mm").format(reversePaymentV2.getPaymentDate()));

        NumberFormat currFormat = NumberFormat.getCurrencyInstance();
        mValue.setText(currFormat.format(reversePaymentV2.getValue().doubleValue()));

        containerReverse.setOnClickListener(viewAdapter -> mOnPaymentListClickListener.onPaymentListClick(i, i1));

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    public interface OnPaymentListClickListener {
        void onPaymentListClick(int position, int positionChild);
    }

    private void setFields(int position, View.OnClickListener clickListener) {

        Boolean isRefund = transactionList.get(position).isRefund();

        Payment payment = ((TransactionProviderResponse) transactionList.get(position)).getPayment();

        if (payment == null && ((TransactionProviderResponse) transactionList.get(position)).getReversals().isEmpty()){
            containerAdapter.setVisibility(View.GONE);
        } else {
            containerAdapter.setVisibility(View.VISIBLE);
        }

        if (payment != null) {

            boolean isTrx = ((TransactionProviderResponse) transactionList.get(position)).getPayment().isLastTrx();

            if (!isRefund && !((TransactionProviderResponse) transactionList.get(position)).getReversals().isEmpty()) {
                mShowReverse.setVisibility(View.VISIBLE);
            } else {
                mShowReverse.setVisibility(View.GONE);
            }

            mBrand.setText(payment.getCard().getBrand());

            mStatus.setText(payment.getPaymentStatus().name());
            mFourDigits.setText(String.format("BIN **** %s", payment.getCard().getPanLast4Digits()));
            mDate.setText(new SimpleDateFormat("dd/MM HH'h'mm").format(payment.getPaymentDate()));
            if (isTrx) {
                mLastTrx.setText("lastTrx: " + isTrx);
                mLastTrx.setVisibility(View.VISIBLE);
            }

            NumberFormat currFormat = NumberFormat.getCurrencyInstance();
            mValue.setText(currFormat.format(payment.getValue().doubleValue()));

            mTypePayment.setText(context.getString(R.string.title_payment));
            mTypePayment.setVisibility(View.VISIBLE);

            containerAdapter.setOnClickListener(viewAdapter -> mOnPaymentListClickListener.onPaymentListClick(position, -1));
            mShowReverse.setOnClickListener(clickListener);
        }
    }

}
