package tadakazu1972.fireemergency;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

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
    //参集先　消防局or消防署の選択結果判別用
    private Integer mIndex = 0; //0:消防局 1:消防署

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

        //戻るボタン
        mView.findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //基礎データ入力画面へ戻る
                Intent intent = new Intent(mActivity, DataActivity.class);
                startActivity(intent);
            }
        });

        //参集先　到着ボタン
        mView.findViewById(R.id.btnNext).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //消防局　消防署　選択ダイアログへ遷移
                KyokuSyoSelectDialog();
            }
        });
    }

    //消防局　消防署　選択ダイアログ
    private void KyokuSyoSelectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_kyokusyo, (ViewGroup) findViewById(R.id.dlgKyokuSyo));
        //消防局ボタン　クリックリスナー設定
        layout.findViewById(R.id.btnKyoku).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //消防局選択　ダイアログへ遷移
                mIndex = 0;
                SansyusyoSelectDialog(mIndex);
                Toast.makeText(mActivity, "消防局", Toast.LENGTH_SHORT).show();
            }
        });
        //消防署ボタン　クリックリスナー設定
        layout.findViewById(R.id.btnSyo).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //消防署選択　ダイアログへ遷移
                mIndex = 1;
                SansyusyoSelectDialog(mIndex);
                Toast.makeText(mActivity, "消防署", Toast.LENGTH_SHORT).show();
            }
        });
        //セット
        builder.setView(layout);
        builder.setNegativeButton("閉じる", null);
        builder.setCancelable(false);
        builder.create();
        builder.show();
    }

    //消防署選択
    private void SansyusyoSelectDialog(Integer index){
        String resName = "Kyoku"; //デフォルトで消防局に設定しておく
        //消防局を選択
        if (index == 0){
            resName = "Kyoku";
        } else if (index == 1) {
            resName = "Syo";
        }
        // res/values/arrays.xmlにKyokuまたSyoとして消防局・消防署を設定している。それを読み込む。
        int resourceId = getResources().getIdentifier(resName, "array", getPackageName());
        //取得した配列リソースIDを文字列配列に格納
        final String[] mList = getResources().getStringArray(resourceId);
        GridView gridView = new GridView(this);
        gridView.setNumColumns(2);
        gridView.setHorizontalSpacing(80);
        gridView.setVerticalSpacing(40);
        gridView.setPadding(80,20,80,20);
        gridView.setAdapter(new ArrayAdapter<>(this, R.layout.grid_kyokusyo, mList));
        //gridView.setNumColumns(2);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>parent, View view, int position, long id){
                String s = mList[position];
                Toast.makeText(mActivity, s, Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■参集先　メール送信\n   必ず参集先に到着してから送信");
        builder.setView(gridView);
        builder.setPositiveButton("メール送信", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                //基礎データ　勤務消防署、津波避難消防署とのチェック判定へ
                
            }
        });
        builder.setNegativeButton("戻る", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }
}

