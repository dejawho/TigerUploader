package com.tiger.tigeruploader;

import android.app.Application;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(formUri = "https://collector.tracepot.com/23bf8960")
public class TigerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
    }
}
