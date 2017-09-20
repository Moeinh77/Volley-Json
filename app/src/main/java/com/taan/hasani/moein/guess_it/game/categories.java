package com.taan.hasani.moein.guess_it.game;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.taan.hasani.moein.volley.R;

public class categories extends AppCompatActivity {

    Button varzeshi, english;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        varzeshi = (Button) findViewById(R.id.varzeshi);
        english = (Button) findViewById(R.id.english);

        //////////////////////////
        varzeshi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alert_dialog_function("ورزشی");

            }
        });
        //////////////////////////

    }

    public void alert_dialog_function(final String category) {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.difficulty_dialog);
        dialog.setTitle("This is my custom dialog box");
        dialog.setCancelable(true);

        final RadioButton easy_RadioButton = (RadioButton) dialog.findViewById(R.id.easy);
        final RadioButton normal_RadioButton = (RadioButton) dialog.findViewById(R.id.normal);
        final RadioButton hard_RadioButton = (RadioButton) dialog.findViewById(R.id.hard);


        final Button start = (Button) dialog.findViewById(R.id.start);
        Button cancel = (Button) dialog.findViewById(R.id.cancel);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String difficulty = "متوسط";
                String rIsChecked = "false";

                if (easy_RadioButton.isChecked()) {
                    difficulty = easy_RadioButton.getText().toString();
                    rIsChecked = "true";
                }
                if (normal_RadioButton.isChecked()) {
                    difficulty = normal_RadioButton.getText().toString();
                    rIsChecked = "true";

                }
                if (hard_RadioButton.isChecked()) {
                    difficulty = hard_RadioButton.getText().toString();
                    rIsChecked = "true";

                }

                Intent i = new Intent(categories.this, two_player.class);

                if (rIsChecked.equals("true")) {

                    i.putExtra("category", category);
                    i.putExtra("difficulty", difficulty);

                    startActivity(i);

                } else {
                    Toast.makeText(getApplicationContext(), "درجه سطحی را انتخاب کنید", Toast.LENGTH_LONG).show();
                }


            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });


        dialog.show();

    }
}
