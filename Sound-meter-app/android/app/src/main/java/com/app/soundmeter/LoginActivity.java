package com.app.soundmeter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.soundmeter.api.UserAPI;
import com.app.soundmeter.configuration.DataHolder;
import com.app.soundmeter.configuration.RetrofitClient;
import com.app.soundmeter.dto.ResponseMessage;
import com.app.soundmeter.dto.UserCredentials;
import com.app.soundmeter.dto.UserProfile;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

@Slf4j
public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.EditTextLoginUsername)
    EditText editTextLoginUsername;
    @BindView(R.id.EditTextLoginPassword)
    EditText editTextLoginPassword;
    @BindView(R.id.ButtonLogin)
    Button buttonLogin;
    @BindView(R.id.ButtonUnauthorized)
    Button unauthorizedButton;
    @BindView(R.id.TextVieGoToRegisterPage)
    TextView textViewGoToRegisterPage;

    private Retrofit retrofit = RetrofitClient.getRetrofit();
    private UserAPI userAPI = retrofit.create(UserAPI.class);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        textViewGoToRegisterPage.setOnClickListener(view -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        unauthorizedButton.setOnClickListener(view -> {
            startActivity(new Intent(this, MainActivity.class));
        });

        buttonLogin.setOnClickListener(view -> {
            login(
                    UserCredentials
                            .builder()
                            .login(editTextLoginUsername.getText().toString())
                            .password(editTextLoginPassword.getText().toString())
                            .build(),
                    view
            );
        });
    }

    public void login(UserCredentials userCredentials, final View v) {
        Call<UserProfile> login = userAPI.loginUser(userCredentials);
        login.enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                log.info("/users/login (POST), Response: " + response.code() + "/ message: " + response.message());
                if (response.code() == 200) {
                    UserProfile userProfile = response.body();
                    DataHolder.getInstance().setUserProfile(userProfile);
                    openMenuActivity();
                } else {
                    openAlert(v);
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                log.info("/users/login (POST), Response: " + t.getMessage());
            }
        });
    }

    public void openAlert(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Niepoprawne dane logowania!");
        alertDialogBuilder.setPositiveButton("OK",
                ((DialogInterface arg0, int arg1)->{
                    Toast.makeText(LoginActivity.this, "Zaloguj się ponownie", Toast.LENGTH_LONG).show();
                }));
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void openMenuActivity() {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }

}