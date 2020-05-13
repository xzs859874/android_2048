package com.a2048;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class RankActivity extends AppCompatActivity {

    private ContentResolver resolver;
    private Uri uri;
    private Cursor cursor;
    private List<RankBean> ranklist = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_rank);

        resolver = this.getContentResolver();
        uri = Uri.parse("content://com.example.mydb.MyProvider/rank");
        initrank();
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.backmenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.back_home:
                finish();
                break;
            case R.id.clear_all:
                resolver.delete(uri,"_id>?",new String[]{"0"});
                ranklist.clear();
                initrank();
                break;
        }
        return true;
    }

    private  void  initrank(){

        RankAdapter adapter = new RankAdapter(RankActivity.this,R.layout.rank_item,ranklist);
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        cursor = resolver.query(uri, new String[]{"_id","name","score"}, null, null, "score DESC");
        if(cursor != null) {
            while (cursor.moveToNext()) {
                //getPosition获取的是0开始
                Integer num = cursor.getPosition()+1;
                Integer id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                Integer score = cursor.getInt(cursor.getColumnIndexOrThrow("score"));
                RankBean rankBean = new RankBean(id,name,score,num);
                ranklist.add(rankBean);
            }
            cursor.close();
        }

    }
}



//    public List findAll(){
//        cursor = resolver.query(uri,new String[]{"id,name,score"},null,null,"score desc");
//        if(cursor != null) {
//            while (cursor.moveToNext()) {
//                int id = cursor.getInt(0);
//                String name = cursor.getString(1);
//                int score = cursor.getInt(2);
//
//                map.put("id",id);
//                map.put("name",name);
//                map.put("score",score);
//                list1.add(map);
//                System.out.println(id+ "---" + name+">>>>"+score);
//            }
//        }
//        cursor.close();
//        return list1;
//    }


//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        listView = (ListView) findViewById(R.id.list_view);
//    }
//
//    public void test(View view) {
//        /**
//         * 可以想象客户端访问服务器，需要需要用到协议HTTP
//         * 而想访问ContentProvider，需要ContentResolver
//         */
//        ContentResolver contentProvider = getContentResolver();
//
//        /**
//         * 可以想象访问服务器，需要这样拼接访问地址 http://
//         * 而想访问ContentProvider，需要这样拼接访问地址 content://
//         * 必须拿到暴露的授权authorities="autho.prov.cp.MyContentProvider" 进行拼接
//         */
//        uri = Uri.parse("content://com.example.mydb.MyProvider");
//
//        // 查询
//        contentProvider.query(uri, null, null, null, null, null);
//
//        // 增加
//        // contentProvider.insert(uri, null);
//
//        // 修改
//        // contentProvider.update(uri, null, null, null);
//
//        // 删除
//        // contentProvider.delete(uri, null, null);
//    }
//
//    /**
//     * 查询
//     */
//    public void findAll(View view) {
//        cursor = contentResolver.query(uri,
//                new String[]{"_id", "name", "age"},
//                null, null
//                , null, null);
//
//        /**
//         * 使用SimpleCursorAdapter 适配器
//         */
//        SimpleCursorAdapter simpleCursorAdapter = new
//                SimpleCursorAdapter(RankActivity.this, // 上下文
//                R.layout.rank_item, // Item布局
//                cursor, // Cursor 查询出来的游标 这里面有数据库里面的数据
//                new String[]{"_id", "name", "score"}, // 从哪里来，指的是 查询出数据库列名
//                new int[]{R.id.rank_id, R.id.rank_name, R.id.rank_score}, // 到哪里去，指的是，把查询出来的数据，赋值给Item布局 的控件
//                SimpleCursorAdapter.NO_SELECTION);
//
//        // 给ListView设置使用SimpleCursorAdapter适配器
//        listView.setAdapter(simpleCursorAdapter);
//
//        // 注意：在数据展示完成后，不要关闭游标， 在Activity结束后在关闭cursor.close();
//    }
//
//    /**
//     * 增加
//     */
//    public void insert(View view) {
//
//
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (cursor != null) {
//            cursor.close();
//        }
//    }
//}
