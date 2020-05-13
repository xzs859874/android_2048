package com.a2048;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import java.util.Random;;
import java.util.Vector;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final long VERSION = 1L;
    private int tt = 1;
    int a = 1;

    private String[] mycolor = {"#EEE4DA", "#ECECC8", "#F4A460",
            "#D2691E", "#FF3030", "#8B1A1A", "#FFFF00"};
    private float x1 = 0;
    private float x2 = 0;
    private float y1 = 0;
    private float y2 = 0;



    private Button[] buttons;
    private Button saveButton;
    private Button restartButton;
    private TextView scoreText;
    private TextView timerText;
    private long currentSecond = 0;//计时器当前秒数
    private int[] values;              //用于标志每个位置的数值
    private int score;                 //用于记录分数
    private Vector<Integer> emptyItem = new Vector<>(); //用于记录空位置元素(0-15)
    private ContentResolver resolver;
    private Uri uri;
    private ContentValues value;
    private EditText edit;
    private Bundle bundle;
    SharedPreferences sharedPreferences;
    private Handler mhandle = new Handler();
    private AudioService audioService;
    private Intent in;
    private Boolean isPauseTimer = false;
    private ImageButton musicButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        resolver = this.getContentResolver();
        timerText = (TextView) findViewById(R.id.timer_text);
        bundle = new Bundle();
        bundle = getIntent().getExtras();
        values = new int[16];

       //判断是开始游戏还是继续游戏，开始游戏初始化界面，继续游戏赋值后刷新ui
        if (bundle.getInt("status")!=0 && bundle.getInt("timer")>0){
            values = bundle.getIntArray("values");
            score = bundle.getInt("score");
            currentSecond = (long) bundle.getInt("timer");
            refreshEmptyItem();
        }else {
            reSetData();
           for (int i = 0; i < 16; i++) {
               values[i] = 0;
           }
            initNums();
        }
        in = new Intent(this,AudioService.class);
        //打开服务
        startService(in);
        //绑定服务
        bindService(in, conn, Context.BIND_AUTO_CREATE);
        //初始化计时器
        iniTimer();
        //初始化界面组件：16个矩形
        initView();
        //根据values数组更新界面,和emptyItem
        refresh();


        sharedPreferences = getSharedPreferences("gameData",0);
        saveButton = (Button) findViewById(R.id.save);
        restartButton = (Button) findViewById(R.id.restart);
        musicButton =(ImageButton) findViewById(R.id.music);
        saveButton.setOnClickListener(this);
        restartButton.setOnClickListener(this);
        musicButton.setOnClickListener(this);
        ExitApplication.getInstance().addActivity(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.restart:
                currentSecond = 0;
                values = new int[16];
                score = 0;
                initNums();
                refresh();
                break;
            case R.id.save:
                //保存数据至SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                for (int i = 0;i<16;i++){
                    editor.putInt("value"+Integer.toString(i),values[i]);
                }
                editor.putInt("score",score);
                editor.putInt("timer",(int) currentSecond);
                editor.apply();
                Toast.makeText(MainActivity.this,R.string.save_jd,Toast.LENGTH_SHORT).show();
                break;
            case R.id.music:
                if (audioService.player.isPlaying()){
                    musicButton.setImageResource(R.mipmap.music_pause);
                    audioService.player.pause();
                }else {
                    musicButton.setImageResource(R.mipmap.music_start);
                    audioService.player.start();
                }

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                isPauseTimer = true;
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.back_home);
                builder.setCancelable(false);
                builder.setMessage(R.string.save_tip_1);
                builder.setNegativeButton(R.string.save_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        isPauseTimer = false;
                        dialogInterface.dismiss();
                        finish();
                    }
                });
                builder.setPositiveButton(R.string.save_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        for (int j = 0;j<16;j++){
                            editor.putInt("value"+Integer.toString(j),values[j]);
                        }
                        editor.putInt("score",score);
                        editor.putInt("timer",(int) currentSecond);
                        editor.apply();
                        Toast.makeText(MainActivity.this,R.string.save_jd,Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                        finish();
                    }
                });
                builder.create().show();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onPause() {
        super.onPause();
        //解绑服务
        unbindService(conn);
        //停止服务
        stopService(in);


    }

    @Override
    protected void onResume() {
        super.onResume();
        //绑定service
        bindService(in, conn, Context.BIND_AUTO_CREATE);
        //开始service
        startService(in);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //停掉 服务
        stopService(in);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        for (int k = 0;k<16;k++){
//            editor.putInt("value"+Integer.toString(k),values[k]);
//        }
//        editor.putInt("score",score);
//        editor.putInt("timer",(int) currentSecond);
//        editor.apply();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray("values", values);
        outState.putInt("score", score);
        outState.putInt("timers",(int) currentSecond);
        Log.d("__main:saveInstance:", "************");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        values = savedInstanceState.getIntArray("values");
        score = savedInstanceState.getInt("score");
        currentSecond = savedInstanceState.getInt("timers");
        refreshEmptyItem();
        Log.d("__main:restoreInstance:", "************");
    }

    //使用ServiceConnection来监听Service状态的变化
    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            audioService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            //实例化audioService,通过binder来实现
            audioService = ((AudioService.AudioBinder)binder).getService();
        }
    };
    /**
     *判断当前应用程序处于前台还是后台
     */
    public static boolean isApplicationBroughtToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    //初始化计时器
    private  void iniTimer(){


        new Thread(new Runnable() {
            @Override
            public void run() {
                timerText.setText(com.a2048.Timer.getFormatHMS(currentSecond));
                if (isApplicationBroughtToBackground(MainActivity.this) || isPauseTimer){
                    mhandle.postDelayed(this, 1000);
                }else {
                    mhandle.postDelayed(this, 1000);
                    currentSecond = currentSecond+1000;
                }

            }

        }).start();
    }

    //根据values数组更新界面,和emptyItem
    private void refresh() {
        //更新界面
        for (int i = 0; i < 16; i++) {
            if (values[i] == 0) {
                buttons[i].setText("");
                buttons[i].setBackgroundColor(Color.parseColor("#E8E8E8"));
            } else {
                buttons[i].setText(Integer.toString(values[i]));
                buttons[i].setBackgroundColor(getColorFor(values[i]));
            }
        }
        //更新emptyItem;
        refreshEmptyItem();
        //判断游戏是否结束
        if (emptyItem.size() == 0 && isEnd()) {
                isPauseTimer = true;
                edit = new EditText(MainActivity.this);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(getResources().getString(R.string.over_tip_p)+ Integer.toString(score) + "!\n" +getResources().getString(R.string.over_tip_l));
                builder.setTitle(R.string.overgame);
                builder.setView(edit);
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.save_record, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String sname = edit.getText().toString().trim();
                        if (sname.length()>6){
                            sname =sname.substring(0,5)+"~~";
                        }
                        uri = Uri.parse("content://com.example.mydb.MyProvider/rank");
                        value = new ContentValues();
                        value.put("name",sname);
                        value.put("score",score);
                        resolver.insert(uri,value);
                        dialog.dismiss();
                        finish();
                        Toast.makeText(MainActivity.this,R.string.save_tip_success,Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton(R.string.back_home, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();

                    }
                });

                builder.create().show();


        }
        scoreText.setText(Integer.toString(score));
    }
    //初始化界面组件
    public void initView() {
        buttons = new Button[17];
        buttons[0] = (Button) findViewById(R.id.button1);
        buttons[1] = (Button) findViewById(R.id.button2);
        buttons[2] = (Button) findViewById(R.id.button3);
        buttons[3] = (Button) findViewById(R.id.button4);
        buttons[4] = (Button) findViewById(R.id.button5);
        buttons[5] = (Button) findViewById(R.id.button6);
        buttons[6] = (Button) findViewById(R.id.button7);
        buttons[7] = (Button) findViewById(R.id.button8);
        buttons[8] = (Button) findViewById(R.id.button9);
        buttons[9] = (Button) findViewById(R.id.button10);
        buttons[10] = (Button) findViewById(R.id.button11);
        buttons[11] = (Button) findViewById(R.id.button12);
        buttons[12] = (Button) findViewById(R.id.button13);
        buttons[13] = (Button) findViewById(R.id.button14);
        buttons[14] = (Button) findViewById(R.id.button15);
        buttons[15] = (Button) findViewById(R.id.button16);
        scoreText = (TextView) findViewById(R.id.score);

        //注册子控件button的clickable为false,才能在检查到Button上滑动的效果
        for (int i = 0; i < 16; i++) {
            buttons[i].setClickable(false);
        }

    }

    //根据values设置emptyItem
    private void refreshEmptyItem() {
        emptyItem.clear();
        for (int i = 0; i < 16; i++) {
            if (values[i] == 0) {
                emptyItem.add(i);
            }
        }
    }


    private void reSetData(){
        score = 0;
        values = new int[16];
        currentSecond = 0;
    }

    //初始化游戏参数
    public void initNums() {

        refreshEmptyItem();

        int[] nums = new int[2];

        //生成两个0-15内的随机数,必须保证不相同(用于记录初始数值位置,初始产生两个数)
        Random random = new Random();
        nums[0] = random.nextInt(16);
        nums[1] = random.nextInt(16);
        while (nums[1] == nums[0]) {
            nums[1] = random.nextInt(16);
        }

        //生成一个0-1随机数;0表示位置填充2,1表示位置填充4
        int a;
        a = random.nextInt(2);
        values[nums[0]] = 2 * (a + 1);
        a = random.nextInt(2);
        values[nums[1]] = 2 * (a + 1);
    }

    //如果有空白位置，则生成一个新的数(2或4),否则游戏结束
    public Boolean generateNew() {
        refreshEmptyItem();

        Random random = new Random();
        int a = random.nextInt(2);
        a = 2 * (a + 1);

        int countEmpty = emptyItem.size();
        if (countEmpty > 0) {
            int loc = random.nextInt(countEmpty);
            loc = emptyItem.elementAt(loc);
            values[loc] = a;
            buttons[loc].setText(Integer.toString(a));
            buttons[loc].setBackgroundColor(Color.parseColor(mycolor[a / 2 - 1]));
            refreshEmptyItem();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            x1 = event.getX();
            y1 = event.getY();
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            x2 = event.getX();
            y2 = event.getY();
            boolean flag = false;
            if (y1 - y2 > 50) {
                flag = doUp();
            } else if (y2 - y1 > 50) {
                flag = doDown();
            } else if (x1 - x2 > 50) {
                flag = doLeft();
            } else if (x2 - x1 > 50) {
                flag = doRight();
            }

            //生成一个新的数
            if (flag) {
                generateNew();
            }

            refresh();
        }
        return super.onTouchEvent(event);
    }

    private Boolean isEnd() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                if (values[4 * i + j] == values[4 * i + j + 1]) {
                    return false;
                }
            }
        }

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3 * 4; j += 4) {
                if (values[i + j] == values[i + j + 4]) {
                    return false;
                }
            }
        }

        return true;
    }

    //游戏逻辑
    private Boolean doUp() {
        Boolean isChanged = false;
        for (int i = 0; i < 4; i++) {
            Vector<Integer> loc = new Vector<>(4);
            Vector<Integer> val = new Vector<>(4);
            for (int j = i + 3 * 4; j >= i; j -= 4) {
                if (values[j] != 0) {
                    loc.add(j);
                    val.add(values[j]);
                }
            }
            if (loc.size() == 1) {
                if (i != loc.elementAt(0)) {
                    values[i] = val.elementAt(0);
                    values[loc.elementAt(0)] = 0;
                    isChanged = true;
                }
            } else if (loc.size() == 2) {
                if (val.elementAt(0).equals(val.elementAt(1))) {
                    values[loc.elementAt(0)] = 0;
                    values[loc.elementAt(1)] = 0;
                    values[i] = val.elementAt(0) * 2;
                    score += val.elementAt(0) * 2;
                    isChanged = true;
                } else {
                    values[loc.elementAt(0)] = 0;
                    values[loc.elementAt(1)] = 0;
                    values[i] = val.elementAt(1);
                    values[i + 4] = val.elementAt(0);
                    if (!loc.elementAt(0).equals(i + 4)) {
                        isChanged = true;
                    }
                }
            } else if (loc.size() == 3) {
                for (int j = 0; j < 3; j++) {
                    values[loc.elementAt(j)] = 0;
                }

                if (val.elementAt(2).equals(val.elementAt(1))) {
                    score += val.elementAt(2) * 2;
                    values[i] = val.elementAt(2) * 2;
                    values[i + 4] = val.elementAt(0);
                    isChanged = true;
                } else if (val.elementAt(1).equals(val.elementAt(0))) {
                    score += val.elementAt(1) * 2;
                    values[i] = val.elementAt(2);
                    values[i + 4] = val.elementAt(1) * 2;
                    isChanged = true;
                } else {
                    values[i] = val.elementAt(2);
                    values[i + 4] = val.elementAt(1);
                    values[i + 2 * 4] = val.elementAt(0);
                    if (!loc.elementAt(0).equals(i + 2 * 4)) {
                        isChanged = true;
                    }
                }
            } else if (loc.size() == 4) {
                if (val.elementAt(3).equals(val.elementAt(2))) {
                    if (val.elementAt(1).equals(val.elementAt(0))) {
                        score = score + val.elementAt(3) * 2 + val.elementAt(1) * 2;
                        for (int j = 0; j < 4; j++) {
                            values[loc.elementAt(j)] = 0;
                        }

                        values[i] = val.elementAt(3) * 2;
                        values[i + 4] = val.elementAt(1) * 2;
                    } else {
                        values[loc.elementAt(0)] = 0;
                        values[i] = val.elementAt(3) * 2;
                        values[i + 4] = val.elementAt(1);
                        values[i + 2 * 4] = val.elementAt(0);
                    }
                    isChanged = true;
                } else if (val.elementAt(2).equals(val.elementAt(1))) {
                    score += val.elementAt(2) * 2;
                    values[loc.elementAt(0)] = 0;
                    values[i + 4] = val.elementAt(2) * 2;
                    values[i + 2 * 4] = val.elementAt(0);
                    isChanged = true;
                } else if (val.elementAt(1).equals(val.elementAt(0))) {
                    score += val.elementAt(0) * 2;
                    values[loc.elementAt(0)] = 0;
                    values[i + 2 * 4] = val.elementAt(0) * 2;
                    isChanged = true;
                }
            }
        }
        refreshEmptyItem();
        return isChanged;
    }

    private Boolean doDown() {
        //isChanged用于判断当前方向操作是否发生变化,变化包括合并和移动
        Boolean isChanged = false;
        for (int i = 0; i < 4; i++) {
            Vector<Integer> loc = new Vector<>(4);
            Vector<Integer> val = new Vector<>(4);
            for (int j = i; j <= i + 3 * 4; j += 4) {
                if (values[j] != 0) {
                    loc.add(j);
                    val.add(values[j]);
                }
            }
            if (loc.size() == 1) {
                if (loc.elementAt(0) != (i + 3 * 4)) {
                    values[i + 3 * 4] = val.elementAt(0);
                    values[loc.elementAt(0)] = 0;
                    isChanged = true;
                }
            } else if (loc.size() == 2) {
                if (val.elementAt(0).equals(val.elementAt(1))) {
                    values[loc.elementAt(0)] = 0;
                    values[loc.elementAt(1)] = 0;
                    values[i + 3 * 4] = val.elementAt(0) * 2;
                    score += val.elementAt(0) * 2;
                    isChanged = true;
                } else {
                    values[loc.elementAt(0)] = 0;
                    values[loc.elementAt(1)] = 0;
                    values[i + 3 * 4] = val.elementAt(1);
                    values[i + 2 * 4] = val.elementAt(0);
                    if (!loc.elementAt(0).equals(i + 2 * 4)) {
                        isChanged = true;
                    }
                }
            } else if (loc.size() == 3) {
                for (int j = 0; j < 3; j++) {
                    values[loc.elementAt(j)] = 0;
                }

                if (val.elementAt(2).equals(val.elementAt(1))) {
                    score += val.elementAt(2) * 2;
                    values[i + 3 * 4] = val.elementAt(2) * 2;
                    values[i + 2 * 4] = val.elementAt(0);
                    isChanged = true;
                } else if (val.elementAt(1).equals(val.elementAt(0))) {
                    score += val.elementAt(1) * 2;
                    values[i + 3 * 4] = val.elementAt(2);
                    values[i + 2 * 4] = val.elementAt(1) * 2;
                    isChanged = true;
                } else {
                    values[i + 3 * 4] = val.elementAt(2);
                    values[i + 2 * 4] = val.elementAt(1);
                    values[i + 4] = val.elementAt(0);
                    if (!loc.elementAt(0).equals(i + 4)) {
                        isChanged = true;
                    }
                }
            } else if (loc.size() == 4) {
                if (val.elementAt(3).equals(val.elementAt(2))) {
                    if (val.elementAt(1).equals(val.elementAt(0))) {
                        score = score + val.elementAt(3) * 2 + val.elementAt(1) * 2;
                        for (int j = 0; j < 4; j++) {
                            values[loc.elementAt(j)] = 0;
                        }

                        values[i + 3 * 4] = val.elementAt(3) * 2;
                        values[i + 2 * 4] = val.elementAt(1) * 2;
                    } else {
                        values[loc.elementAt(0)] = 0;
                        values[i + 3 * 4] = val.elementAt(3) * 2;
                        values[i + 2 * 4] = val.elementAt(1);
                        values[i + 4] = val.elementAt(0);
                    }
                    isChanged = true;
                } else if (val.elementAt(2).equals(val.elementAt(1))) {
                    score += val.elementAt(2) * 2;
                    values[loc.elementAt(0)] = 0;
                    values[i + 2 * 4] = val.elementAt(2) * 2;
                    values[i + 4] = val.elementAt(0);
                    isChanged = true;
                } else if (val.elementAt(1).equals(val.elementAt(0))) {
                    score += val.elementAt(0) * 2;
                    values[loc.elementAt(0)] = 0;
                    values[i + 4] = val.elementAt(0) * 2;
                    isChanged = true;
                }
            }
        }
        refreshEmptyItem();
        return isChanged;
    }

    private Boolean doLeft() {
        Boolean isChanged = false;
        for (int i = 0; i < 4; i++) {
            Vector<Integer> loc = new Vector<>(4);
            Vector<Integer> val = new Vector<>(4);
            for (int j = 3 + i * 4; j >= i * 4; j--) {
                if (values[j] != 0) {
                    loc.add(j);
                    val.add(values[j]);
                }
            }
            if (loc.size() == 1) {
                if (loc.elementAt(0) != (i * 4)) {
                    values[i * 4] = val.elementAt(0);
                    values[loc.elementAt(0)] = 0;
                    isChanged = true;
                }
            } else if (loc.size() == 2) {
                if (val.elementAt(0).equals(val.elementAt(1))) {
                    values[loc.elementAt(0)] = 0;
                    values[loc.elementAt(1)] = 0;
                    values[i * 4] = val.elementAt(0) * 2;
                    score += val.elementAt(0) * 2;
                    isChanged = true;
                } else {
                    values[loc.elementAt(0)] = 0;
                    values[loc.elementAt(1)] = 0;
                    values[i * 4] = val.elementAt(1);
                    values[1 + i * 4] = val.elementAt(0);
                    if (!loc.elementAt(0).equals(1 + i * 4)) {
                        isChanged = true;
                    }
                }
            } else if (loc.size() == 3) {
                for (int j = 0; j < 3; j++) {
                    values[loc.elementAt(j)] = 0;
                }

                if (val.elementAt(2).equals(val.elementAt(1))) {
                    score += val.elementAt(2) * 2;
                    values[i * 4] = val.elementAt(2) * 2;
                    values[1 + i * 4] = val.elementAt(0);
                    isChanged = true;
                } else if (val.elementAt(1).equals(val.elementAt(0))) {
                    score += val.elementAt(1) * 2;
                    values[i * 4] = val.elementAt(2);
                    values[1 + i * 4] = val.elementAt(1) * 2;
                    isChanged = true;
                } else {
                    values[i * 4] = val.elementAt(2);
                    values[1 + i * 4] = val.elementAt(1);
                    values[2 + i * 4] = val.elementAt(0);
                    if (!loc.elementAt(0).equals(2 + i * 4)) {
                        isChanged = true;
                    }
                }
            } else if (loc.size() == 4) {
                if (val.elementAt(3).equals(val.elementAt(2))) {
                    if (val.elementAt(1).equals(val.elementAt(0))) {
                        score = score + val.elementAt(3) * 2 + val.elementAt(1) * 2;
                        for (int j = 0; j < 4; j++) {
                            values[loc.elementAt(j)] = 0;
                        }

                        values[i * 4] = val.elementAt(3) * 2;
                        values[1 + i * 4] = val.elementAt(1) * 2;
                    } else {
                        values[loc.elementAt(0)] = 0;
                        values[i * 4] = val.elementAt(3) * 2;
                        values[1 + i * 4] = val.elementAt(1);
                        values[2 + i * 4] = val.elementAt(0);
                    }
                    isChanged = true;
                } else if (val.elementAt(2).equals(val.elementAt(1))) {
                    score += val.elementAt(2) * 2;
                    values[loc.elementAt(0)] = 0;
                    values[1 + i * 4] = val.elementAt(2) * 2;
                    values[2 + i * 4] = val.elementAt(0);
                    isChanged = true;
                } else if (val.elementAt(1).equals(val.elementAt(0))) {
                    score += val.elementAt(0) * 2;
                    values[loc.elementAt(0)] = 0;
                    values[2 + i * 4] = val.elementAt(0) * 2;
                    isChanged = true;
                }
            }
        }
        refreshEmptyItem();
        return isChanged;
    }

    private Boolean doRight() {
        Boolean isChanged = false;
        for (int i = 0; i < 4; i++) {
            Vector<Integer> loc = new Vector<>(4);
            Vector<Integer> val = new Vector<>(4);
            for (int j = i * 4; j < 4 + i * 4; j++) {
                if (values[j] != 0) {
                    loc.add(j);
                    val.add(values[j]);
                }
            }
            if (loc.size() == 1) {
                if (loc.elementAt(0) != (3 + i * 4)) {
                    values[3 + i * 4] = val.elementAt(0);
                    values[loc.elementAt(0)] = 0;
                    isChanged = true;
                }
            } else if (loc.size() == 2) {
                if (val.elementAt(0).equals(val.elementAt(1))) {
                    values[loc.elementAt(0)] = 0;
                    values[loc.elementAt(1)] = 0;
                    values[3 + i * 4] = val.elementAt(0) * 2;
                    score += val.elementAt(0) * 2;
                    isChanged = true;
                } else {
                    values[loc.elementAt(0)] = 0;
                    values[loc.elementAt(1)] = 0;
                    values[3 + i * 4] = val.elementAt(1);
                    values[2 + i * 4] = val.elementAt(0);
                    if (!loc.elementAt(0).equals(2 + i * 4)) {
                        isChanged = true;
                    }
                }
            } else if (loc.size() == 3) {
                for (int j = 0; j < 3; j++) {
                    values[loc.elementAt(j)] = 0;
                }

                if (val.elementAt(2).equals(val.elementAt(1))) {
                    score += val.elementAt(2) * 2;
                    values[3 + i * 4] = val.elementAt(2) * 2;
                    values[2 + i * 4] = val.elementAt(0);
                    isChanged = true;
                } else if (val.elementAt(1).equals(val.elementAt(0))) {
                    score += val.elementAt(1) * 2;
                    values[3 + i * 4] = val.elementAt(2);
                    values[2 + i * 4] = val.elementAt(1) * 2;
                    isChanged = true;
                } else {
                    values[3 + i * 4] = val.elementAt(2);
                    values[2 + i * 4] = val.elementAt(1);
                    values[1 + i * 4] = val.elementAt(0);
                    if (!loc.elementAt(0).equals(1 + i * 4)) {
                        isChanged = true;
                    }
                }
            } else if (loc.size() == 4) {
                if (val.elementAt(3).equals(val.elementAt(2))) {
                    if (val.elementAt(1).equals(val.elementAt(0))) {
                        score = score + val.elementAt(3) * 2 + val.elementAt(1) * 2;
                        for (int j = 0; j < 4; j++) {
                            values[loc.elementAt(j)] = 0;
                        }

                        values[3 + i * 4] = val.elementAt(3) * 2;
                        values[2 + i * 4] = val.elementAt(1) * 2;
                    } else {
                        values[loc.elementAt(0)] = 0;
                        values[3 + i * 4] = val.elementAt(3) * 2;
                        values[2 + i * 4] = val.elementAt(1);
                        values[1 + i * 4] = val.elementAt(0);
                    }
                    isChanged = true;
                } else if (val.elementAt(2).equals(val.elementAt(1))) {
                    score += val.elementAt(2) * 2;
                    values[loc.elementAt(0)] = 0;
                    values[2 + i * 4] = val.elementAt(2) * 2;
                    values[1 + i * 4] = val.elementAt(0);
                    isChanged = true;
                } else if (val.elementAt(1).equals(val.elementAt(0))) {
                    score += val.elementAt(0) * 2;
                    values[loc.elementAt(0)] = 0;
                    values[1 + i * 4] = val.elementAt(0) * 2;
                    isChanged = true;
                }
            }
        }
        refreshEmptyItem();
        return isChanged;
    }

    private int getColorFor(int v) {
        if (v / 2 == 1) {
            return Color.parseColor(mycolor[0]);
        } else if (v / 4 == 1) {
            return Color.parseColor(mycolor[1]);
        } else if (v / 8 == 1) {
            return Color.parseColor(mycolor[2]);
        } else if (v / 16 == 1) {
            return Color.parseColor(mycolor[3]);
        } else if (v / 32 == 1) {
            return Color.parseColor(mycolor[4]);
        } else if (v / 64 == 1) {
            return Color.parseColor(mycolor[5]);
        } else {
            return Color.parseColor(mycolor[6]);
        }
    }


}

