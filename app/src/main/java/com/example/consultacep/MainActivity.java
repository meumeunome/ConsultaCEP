package com.example.consultacep;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText cep = (EditText) findViewById(R.id.cepNum);
        final Button pesquisar = (Button) findViewById(R.id.pesquisar);
        final TextView logradouro = (TextView) findViewById(R.id.logradouro);
        final TextView complemento = (TextView) findViewById(R.id.complemento);
        final TextView bairro = (TextView) findViewById(R.id.bairro);
        final TextView localidade = (TextView) findViewById(R.id.localidade);
        final TextView uf = (TextView) findViewById(R.id.uf);

        final OkHttpClient client = new OkHttpClient();

        pesquisar.setOnClickListener(new View.OnClickListener(){
           @Override
            public void onClick(View view){
               switch(view.getId()){
                   case R.id.pesquisar:
                       final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                       builder.setCancelable(true);
                       builder.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
                           @Override
                           public void onClick(DialogInterface dialogInterface, int i){
                               dialogInterface.cancel();
                           }
                       });

                       if(cep.getText().length() != 8) {
                           builder.setTitle("Entrada Inválida!");
                           builder.setMessage("O CEP deve ter 8 caracteres.");
                           builder.show();
                           break;
                       }

                       String url = "https://viacep.com.br/ws/" + cep.getText() + "/json/";

                       Request request = new Request.Builder()
                               .url(url)
                               .build();

                       client.newCall(request).enqueue(new Callback() {
                           @Override
                           public void onFailure(@NotNull Call call, @NotNull IOException e) {
                               e.printStackTrace();
                               MainActivity.this.runOnUiThread(new Runnable() {
                                   @Override
                                   public void run() {
                                       logradouro.setText("");
                                       complemento.setText("");
                                       bairro.setText("");
                                       localidade.setText("");
                                       uf.setText("");
                                   }
                               });
                           }

                           @Override
                           public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                               if(response.isSuccessful()){
                                   final String myResponse = response.body().string();

                                   final HashMap<String,String> result = new ObjectMapper().readValue(myResponse, HashMap.class);

                                   if(result.get("erro") == null) {
                                       MainActivity.this.runOnUiThread(new Runnable() {
                                           @Override
                                           public void run() {
                                               logradouro.setText(result.get("logradouro"));
                                               complemento.setText(result.get("complemento"));
                                               bairro.setText(result.get("bairro"));
                                               localidade.setText(result.get("localidade"));
                                               uf.setText(result.get("uf"));
                                           }
                                       });
                                   } else{ //Caso o CEP seja inválido.
                                       MainActivity.this.runOnUiThread(new Runnable() {
                                           @Override
                                           public void run() {
                                               builder.setTitle("CEP inválido!");
                                               builder.setMessage("O CEP informado não foi encontrado.");
                                               builder.show();
                                           }
                                       });
                                   }
                               }
                           }
                       });

               }
           }
        });
    }
}
