package com.taan.hasani.moein.guess_it.game_play;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.taan.hasani.moein.guess_it.appcontroller.AppController;
import com.taan.hasani.moein.volley.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

public class two_player extends AppCompatActivity {

    private String url = "http://online6732.tk/guessIt.php", id, completeWord,
            incompleteWord, gamedID, recivedTime, category;
    private int number_of_trueGuess;
    private TextView word, message, timer, player2_textview, player1_textview;
    private EditText entered_word;
    private CountDownTimer countDownTimer;
    private String turn,
            flag__nextWord_Timer = null;//baraye inke check konim aya vared next word shode
    //agar shode bashad timer be karoftade
    private int spent_time = 0;
    private boolean inGame = true;
    private boolean flag_counter;//baraye inke dar onDestroy error rokh nade age timer rah nayoftade bood

    private boolean words_loaded;//baraye jelo giri az crash dar soorat zadan next ya check
    //agar kalame load nashode bashad
    private int currentword_number;
    private int Toatalwords;//tedad kalamte har bazi ke az server miad
    String Rivalscore_gameEnd, Playerscore_gameEnd;
    private int RivalWordsNumber;//tedad kalameti ke harif dashte
    private String status, type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_player);

        String MY_PREFS_NAME = "username and password";
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        id = prefs.getString("userID", null);

        Button check_bt = (Button) findViewById(R.id.check_bt);
        Button nextWord_bt = (Button) findViewById(R.id.nextWord_bt);
        word = (TextView) findViewById(R.id.word);
        message = (TextView) findViewById(R.id.message);
        entered_word = (EditText) findViewById(R.id.enteredWord);
        timer = (TextView) findViewById(R.id.timer);
        player2_textview = (TextView) findViewById(R.id.rivalscore);
        player1_textview = (TextView) findViewById(R.id.yourscore);


        //difficaulty va category ro az activity ghabl migirad
        Bundle bundle = getIntent().getExtras();
        category = bundle.getString("category");
        type = bundle.getString("type");
        //Toatalwords = bundle.getInt("totalwordsnumber");
        ///////////////////////////////////////////////////////

        message.setVisibility(View.INVISIBLE);
        word.setVisibility(View.INVISIBLE);


        newTwoPlayerGame();

        check_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (words_loaded) {

                    if (turn.equals("notmyturn")) {

                        Toast.makeText(getApplicationContext(), "لطفا صبر کنید...", Toast.LENGTH_SHORT).show();

                    } else if (!timer.getText().toString().equals("")) {

                        String Player_score = timer.getText().toString();
                        String Player_time = Integer.toString(15 - Integer.parseInt(Player_score));
                        String myturn;

                        if (!entered_word.getText().toString().equals("")) {

                            if (entered_word.getText().toString().equals(completeWord)) {

                                countDownTimer.cancel();

                                number_of_trueGuess++;

                                myturn = "no";//playerdg javab dade hala bayad nobat ro avaz konim

                                word.setText(completeWord);

                                Snackbar.make(findViewById(R.id.twoPlayerActivity), "Congratulations !!! Your guess was RIGHT !"
                                        , Snackbar.LENGTH_LONG)
                                        .setActionTextColor(Color.YELLOW).show();

                                //seda movafaghat dar soorat javab doros
                                MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),
                                        R.raw.success);
                                mediaPlayer.start();
                                ////////////////////////////////////////////////////////

                                setAnswer(entered_word.getText().toString(),
                                        Player_time, Player_score, myturn);

                                //baraye check kardan nobat
                                sendNextWord();
                                ////////////////////////////

                            } else {
                                Snackbar.make(findViewById(R.id.twoPlayerActivity), "No,Guess again !"
                                        , Snackbar.LENGTH_LONG)
                                        .setActionTextColor(Color.YELLOW).show();

                                myturn = "yes";//javab ghalatehamchenan nobat bazikon baghi mimanad

                                setAnswer(entered_word.getText().toString(),
                                        Player_time, Player_score, myturn);

                            }

//                            if (timer.getText().toString().equals("0")) {
//                                myturn = "no";
//                                Toast.makeText(getApplicationContext(), "Times up!", Toast.LENGTH_SHORT).show();
//                                setAnswer(entered_word.getText().toString(),
//                                        Player_time, Player_score, myturn);
//                            }
                        }
                    }
                }
            }
        });

        nextWord_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (words_loaded) {

                    if (flag__nextWord_Timer.equals("yes")) {

                        countDownTimer.cancel();

                        if (timer.getText().toString() != recivedTime) {

                            spent_time = Integer.parseInt(recivedTime)
                                    - Integer.parseInt(timer.getText().toString());

                            sendNextWord();

                        } else {

                            sendNextWord();
                        }

                    } else {

                        sendNextWord();

                    }
                }
            }

        });


    }

    public void newTwoPlayerGame() {

        number_of_trueGuess = 0;
        currentword_number = 0;

        HashMap<String, String> info = new HashMap<>();

        info.put("action", "newGame");
        info.put("userID", id);
        info.put("mode", "twoPlayer");
        info.put("type", type);

        JSONObject jsonObject = new JSONObject(info);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url, jsonObject, new Response.Listener<JSONObject>() {


            @Override
            public void onResponse(JSONObject response) {

                try {

                    Toast.makeText(getApplicationContext(),
                            response.toString(), Toast.LENGTH_SHORT).show();

                    if (response.getString("gameID").equals("-1")) {

                        isMyGameReady();

                    } else {

                        gamedID = response.getString("gameID");

                        setGameSettings();
                        Toast.makeText(getApplicationContext(),
                                gamedID, Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),
                            "newTwoPlayerGame " + e.toString(), Toast.LENGTH_SHORT).show();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        "*newTwoPlayerGame**Volley  :" + error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void isMyGameReady() {
        HashMap<String, String> info = new HashMap<>();

        info.put("action", "isMyGameReady");
        info.put("userID", id);

        JSONObject jsonObject = new JSONObject(info);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url, jsonObject, new Response.Listener<JSONObject>() {


            @Override
            public void onResponse(JSONObject response) {

                try {

                    Toast.makeText(getApplicationContext(),
                            response.getString("responseData"), Toast.LENGTH_SHORT).show();
                    if (inGame) {
                        if (response.getString("gameID").equals("-1")) {


                            new Handler().postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    isMyGameReady();
                                }
                            }, 3000);


                        } else {
                            gamedID = response.getString("gameID");
                            setGameSettings();
                        }
                    }

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),
                            e.toString(), Toast.LENGTH_SHORT).show();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        "isMyGameReady***Volley  :" + error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        AppController.getInstance().addToRequestQueue(jsonObjectRequest);

    }

    public void setGameSettings() {

        HashMap<String, String> info = new HashMap<>();

        info.put("action", "setGameSetting");
        info.put("userID", id);
        info.put("gameID", gamedID);

        try {

            info.put("categories", URLEncoder.encode(category, "utf-8"));

        } catch (UnsupportedEncodingException e) {
            Toast.makeText(getApplicationContext(), "UnsupportedEncodingException", Toast.LENGTH_SHORT).show();
        }


        JSONObject jsonObject = new JSONObject(info);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url, jsonObject, new Response.Listener<JSONObject>() {


            @Override
            public void onResponse(JSONObject response) {

                try {

                    if (response.getString("dataIsRight").equals("yes")) {

                        sendNextWord();

                    } else {

                        Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_SHORT).show();

                    }

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),
                            "setGameSetting " + e.toString(), Toast.LENGTH_SHORT).show();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        "setGameSetting***Volley  :" + error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        AppController.getInstance().addToRequestQueue(jsonObjectRequest);


    }

    public void sendNextWord() {

        if (inGame) {

            gameInfo_during();

            HashMap<String, String> info = new HashMap<>();

            info.put("action", "sendNextWord");
            info.put("gameID", gamedID);
            info.put("userID", id);

            entered_word.setText("");
            word.setText("");
            message.setText("");
            timer.setText("");

            JSONObject jsonObject = new JSONObject(info);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                    url, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    try {

                        if (response.getString("dataIsRight").equals("yes")) {
                            //dataIsRight zamani ke nobatet bashe yes miad


                            //agar kalame ha tamam shode bashad dialog payan ro miare
                            if (currentword_number == (Toatalwords + 1)) {
                                alert_dialog_function_game_end();
                            }
                            ///////////////////////////////////////////////////////////

                            currentword_number++;


                            //   if(!response.getJSONObject("word").
                            //           getString("word").equals("outOfWords"))
                            //baraye inke bebinim kalamt tamoom shode ya na
                            //   {

                            turn = "myturn";

                            flag__nextWord_Timer = "yes";

                            incompleteWord = response.getJSONObject("word").getString("incompleteWord");

                            completeWord = response.getJSONObject("word").getString("word");

                            recivedTime = response.getJSONObject("word").getString("time");

                            ////////////////////////////////////////////
                            countDownTimer = new CountDownTimer((Integer.parseInt(recivedTime) - spent_time) * 1000, 1000) {
                                public void onTick(long millisUntilFinished) {
                                    timer.setText(String.valueOf(millisUntilFinished / 1000));
                                    flag_counter = true;

                                }

                                public void onFinish() {
                                    timer.setText("0");
                                    setAnswer(entered_word.getText().toString(),
                                            "0", "0", "no");
                                    spent_time = 0;
                                    words_loaded = false;
                                    // check kardane nobat
                                    sendNextWord();
                                    ///////////////////////
                                }
                            };
                            ////////////////////////////////////////////
                            countDownTimer.start();

                            word.setVisibility(View.VISIBLE);
                            word.setText(incompleteWord);

                            words_loaded = true;

                            //} else{
                            //     alert_dialog_function_game_end();
                            // }
                        } else {


                            if (currentword_number == (Toatalwords)) {

                                alert_dialog_function_game_end();

                            }

                            message.setVisibility(View.INVISIBLE);
                            word.setVisibility(View.INVISIBLE);

                            turn = "notmyturn";

                            //Toast.makeText(getApplicationContext(),
                            //        "It's not your turn yet, Please wait...", Toast.LENGTH_SHORT).show();
                            timer.setText("لطفا صبر کنید...");
                            //ferestadan request baraye inke bebinim nobateman shode ya na
                            new Handler().postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    sendNextWord();
                                }
                            }, 1000);
                            //////////////////////////////////////////

                            //   }
                        }


                    } catch (JSONException e) {

                        Toast.makeText(getApplicationContext(), "sendNextWord " + e.toString(),
                                Toast.LENGTH_SHORT).show();

                    }


                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(),
                            "sendNextWord***Volley  :" + error.toString(), Toast.LENGTH_SHORT).show();
                }
            });

            AppController.getInstance().addToRequestQueue(jsonObjectRequest);


        }
    }

    public void setAnswer(String entered_word, String player_time,
                          String player_score, String myturn) {

        HashMap<String, String> info = new HashMap<>();
        HashMap<String, String> answer_hashmap = new HashMap<>();
        /////////////////////////
        answer_hashmap.put("time", player_time);
        answer_hashmap.put("score", player_score);
        answer_hashmap.put("answer", entered_word);
        answer_hashmap.put("myTurn", myturn);

        JSONObject answer = new JSONObject(answer_hashmap);

        /////////////////////////
        info.put("action", "setAnswer");
        info.put("gameID", gamedID);
        info.put("userID", id);
        info.put("answer", answer.toString());
        /////////////////////////

        JSONObject jsonObject = new JSONObject(info);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                // Toast.makeText(getApplicationContext(),
                //       "setAnswer response  :" + response.toString(), Toast.LENGTH_SHORT).show();

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        "setAnswer***Volley  :" + error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void gameInfo_during() {

        final String MY_PREFS_NAME = "username and password";
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        final String id = prefs.getString("userID", null);

        HashMap<String, String> info = new HashMap<>();

        info.put("action", "sendGameInformation");
        info.put("userID", id);
        info.put("gameID", gamedID);

        JSONObject jsonObject = new JSONObject(info);
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    String your_score = response.getString("playerOneTotalScore");

                    String rival_score = response.getString("playerTwoTotalScore");

                    status = response.getString("status");//baraye inke bebinim
                    //bazi tamoom shode ya na

                    if (id.equals(response.getString("playerOneID"))) {

                        player2_textview.setText("امتیاز " + response.getString("playerTwoID")
                                + " : " + rival_score);
                        player1_textview.setText("امتیاز شما : " + your_score);

                        player2_textview.setTextColor(Color.RED);
                        player1_textview.setTextColor(Color.GREEN);

                        Rivalscore_gameEnd = rival_score;
                        Playerscore_gameEnd = your_score;

                        RivalWordsNumber = Integer.parseInt(
                                response.getString("playerTwoRounds"));

                    } else if (id.equals(response.getString("playerTwoID"))) {

                        player2_textview.setText("امتیاز شما : " + rival_score);
                        player1_textview.setText("امتیاز " + response.getString("playerOneID") +
                                " : " + your_score);

                        player2_textview.setTextColor(Color.GREEN);
                        player1_textview.setTextColor(Color.RED);

                        Rivalscore_gameEnd = your_score;
                        Playerscore_gameEnd = rival_score;

                        RivalWordsNumber = Integer.parseInt(
                                response.getString("playerOneRounds"));
                    }

                    if (Toatalwords == 0) {
                        Toatalwords = Integer.parseInt(response.getString("numberOfWords"));

//                        Toast.makeText(getApplicationContext(), String.valueOf(Toatalwords)
//                                + "game during word numbers", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        AppController.getInstance().addToRequestQueue(jsonObjectRequest);

    }

    public void alert_dialog_function_game_end() {

        gameInfo_during();

        Toast.makeText(getApplicationContext(), "Rival words:" + String.valueOf(RivalWordsNumber),
                Toast.LENGTH_SHORT).show();
        //agar ke tedad javab hay harif be andaze player bood
        //dialog payan ro biar age nabood request bede ta andaze hsode bashe ****
        if (status.equals("game ended")) {

            inGame = false;

            if (countDownTimer != null) countDownTimer.cancel();


            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.game_end_twoplayer_dialog);
            dialog.setCancelable(false);

            Button cancel = (Button) dialog.findViewById(R.id.no);
            TextView guesses_true = (TextView) dialog.findViewById(R.id.guesses_true);
            TextView yourscore_gameEnd = (TextView) dialog.findViewById(R.id.player_score_gameEnd);
            TextView guesses_false = (TextView) dialog.findViewById(R.id.guesses_false);
            TextView rivalScore = (TextView) dialog.findViewById(R.id.rivalScore);
            // TextView newHighScore_view = (TextView) dialog.findViewById(R.id.newHighscore);

            // newHighScore_view.setVisibility(View.INVISIBLE);

            yourscore_gameEnd.setText(Playerscore_gameEnd);

            //ejra baraye gereftane akharin score e nahaee
            /////////////////////////////////
            rivalScore.setText(Rivalscore_gameEnd);

            //  int highscore = prefs.getInt("HighScore", 0);//gereftane higscore

            //dar soorat zadane highscore jadid
//        if (Total_gamescore > highscore) {
//
//            //save kardane highscore e jadid
//            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME
//                    , MODE_PRIVATE).edit();
//            editor.putInt("HighScore", Total_gamescore);
//            editor.apply();
//            //////////////////////////////////
//
//            newHighScore_view.setVisibility(View.VISIBLE);//neshan dadan payame highscore
//
//            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.success);
//            mediaPlayer.start();
//
//        }
            /////////////////////////////////////////////////////////

            guesses_true.setText(String.valueOf(number_of_trueGuess));
            guesses_false.setText(String.valueOf(Toatalwords - number_of_trueGuess));

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });


            dialog.show();


        } else {

            if (inGame) {

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        alert_dialog_function_game_end();
                    }
                }, 1000);

            }
        }

    }

    @Override
    protected void onDestroy() {
        inGame = false;

        if (flag_counter) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }


}
