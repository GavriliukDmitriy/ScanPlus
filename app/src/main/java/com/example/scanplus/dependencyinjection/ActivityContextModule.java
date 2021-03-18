package com.example.scanplus.dependencyinjection;


import android.app.Activity;
import android.content.Context;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class ActivityContextModule {

    private final Context context;

    ActivityContextModule(Activity context){
        this.context = context;
    }

    @Named("activity_context")
    @ApplicationScope
    @Provides
    public Context context(){
        return context;
    }
}
}
