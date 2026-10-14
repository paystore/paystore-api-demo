package br.com.phoebus.payments.demo.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.phoebus.android.payments.api.provider.response.TransactionProviderResponse;
import br.com.phoebus.payments.demo.R;

public class TransactionListFragment<ProviderResponse> extends Fragment  implements TransactionExpandableListAdapter.OnPaymentListClickListener{

    private List<ProviderResponse> mTransactions;
    private RecyclerView mRecyclerView;
    private OnTransactionSelectedClickListener mOnTransactionSelectedClickListener;


    public TransactionListFragment(List<ProviderResponse> transactions, OnTransactionSelectedClickListener onTransactionSelectedClickListener) {
        mOnTransactionSelectedClickListener = onTransactionSelectedClickListener;
        mTransactions = transactions;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_transaction_list, container, false);
        TextView textView = view.findViewById(R.id.empty_transaction_list);

        if(!mTransactions.isEmpty()) {
            ExpandableListView expandableListView = view.findViewById(R.id.expand_result_transaction);
            expandableListView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);


            expandableListView.setAdapter(new TransactionExpandableListAdapter(getContext(), (List<br.com.phoebus.android.payments.api.provider.response.ProviderResponse>) mTransactions, this));
        }

        return view;

    }

    @Override
    public void onPaymentListClick(int position, int positionChild) {
        if (mOnTransactionSelectedClickListener != null){

            if(positionChild != -1){
                mOnTransactionSelectedClickListener.onTransactionReverseSelectedClick(((TransactionProviderResponse) mTransactions.get(position)).getReversals().get(positionChild));
            }else{
                mOnTransactionSelectedClickListener.onTransactionSelectedClick((TransactionProviderResponse) mTransactions.get(position));
            }
        }
    }
}

