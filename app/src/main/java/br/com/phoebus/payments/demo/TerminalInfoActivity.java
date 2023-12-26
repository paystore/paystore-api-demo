package br.com.phoebus.payments.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import br.com.phoebus.android.payments.api.provider.TerminalInfoContract;
import br.com.phoebus.payments.demo.fragments.CursorUtils;

public class TerminalInfoActivity extends AppCompatActivity {

    TextView merchantId, merchantName, merchantCommercialName, terminalId, nationalId, postalCode, street, city, state, stateAbbreviation, country,
            complement, neighbourhood, addressNumber, merchantWebsite, merchantEmail, merchantPhone, merchantCategoryCode, merchantNationalType, subAcquirerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal_info);

        this.setTitle(R.string.terminal_info_string);

        merchantId = findViewById(R.id.merchantId);
        merchantName = findViewById(R.id.merchantName);
        merchantCommercialName = findViewById(R.id.merchantCommercialName);
        terminalId = findViewById(R.id.terminalId);
        nationalId = findViewById(R.id.nationalId);
        postalCode = findViewById(R.id.postalCode);
        street = findViewById(R.id.street);
        city = findViewById(R.id.city);
        state = findViewById(R.id.state);
        stateAbbreviation = findViewById(R.id.stateAbbreviation);
        country = findViewById(R.id.country);
        complement = findViewById(R.id.complement);
        neighbourhood = findViewById(R.id.neighbourhood);
        addressNumber = findViewById(R.id.addressNumber);
        merchantWebsite = findViewById(R.id.merchantWebsite);
        merchantEmail = findViewById(R.id.merchantEmail);
        merchantPhone = findViewById(R.id.merchantPhone);
        merchantCategoryCode = findViewById(R.id.merchantCategoryCode);
        merchantNationalType = findViewById(R.id.merchantNationalType);
        subAcquirerId = findViewById(R.id.subAcquirerId);

        setTerminalInformation();
    }

    private void setTerminalInformation() {
        Cursor terminalInformationCursor = getApplicationContext().getContentResolver().query(
                TerminalInfoContract.getUriBuilder().build(),
                null, "", null, "");

        if (!CursorUtils.isEmpty(terminalInformationCursor) && terminalInformationCursor.moveToFirst()) {
            merchantId.setText(CursorUtils.getString(terminalInformationCursor, TerminalInfoContract.column.MERCHANT_ID));
            merchantName.setText(CursorUtils.getString(terminalInformationCursor, TerminalInfoContract.column.MERCHANT_NAME));
            merchantCommercialName.setText(CursorUtils.getString(terminalInformationCursor, TerminalInfoContract.column.MERCHANT_COMMERCIAL_NAME));
            terminalId.setText(CursorUtils.getString(terminalInformationCursor, TerminalInfoContract.column.TERMINAL_ID));
            nationalId.setText(CursorUtils.getString(terminalInformationCursor, TerminalInfoContract.column.NATIONAL_ID));
            postalCode.setText(CursorUtils.getString(terminalInformationCursor, TerminalInfoContract.column.MC_POSTAL_CODE));
            street.setText(CursorUtils.getString(terminalInformationCursor, TerminalInfoContract.column.MC_STREET));
            city.setText(CursorUtils.getString(terminalInformationCursor, TerminalInfoContract.column.MC_CITY));
            state.setText(CursorUtils.getString(terminalInformationCursor, TerminalInfoContract.column.MC_STATE));
            stateAbbreviation.setText(CursorUtils.getString(terminalInformationCursor, TerminalInfoContract.column.MC_STATE_ABBREVIATION));
            country.setText(CursorUtils.getString(terminalInformationCursor, TerminalInfoContract.column.MC_COUNTRY));
            complement.setText(CursorUtils.getString(terminalInformationCursor, TerminalInfoContract.column.MC_COMPLEMENT));
            neighbourhood.setText(CursorUtils.getString(terminalInformationCursor, TerminalInfoContract.column.MC_NEIGHBOURHOOD));
            addressNumber.setText(CursorUtils.getString(terminalInformationCursor, TerminalInfoContract.column.MC_ADDRESS_NUMBER));
            merchantWebsite.setText(CursorUtils.getString(terminalInformationCursor, TerminalInfoContract.column.MC_WEB_SITE));
            merchantEmail.setText(CursorUtils.getString(terminalInformationCursor, TerminalInfoContract.column.MC_EMAIL));
            merchantPhone.setText(CursorUtils.getString(terminalInformationCursor, TerminalInfoContract.column.MC_PHONE));
            merchantCategoryCode.setText(CursorUtils.getString(terminalInformationCursor, TerminalInfoContract.column.MC_CATEGORY_CODE));
            merchantNationalType.setText(CursorUtils.getString(terminalInformationCursor, TerminalInfoContract.column.MC_NATIONAL_TYPE));
            subAcquirerId.setText(CursorUtils.getString(terminalInformationCursor, TerminalInfoContract.column.SUB_ACQUIRER_ID));
        }
    }

}