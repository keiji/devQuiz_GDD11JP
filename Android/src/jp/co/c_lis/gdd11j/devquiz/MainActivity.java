package jp.co.c_lis.gdd11j.devquiz;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.apps.gddquiz.IQuizService;

public class MainActivity extends Activity {

    ServiceConnection mCon = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
        }
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("DevQUiz", "onServiceConnected");

            IQuizService s = IQuizService.Stub.asInterface(service);
            try {
                String code = s.getCode();
                Log.d("DevQUiz", code);
            } catch (RemoteException e) {
                Log.d("DevQUiz", "RemoteException", e);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("DevQUiz", "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Intent service = new Intent(IQuizService.class.getName());
        bindService(service, mCon, Context.BIND_AUTO_CREATE);
        
    }
}