package g.o.gotechpos;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class SplashScreenActivity extends Activity {
    private static Context context;

    private static boolean isNightModeEnabled = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences mPrefs =  PreferenceManager.getDefaultSharedPreferences(this);
        isNightModeEnabled = mPrefs.getBoolean("NIGHT_MODE", false);
        if (isNightModeEnabled) {
            setTheme(R.style.AppThemeDM);
        }
        else{
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        context=this;



        setContentView(R.layout.activity_splash_screen);

        Thread background = new Thread() {
            public void run() {
                try {
                    sleep(1800);

                    Intent intent = new Intent(SplashScreenActivity.this,Login.class);
                    startActivityForResult(intent,2);

                } catch (Exception e) {

                }
            }
        };

        // start thread
        background.start();




    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==1){
            startActivity(new Intent(this,Login.class));
        }
        else{
            finish();
        }
    }

    public static boolean isNightModeEnabled() {
        return isNightModeEnabled;
    }


    public static void setIsNightModeEnabled(boolean isNightModeEnabled) {
        SplashScreenActivity.isNightModeEnabled = isNightModeEnabled;
        SharedPreferences mPrefs =  PreferenceManager.getDefaultSharedPreferences(context);
        mPrefs.edit().putBoolean("NIGHT_MODE", isNightModeEnabled).commit();


    }

}
