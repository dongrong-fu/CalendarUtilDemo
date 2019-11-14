package com.example.calendarutildemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private boolean mIsCheckNow;
    public static final String TITLE = "日历title";
    public static final String DESCRIPTION = "日历Content";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv_set_calendar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsCheckNow = true;
                fetchPermission(99);
            }
        });
        findViewById(R.id.tv_delete_calendar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsCheckNow = false;
                fetchPermission(99);
            }
        });
    }

    public void fetchPermission(int requestCode) {
        int checkSelfPermission;
        try {
            checkSelfPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return;
        }

        if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
            // 如果没有授权，就请求用户授权
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_CALENDAR,
                    Manifest.permission.READ_CALENDAR}, requestCode);
        }else{
            Observable.create(new ObservableOnSubscribe<Boolean>() {
                @Override
                public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                    if (mIsCheckNow) {
                        if (!CalendarReminderUtils.isNoCursor(MainActivity.this)) {
                            MainActivity.this.addCalender();
                            e.onNext(true);
                        } else {
                            e.onNext(false);
                        }
                    } else {
                        if (!CalendarReminderUtils.isNoCursor(MainActivity.this)) {
                            MainActivity.this.closeCalender();
                            e.onNext(true);
                        } else {
                            e.onNext(false);
                        }
                    }
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean saveResult) throws Exception {
                            if (!saveResult) {
                                CalendarPermissionUtil.showWaringDialog(MainActivity.this);
                            }
                        }
                    });
        }

    }

    private void closeCalender(){
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                CalendarReminderUtils.deleteCalendarEvent(MainActivity.this, TITLE);
                e.onNext(!CalendarReminderUtils.isNoCursor(MainActivity.this.getApplicationContext()) && CalendarReminderUtils.isNoCalendarData(MainActivity.this.getApplicationContext(), TITLE));
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean saveResult) throws Exception {
                        if (saveResult) { //删除成功
                            Toast.makeText(MainActivity.this.getApplicationContext(), "删除成功！", Toast.LENGTH_SHORT).show();
                        } else {
                            CalendarPermissionUtil.showWaringDialog(MainActivity.this);
                        }
                    }
                });
    }

    private void addCalender(){
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                CalendarReminderUtils.deleteCalendarEvent(MainActivity.this, TITLE);
                String[] dayList = new String[]{"20191114", "20191115", "20191116", "20191117", "20191118", "20191119", "20191120"};
                // 用户同意的授权请求
                for (String day : dayList) {
                    long dayms = getMsFromDayTime(Integer.parseInt(day.substring(0, 4)), Integer.parseInt(day.substring(4, 6)) - 1, Integer.parseInt(day.substring(6, 8)), 12, 00);
                    CalendarReminderUtils.addCalendarEvent(MainActivity.this, TITLE, DESCRIPTION, dayms, 0);
//                    if (dayms > System.currentTimeMillis()) {
//                        CalendarReminderUtils.addCalendarEvent(MainActivity.this, TITLE, DESCRIPTION, dayms, 0);
//                    }
                }
                e.onNext(!CalendarReminderUtils.isNoCalendarData(MainActivity.this.getApplicationContext(), TITLE));
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean saveResult) throws Exception {
                        if (saveResult) {
                            Toast.makeText(MainActivity.this.getApplicationContext(), "日历设置成功！", Toast.LENGTH_SHORT).show();
                        } else {
                            CalendarPermissionUtil.showWaringDialog(MainActivity.this);
                        }
                    }
                });
    }

    public static long getMsFromDayTime(int year,int month,int day,int hour,int minute){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR,year);
        cal.set(Calendar.MONTH,month);
        cal.set(Calendar.DAY_OF_MONTH,day);
        cal.set(Calendar.HOUR_OF_DAY,hour);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 99) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Observable.create(new ObservableOnSubscribe<Boolean>() {
                    @Override
                    public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                        if (mIsCheckNow) {
                            if (!CalendarReminderUtils.isNoCursor(MainActivity.this)) {
                                MainActivity.this.addCalender();
                                e.onNext(true);
                            } else {
                                e.onNext(false);
                            }
                        } else {
                            if (!CalendarReminderUtils.isNoCursor(MainActivity.this)) {
                                MainActivity.this.closeCalender();
                                e.onNext(true);
                            } else {
                                e.onNext(false);
                            }
                        }
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean saveResult) throws Exception {
                                if (!saveResult) {
                                    CalendarPermissionUtil.showWaringDialog(MainActivity.this);
                                }
                            }
                        });
            } else{
                CalendarPermissionUtil.showWaringDialog(this);
            }
        }
    }

}
