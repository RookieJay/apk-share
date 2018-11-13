package com.zjc.apkshare;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHodler> {

    private List<ResolveInfo> apps;
    private PackageManager pm;
    private Context context;
    private int clickPosition = -1;
    private List<ResolveInfo> mSelectedApps = new ArrayList<>();

    public MyAdapter(List<ResolveInfo> apps, PackageManager pm, Context context) {
        this.apps = apps;
        this.pm = pm;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHodler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new MyViewHodler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHodler holder, int position) {
        final ResolveInfo app = apps.get(position);
        holder.rbSelect.setClickable(false);
        holder.rbSelect.setFocusable(false);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickPosition = holder.getLayoutPosition();
                if (holder.rbSelect.isChecked()) {
                    holder.rbSelect.setChecked(false);
                    if (mSelectedApps.contains(app)) {
                        mSelectedApps.remove(app);
                    }
                } else {
                    holder.rbSelect.setChecked(true);
                    mSelectedApps.add(app);
                }
            }
        });
        holder.rbSelect.setChecked(position == clickPosition);
        CharSequence appName = app.loadLabel(pm);
        Drawable appIcon = app.loadIcon(pm);
        holder.tvAppName.setText(appName);
        Glide.with(holder.ivIcon).load(appIcon).into(holder.ivIcon);
        String appDir = null;
        try {
            appDir = pm.getApplicationInfo(app.activityInfo.packageName, 0).sourceDir;
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        appDir = "file://" + appDir;
//        Log.d("appDir", appDir);
        Uri uri = Uri.parse(appDir);
//        Log.d("uri", uri.getPath());
        String size = FileSizeUtil.getAutoFileOrFilesSize(uri.getPath());
//        Log.d("apk大小", size);
        holder.tvAppSize.setText(size);
        ApplicationInfo appInfo = null;
        try {
            appInfo = pm.getApplicationInfo(app.activityInfo.packageName, 0);
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String appFile = appInfo.sourceDir;
        long installedTime = new File(appFile).lastModified();
        DateUtils dateUtil = new DateUtils(installedTime);
        String realTime = dateUtil.format(DateUtils.DEFAULT_DATETIME_PATTERN);
//        Log.d("realtime", realTime);
        holder.tvAppInsTime.setText(realTime);
    }


    /**
     * 读取文件创建时间
     */
    public static void getCreateTime(String filePath){
        String strTime = null;
        try {
            Process p = Runtime.getRuntime().exec("cmd /C dir "
                    + filePath
                    + "/tc" );
            InputStream is = p.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while((line = br.readLine()) != null){
                if(line.endsWith(".apk")){
                    strTime = line.substring(0,17);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("创建时间    " + strTime);
        //输出：创建时间   2009-08-17  10:21
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public List<ResolveInfo> getSelectedItem() {
        return mSelectedApps;
    }

    public class MyViewHodler extends RecyclerView.ViewHolder {

        RadioButton rbSelect;
        ImageView ivIcon;
        TextView tvAppName;
        TextView tvAppSize;
        TextView tvAppInsTime;

        public MyViewHodler(@NonNull View itemView) {
            super(itemView);
            this.rbSelect = itemView.findViewById(R.id.radio_button);
            this.ivIcon = itemView.findViewById(R.id.iv_icon);
            this.tvAppName = itemView.findViewById(R.id.tv_app_name);
            this.tvAppSize = itemView.findViewById(R.id.tv_app_size);
            this.tvAppInsTime = itemView.findViewById(R.id.tv_app_install_time);
        }
    }
}
