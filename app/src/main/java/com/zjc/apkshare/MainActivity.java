package com.zjc.apkshare;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private PackageManager pm;
    private List<ResolveInfo> mApps;
    private MyAdapter myAdapter;
    private Toolbar mToolbar;
    private TextView tvBarTitle;
    private TextView tvConfirm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initTitle();
        mRecyclerView = findViewById(R.id.recycler_view);
        pm = getPackageManager();
        initApp();
    }

    private void initTitle() {
        mToolbar = findViewById(R.id.toolbar);
        tvBarTitle = findViewById(R.id.bar_title);
        tvConfirm = findViewById(R.id.bar_right);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
        }
        tvBarTitle.setText("ApkShare");
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ResolveInfo> apps = myAdapter.getSelectedItem();
                if (null == apps || apps.size() == 0) {
                    Toast.makeText(MainActivity.this, "please choose the apps you'd like to share", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(MainActivity.this, "您准备分享:"+apps.size()+"个应用", Toast.LENGTH_SHORT).show();
                ArrayList<Uri> uris = new ArrayList<>();
                for (int i=0; i<apps.size(); i++) {
                    ResolveInfo app = apps.get(i);
                    String appDir = null;
                    try {
                        appDir = getPackageManager().getApplicationInfo(app.activityInfo.packageName, 0).sourceDir;
                    }
                    catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    appDir = "file://" + appDir;
                    Log.d("appDir", appDir);
                    Uri uri = Uri.parse(appDir);
                    uris.add(uri);
                }
                /**
                 * 重点！！！圈起来要考
                 * 解决android.os.FileUriExposedException的代码，一般加在onCreate()中
                 */
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    builder.detectFileUriExposure();
                }

                boolean multiple = uris.size() > 1;
                Intent intent = new Intent(multiple ? Intent.ACTION_SEND_MULTIPLE
                        : Intent.ACTION_SEND);
                intent.setType("*/*");
                if (multiple) {
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                } else {
                    intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
                }
                startActivity(Intent.createChooser(intent, "发送"));
            }
        });
    }

    private void initApp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 获取android设备的应用列表
                Intent intent = new Intent(Intent.ACTION_MAIN); // 动作匹配
                intent.addCategory(Intent.CATEGORY_LAUNCHER); // 类别匹配
                mApps = pm.queryIntentActivities(intent, 0);
                // 排序
                Collections.sort(mApps, new Comparator<ResolveInfo>() {

                    @Override
                    public int compare(ResolveInfo a, ResolveInfo b) {
                        // 排序规则
                        PackageManager pm = getPackageManager();
                        return String.CASE_INSENSITIVE_ORDER.compare(a.loadLabel(pm)
                                                                      .toString(), b.loadLabel(pm).toString()); // 忽略大小写
                    }
                });
                loadData();
            }
        }).start();

    }

    private void loadData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myAdapter = new MyAdapter(mApps, pm, MainActivity.this);
                LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this);
                mRecyclerView.setLayoutManager(manager);
                mRecyclerView.addItemDecoration(new DividerItemDecoration(MainActivity.this, DividerItemDecoration.VERTICAL));
                mRecyclerView.setAdapter(myAdapter);
            }
        });
    }

}
