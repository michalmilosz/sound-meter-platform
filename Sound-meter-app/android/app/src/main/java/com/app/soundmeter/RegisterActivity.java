package com.app.soundmeter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.soundmeter.api.UserAPI;
import com.app.soundmeter.configuration.DataHolder;
import com.app.soundmeter.configuration.RetrofitClient;
import com.app.soundmeter.dto.ResponseMessage;
import com.app.soundmeter.dto.UserCredentials;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

// Widok rejestracji
@Slf4j
public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.EditTextRegisterUsername)
    EditText editTextRegisterUsername;
    @BindView(R.id.EditTextRegisterPassword)
    EditText editTextRegisterPassword;
    @BindView(R.id.ButtonRegister)
    Button buttonRegister;

    private Retrofit retrofit = RetrofitClient.getRetrofit();
    private UserAPI userAPI = retrofit.create(UserAPI.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        buttonRegister.setOnClickListener(view -> {
            if(editTextRegisterUsername.getText().toString().isEmpty() || editTextRegisterPassword.getText().toString().isEmpty()){
                openEmptyFieldAlert(view);
            }
            else {
                register(
                        UserCredentials
                                .builder()
                                .login(editTextRegisterUsername.getText().toString())
                                .password(editTextRegisterPassword.getText().toString())
                                .build(),
                        view
                );
            }
        });
    }

    // rejestracja usera po HTTP
    public void register(UserCredentials userCredentials, final View v) {
        Call<ResponseMessage> login = userAPI.registerUser(userCredentials);
        login.enqueue(new Callback<ResponseMessage>() {
            @Override
            public void onResponse(Call<ResponseMessage> call, Response<ResponseMessage> response) {
                log.info("/users/register (POST), Response: " + response.code() + "/ message: " + response.message());
                log.info(String.valueOf(response.code()));
                if (response.code() == 201) {
                    openLoginActivity();
                } else {
                    openAlert(v);
                }
            }

            @Override
            public void onFailure(Call<ResponseMessage> call, Throwable t) {
                log.info("/users/register (POST), Response: " + t.getMessage());
            }
        });
    }

    // Alert ze taki user juz istnieje
    public void openAlert(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Taki użytkownik już istnieje!");
        alertDialogBuilder.setPositiveButton("OK",
                ((DialogInterface arg0, int arg1)->{
                    Toast.makeText(RegisterActivity.this, "Podaj inny login", Toast.LENGTH_LONG).show();
                }));
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    // Alert ze dane lgoowania są puste
    public void openEmptyFieldAlert(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Login i hasło nie mogą być puste!");
        alertDialogBuilder.setPositiveButton("OK",
                ((DialogInterface arg0, int arg1)->{
                    Toast.makeText(RegisterActivity.this, "Podaj poprawne dane", Toast.LENGTH_LONG).show();
                }));
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void openLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}