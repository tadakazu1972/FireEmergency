package tadakazu1972.fireemergency;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PersonalActivity extends AppCompatActivity {
    protected PersonalActivity mActivity = null;
    protected View mView = null;
    //EditText, Spinner, CheckedTextViewの変数確保
    private EditText mEdtPersonalId = null;
    private Spinner mSpnPersonalClass = null;
    private EditText mEdtPersonalAge = null;
    private Spinner mSpnPersonalDepartment = null;
    private EditText mEdtPersonalName = null;
    private CheckedTextView mCtvPersonalEngineer = null;
    private CheckedTextView mCtvPersonalParamedic = null;
    //非常参集職員情報　保存用
    private String personalId = "";
    private String personalClass = "";
    private String personalAge = null;
    private String personalDepartment = null;
    private String personalName = null;
    private Boolean personalEngineer = null;
    private Boolean personalParamedic = null;
    //SharedPreferences
    private SharedPreferences sp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mActivity = this;
        mView = this.getWindow().getDecorView();
        setContentView(R.layout.activity_personal);

        //SharedPreferencesインスタンス取得
        sp = getPreferences(Context.MODE_PRIVATE);

        //EditText, Button 初期化
        initViews();
    }

    private void initViews(){
        //職員番号EditText
        mEdtPersonalId = (EditText)findViewById(R.id.personalId);
        //保存しているデータを読み込んでEditTextにセット
        personalId = sp.getString("personalId", "");
        mEdtPersonalId.setText(personalId);

        //階級Spinner
        mSpnPersonalClass = (Spinner)findViewById(R.id.personalClass);
        //保存しているデータを読み込んでスピナーにセット
        personalClass = sp.getString("personalClass", "");
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.fireClass, R.layout.custom_spinner_item);
        mSpnPersonalClass.setAdapter(adapter1);
        if (personalClass != null){
            int spinnerPosition = adapter1.getPosition(personalClass);
            mSpnPersonalClass.setSelection(spinnerPosition);
        }
        //Spinnerの選択リスナー設定
        mSpnPersonalClass.setOnItemSelectedListener(new OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                personalClass = (String)mSpnPersonalClass.getSelectedItem();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){
                //何もしない
            }
        });

        //年齢EditText
        mEdtPersonalAge = (EditText)findViewById(R.id.personalAge);
        //保存しているデータを読み込んでEditTextにセット
        personalAge = sp.getString("personalAge", "");
        mEdtPersonalAge.setText(personalAge);

        //所属Spinner
        mSpnPersonalDepartment = (Spinner)findViewById(R.id.personalDepartment);
        //保存しているデータを読み込んでスピナーにセット
        personalDepartment = sp.getString("personalDepartment", "");
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.firestation, R.layout.custom_spinner_item);
        mSpnPersonalDepartment.setAdapter(adapter2);
        if (personalDepartment != null){
            int spinnerPosition = adapter2.getPosition(personalDepartment);
            mSpnPersonalDepartment.setSelection(spinnerPosition);
        }
        //Spinnerの選択リスナー設定
        mSpnPersonalDepartment.setOnItemSelectedListener(new OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                personalDepartment = (String)mSpnPersonalDepartment.getSelectedItem();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){
                //何もしない
            }
        });

        //氏名EditText
        mEdtPersonalName = (EditText)findViewById(R.id.personalName);
        //保存しているデータを読み込んでEditTextにセット
        personalName = sp.getString("personalName", "");
        mEdtPersonalName.setText(personalName);

        //資格　機関員CheckedTextView
        mCtvPersonalEngineer = (CheckedTextView)findViewById(R.id.personalEngineer);
        //保存しているデータを読み込んでCheckedTextViewにセット
        personalEngineer = sp.getBoolean("personalEngineer", false);
        mCtvPersonalEngineer.setChecked(personalEngineer);
        //CheckedTextViewのリスナー設定
        mCtvPersonalEngineer.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mCtvPersonalEngineer.setChecked(!mCtvPersonalEngineer.isChecked());
            }
        });

        //資格　救命士
        mCtvPersonalParamedic = (CheckedTextView)findViewById(R.id.personalParamedic);
        //保存しているデータを読み込んでCheckedTextViewにセット
        personalParamedic = sp.getBoolean("personalParamedic", false);
        mCtvPersonalParamedic.setChecked(personalParamedic);
        //CheckedTextViewのリスナー設定
        mCtvPersonalParamedic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mCtvPersonalParamedic.setChecked(!mCtvPersonalParamedic.isChecked());
            }
        });

        //登録ボタン
        mView.findViewById(R.id.btnPersonalSave).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //職員番号
                personalId = mEdtPersonalId.getText().toString();
                sp.edit().putString("personalId", personalId).apply();
                //階級
                sp.edit().putString("personalClass", personalClass).apply();
                //年齢
                personalAge = mEdtPersonalAge.getText().toString();
                sp.edit().putString("personalAge", personalAge).apply();
                //所属
                sp.edit().putString("personalDepartment", personalDepartment).apply();
                //名前
                personalName = mEdtPersonalName.getText().toString();
                sp.edit().putString("personalName", personalName).apply();
                //資格　機関員
                personalEngineer = mCtvPersonalEngineer.isChecked();
                sp.edit().putBoolean("personalEngineer", personalEngineer).apply();
                //資格　救命士
                personalParamedic = mCtvPersonalParamedic.isChecked();
                sp.edit().putBoolean("personalParamedic", personalParamedic).apply();

                //メッセージ表示
                Toast.makeText(mActivity, "職員情報を登録しました", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

