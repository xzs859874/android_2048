package com.a2048;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import java.util.Locale;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG_EXIT = "exit";
    public Bundle s;
    public static final int UPDATE_TEXT = 1;

    private int[] values;
    private int score;
    private int timer;
    public  int st;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        每次切换会前台回到原页面
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
//            //结束你的activity
            finish();
            return;
        }

        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        setContentView(R.layout.layout_menu);
        ExitApplication.getInstance().addActivity(this);
        Button startButton = (Button)findViewById(R.id.start_game);
        Button rank1Button = (Button)findViewById(R.id.rank_button1);
        Button continueButton = (Button)findViewById(R.id.continue_game);
        Button overButton = (Button)findViewById(R.id.over_game);
        Button languageButton = (Button)findViewById(R.id.language);

        startButton.setOnClickListener(this);
        rank1Button.setOnClickListener(this);
        continueButton.setOnClickListener(this);
        overButton.setOnClickListener(this);
        languageButton.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {


        switch (view.getId()){
            case R.id.start_game:
                Intent intent = new Intent();
                intent.setClass(this,MainActivity.class);
                Bundle bundle1 = new Bundle();
                st = 0;
                bundle1.putInt("status",st);
                intent.putExtras(bundle1);
                startActivity(intent);
                break;
            case R.id.rank_button1:
                Intent intent1 = new Intent(MenuActivity.this,RankActivity.class);
                startActivity(intent1);
                break;
            case R.id.continue_game:
                values = new int[16];
                SharedPreferences sharedPreferences = getSharedPreferences("gameData",MODE_PRIVATE);
                score = sharedPreferences.getInt("score",0);
                timer = sharedPreferences.getInt("timer",0);
                for(int i=0;i<16;i++){
                    values[i] = sharedPreferences.getInt("value"+Integer.toString(i),0);
                }

                Intent intent2 = new Intent();
                Bundle bundle = new Bundle();
                st = 1;
                bundle.putInt("status",st);
                bundle.putIntArray("values",values);
                bundle.putInt("score",score);
                bundle.putInt("timer",timer);
                intent2.putExtras(bundle);
                intent2.setClass(this,MainActivity.class);
                startActivity(intent2);
                break;
            case R.id.over_game:
                ExitApplication.getInstance().exit();
                break;
            case R.id.language:
                String ss = getResources().getConfiguration().locale.getLanguage();
                Log.d("message:","当前语言是："+ss);
                if(ss.contains("zh")){
                    Log.d("message:","当前语言是："+ss);
                    Locale.setDefault(Locale.ENGLISH);
                    Log.d("message:","当前语言是："+Locale.getDefault());
                    Configuration configuration =getBaseContext().getResources().getConfiguration();
                    configuration.locale = Locale.ENGLISH;
                    getBaseContext().getResources().updateConfiguration(configuration,getBaseContext().getResources().getDisplayMetrics());
                    recreate();
                }else if (ss.contains("en")){
                    Log.d("message:","当前语言是："+ss);
                    Locale.setDefault(Locale.CHINESE);
                    Log.d("message:","当前语言是："+Locale.getDefault());
                    Configuration configuration = getBaseContext().getResources().getConfiguration();
                    configuration.locale = Locale.CHINESE;
                    getBaseContext().getResources().updateConfiguration(configuration,getBaseContext().getResources().getDisplayMetrics());
                    recreate();
                }

        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            boolean isExit = intent.getBooleanExtra(TAG_EXIT, false);
            if (isExit) {
                this.finish();
            }
        }
    }





}
