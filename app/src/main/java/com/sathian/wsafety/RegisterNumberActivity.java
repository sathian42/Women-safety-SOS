package com.sathian.wsafety;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import android.util.Log;

public class RegisterNumberActivity extends AppCompatActivity {

    TextInputEditText number1, number2, number3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_number);

        number1 = findViewById(R.id.number1Edit);
        number2 = findViewById(R.id.number2Edit);
        number3 = findViewById(R.id.number3Edit);
    }

    public void saveNumbers(View view) {
        String numberString1 = number1.getText() != null ? number1.getText().toString() : "";
        String numberString2 = number2.getText() != null ? number2.getText().toString() : "";
        String numberString3 = number3.getText() != null ? number3.getText().toString() : "";

        if (isValidPhoneNumber(numberString1) && isValidPhoneNumber(numberString2) && isValidPhoneNumber(numberString3)) {
            SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
            SharedPreferences.Editor myEdit = sharedPreferences.edit();
            myEdit.putString("ENUM1", numberString1);
            myEdit.putString("ENUM2", numberString2);
            myEdit.putString("ENUM3", numberString3);
            myEdit.apply();

            // Debugging: Log saved numbers
            Log.d("RegisterNumberActivity", "Numbers Saved: " + numberString1 + ", " + numberString2 + ", " + numberString3);

            // Close the activity after saving numbers
            RegisterNumberActivity.this.finish();
        } else {
            Toast.makeText(this, "Enter valid 10-digit numbers for all fields!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.length() == 10 && phoneNumber.matches("\\d+");
    }
}
