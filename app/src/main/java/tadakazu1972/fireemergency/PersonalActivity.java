package tadakazu1972.fireemergency;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class PersonalActivity extends AppCompatActivity {
    protected PersonalActivity mActivity = null;
    protected View mView = null;
    private Spinner mSpn1 = null;
    private Spinner mSpn2 = null;
    //非常参集職員情報　保存用
    private String personalId = null;
    private String personalClass = null;
    private String personalAge = null;
    private String personalDepartment = null;
    private String personalName = null;
    private Boolean personalEngineer = null;
    private Boolean personalParamedic = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mActivity = this;
        mView = this.getWindow().getDecorView();
        setContentView(R.layout.activity_personal);

        //ボタン初期化
        initButtons();
    }

    private void initButtons(){

    }
}

