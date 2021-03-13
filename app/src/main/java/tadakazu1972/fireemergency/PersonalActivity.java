package tadakazu1972.fireemergency;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class PersonalActivity extends AppCompatActivity {
    protected PersonalActivity mActivity = null;
    protected View mView = null;
    //EditText, Spinner, CheckedTextViewの変数確保
    private EditText mEdtPersonalId = null;
    private Spinner mSpnPersonalClass = null;
    private Spinner mSpnPersonalAge = null;
    private Spinner mSpnPersonalDepartment = null;
    private EditText mEdtPersonalName = null;
    private Spinner mSpnPersonalRide = null;
    private CheckedTextView mCtvPersonalEngineer = null;
    private CheckedTextView mCtvPersonalParamedic = null;
    //非常参集職員情報　保存用
    private String personalId = "";
    private String personalClass = "";
    private String personalAge = null;
    private String personalDepartment = null;
    private String personalName = null;
    private String personalRide = null;
    private Boolean personalEngineer = null;
    private Boolean personalParamedic = null;
    //SharedPreferences
    protected String packageName = null;
    private SharedPreferences sp = null;
    //参集先　消防局or消防署の選択結果判別用
    private Integer mIndex = 0; //0:消防局 1:消防署
    protected String mMainStation;
    protected String mTsunamiStation;
    //メール送信用
    private String[] mailAddress = {"pa0035@city.osaka.lg.jp"}; //初期設定　１個でも配列でないとダメ
    private String subject = "@警防";
    //参集先ダイアログで使う消防署とメールアドレスの辞書型：ボタンタップでStringしか得られないから配列の引数指定ができない、よって辞書型を使う
    private Map<String, String> syoMailAddress = null;
    //参集先を選択したかどうか
    protected Boolean isSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mActivity = this;
        mView = this.getWindow().getDecorView();
        setContentView(R.layout.activity_personal);

        //SharedPreferencesインスタンス取得
        packageName = getPackageName();
        sp = getSharedPreferences(packageName + "_preferences", MODE_PRIVATE);

        //基礎データで登録したデータ呼び出し
        loadData();

        //EditText, Button 初期化
        initViews();

        //署のメールアドレス辞書　初期化
        syoMailAddress = new HashMap<String, String>();
        // res/values/arrays.xmlにSyoとして消防署とメールアドレスを設定している。それを読み込む。
        int resourceId = getResources().getIdentifier("Syo", "array", getPackageName());
        int resourceId2 = getResources().getIdentifier("SyoAddress", "array", getPackageName());
        //取得した配列リソースIDを文字列配列に格納
        final String[] mList = getResources().getStringArray(resourceId);
        final String[] addressArray = getResources().getStringArray(resourceId2);
        //辞書に格納
        for (int i=0; i < mList.length; i++){
            syoMailAddress.put(mList[i], addressArray[i]);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        //基礎データを変更してActivity復帰した際に反映させないと前のままなので
        loadData();
    }

    //基礎データ読み込み
    private void loadData(){
        //勤務消防署
        mMainStation = sp.getString("mainStation","消防局"); // 第２引数はkeyが存在しない時に返す初期値
        //大津波・津波警報時指定署
        mTsunamiStation = sp.getString("tsunamiStation", "消防局"); // 第２引数はkeyが存在しない時に返す初期値
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

        //年齢Spinner
        mSpnPersonalAge = (Spinner)findViewById(R.id.personalAge);
        //保存しているデータを読み込んでスピナーにセット
        personalAge = sp.getString("personalClass", "");
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this, R.array.age, R.layout.custom_spinner_item);
        mSpnPersonalAge.setAdapter(adapter3);
        if (personalAge != null){
            int spinnerPosition = adapter3.getPosition(personalAge);
            mSpnPersonalAge.setSelection(spinnerPosition);
        }
        //Spinnerの選択リスナー設定
        mSpnPersonalAge.setOnItemSelectedListener(new OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                personalAge = (String)mSpnPersonalAge.getSelectedItem();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){
                //何もしない
            }
        });

        //所属Spinner
        mSpnPersonalDepartment = (Spinner)findViewById(R.id.personalDepartment);
        //保存しているデータを読み込んでスピナーにセット
        personalDepartment = sp.getString("personalDepartment", "");
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.personalDepartment, R.layout.custom_spinner_item);
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
        trimSpaces();
        mEdtPersonalName.setText(personalName);

        //現在の乗組Spinner
        mSpnPersonalRide = (Spinner)findViewById(R.id.personalRide);
        //保存しているデータを読み込んでスピナーにセット
        personalRide = sp.getString("personalRide", "");
        ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource(this, R.array.personalRide, R.layout.custom_spinner_item);
        mSpnPersonalRide.setAdapter(adapter4);
        if (personalRide != null){
            int spinnerPosition = adapter4.getPosition(personalRide);
            mSpnPersonalRide.setSelection(spinnerPosition);
        }
        //Spinnerの選択リスナー設定
        mSpnPersonalRide.setOnItemSelectedListener(new OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                personalRide = (String)mSpnPersonalRide.getSelectedItem();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){
                //何もしない
            }
        });

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
                sp.edit().putString("personalAge", personalAge).apply();
                //所属
                sp.edit().putString("personalDepartment", personalDepartment).apply();
                //名前
                personalName = mEdtPersonalName.getText().toString();
                //空白を削除して
                trimSpaces();
                //EditTextに反映させる そうしないと削除したことが伝わらない
                mEdtPersonalName.setText(personalName);
                sp.edit().putString("personalName", personalName).apply();
                //現在の乗組
                sp.edit().putString("personalRide", personalRide).apply();
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
                SansyusyoSelectDialog0();
            }
        });
    }


    //氏名EditTextの間にスペースがあったら削除
    private void trimSpaces(){
        //半角スペース
        personalName = personalName.replaceAll(" ","");
        //全角スペース
        personalName = personalName.replaceAll("　","");
    }


    //消防署選択
    private void SansyusyoSelectDialog0(){
        //３つのボタンどれも押していない初期状態
        isSelected = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_sansyusyo, (ViewGroup) findViewById(R.id.dlgSansyusyo));
        //ボタンに基礎データの勤務所、指定参集署のテキストを設定
        final Button mBtnKinmusyo = layout.findViewById(R.id.btnKinmusyo);
        final Button mBtnShiteisansyusyo = layout.findViewById(R.id.btnShiteisansyusyo);
        final Button mBtnAnother = layout.findViewById(R.id.btnAnother);
        //保存しているデータを読み込んでEditTextにセット
        mBtnKinmusyo.setText(mMainStation);
        mBtnShiteisansyusyo.setText(mTsunamiStation);
        //勤務署ボタン　クリックリスナー設定
        mBtnKinmusyo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //セレクター切替
                v.setSelected(!v.isSelected());
                mBtnShiteisansyusyo.setSelected(false);
                mBtnAnother.setSelected(false);
                if (v.isSelected()){
                    isSelected = true;
                    //勤務消防署が消防局の登録だった場合は、課の選択をするためmIndex:0で書房局ダイアログへ遷移
                    if (mMainStation.equals("消防局")) {
                        mIndex = 0;
                        SansyusyoSelectDialog(mIndex);
                    } else {
                        //勤務消防署のメールアドレス確保しに行く
                        pickupSyoMailAddress(mMainStation);
                    }
                }
            }
        });
        //指定参集署ボタン　クリックリスナー設定
        mBtnShiteisansyusyo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //セレクター切替
                v.setSelected(!v.isSelected());
                mBtnKinmusyo.setSelected(false);
                mBtnAnother.setSelected(false);
                if (v.isSelected()){
                    isSelected = true;
                    //勤務消防署が消防局の登録だった場合は、課の選択をするためmIndex:0で書房局ダイアログへ遷移
                    if (mTsunamiStation.equals("消防局")) {
                        mIndex = 0;
                        SansyusyoSelectDialog(mIndex);
                    } else {
                        //指定参集所のメールアドレス確保しに行く
                        pickupSyoMailAddress(mTsunamiStation);
                    }
                }
            }
        });
        //その他ボタン　クリックリスナー設定
        mBtnAnother.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //セレクター切替
                v.setSelected(!v.isSelected());
                mBtnKinmusyo.setSelected(false);
                mBtnShiteisansyusyo.setSelected(false);
                if (v.isSelected()){
                    isSelected = true;
                    //消防局　消防署　選択ダイアログへ遷移
                    KyokuSyoSelectDialog();
                    //Toast.makeText(mActivity, "消防署", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //セット
        builder.setView(layout);
        builder.setTitle("■参集先　メール送信\n   必ず参集先に到着してから送信");
        //メール送信ボタンタップ
        builder.setPositiveButton("メール送信", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                //参集先をタップしていない場合は警告出してメール送信に進ませない
                if (!isSelected){
                    //参集先を指定していないのでメールアドレスがnull
                    NullMailAddressDialog();
                } else {
                    //メール送信処理へ
                    sendMail();
                }
            }
        });
        builder.setNegativeButton("戻る", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void pickupSyoMailAddress(String _syo){
        //勤務消防署もしくは指定参集署のメールアドレスを格納
        //宛先のメアドも配列から確保、[0]にしているのは送る変数が配列でないとダメだからその１つ目の意味。
        mailAddress[0] = syoMailAddress.get(_syo) + "@city.osaka.lg.jp";
        //件名を「＠」と参集先名
        subject = "@" + _syo;
        Toast.makeText(mActivity, _syo+":"+mailAddress[0], Toast.LENGTH_SHORT).show();
    }

    //参集先指定していないのでメールアドレスがnullアラートダイアログ
    private void NullMailAddressDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_nullmailaddress, (ViewGroup) findViewById(R.id.dlgNullMailAddress));
        //セット
        builder.setView(layout);
        builder.setPositiveButton("はい", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                //参集先選択へ戻る
                SansyusyoSelectDialog0();
            }
        });
        builder.setCancelable(false);
        builder.create();
        builder.show();
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
                //Toast.makeText(mActivity, "消防局", Toast.LENGTH_SHORT).show();
            }
        });
        //消防署ボタン　クリックリスナー設定
        layout.findViewById(R.id.btnSyo).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //消防署選択　ダイアログへ遷移
                mIndex = 1;
                SansyusyoSelectDialog(mIndex);
                //Toast.makeText(mActivity, "消防署", Toast.LENGTH_SHORT).show();
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
        String AddressName = "KyokuAddress";
        //消防局を選択
        if (index == 0){
            resName = "Kyoku";
            AddressName = "KyokuAddress";
        } else if (index == 1) {
            resName = "Syo";
            AddressName = "SyoAddress";
        }
        // res/values/arrays.xmlにKyokuまたSyoとして消防局・消防署を設定している。それを読み込む。
        int resourceId = getResources().getIdentifier(resName, "array", getPackageName());
        int resourceId2 = getResources().getIdentifier(AddressName, "array", getPackageName());
        //取得した配列リソースIDを文字列配列に格納
        final String[] mList = getResources().getStringArray(resourceId);
        final String[] addressArray = getResources().getStringArray(resourceId2);
        GridView gridView = new GridView(this);
        gridView.setNumColumns(2);
        gridView.setHorizontalSpacing(80);
        gridView.setVerticalSpacing(40);
        gridView.setPadding(80,20,80,20);
        gridView.setAdapter(new ArrayAdapter<>(this, R.layout.grid_kyokusyo, mList));
        //参集先をタップされたら
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>parent, View view, int position, long id){
                //セレクター切替
                view.setSelected(!view.isSelected());
                //参集先の文字列を配列から確保
                String sansyusaki = mList[position];
                //宛先のメアドも配列から確保、[0]にしているのは送る変数が配列でないとダメだからその１つ目の意味。
                mailAddress[0] = addressArray[position] + "@city.osaka.lg.jp";
                //件名を「＠」と参集先名
                subject = "@" + sansyusaki;
                Toast.makeText(mActivity, sansyusaki+":"+mailAddress[0], Toast.LENGTH_SHORT).show();
                //基礎データ　勤務消防署、津波避難消防署とのチェック判定へ
                CheckSansyusaki(sansyusaki);
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■参集先　メール送信\n   必ず参集先に到着してから送信");
        builder.setView(gridView);
        //メール送信ボタンタップ
        builder.setPositiveButton("メール送信", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                sendMail();
            }
        });
        builder.setNegativeButton("戻る", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //基礎データで登録されている「勤務消防署」「津波警報時(震度5弱以上)の参集指定署」のどちらにも該当しない場合、アラート表示
    private void CheckSansyusaki(String sansyusaki){
        //判定
        //参集先が消防局の場合
        if (mIndex==0){
            if (!mMainStation.equals("消防局") && !mTsunamiStation.equals("消防局")){
                if (!mMainStation.equals("訓練センター") && !mTsunamiStation.equals("訓練センター")){
                    //アラート
                    //String s = "参集先:" + sansyusaki + ", mainStation:" + mMainStation + ", tsunamiStation:" + mTsunamiStation;
                    //Toast.makeText(mActivity, s + "違うけど大丈夫？", Toast.LENGTH_SHORT).show();
                    AnotherSansyusakiDialog();
                }
            }
        }
        //参集先が消防署の場合
        if (mIndex==1){
            if (!sansyusaki.equals(mMainStation) && !sansyusaki.equals(mTsunamiStation)){
                //アラート
                //String s = "参集先:" + sansyusaki + ", mainStation:" + mMainStation + ", tsunamiStation:" + mTsunamiStation;
                //Toast.makeText(mActivity, s + "違うけど大丈夫？", Toast.LENGTH_SHORT).show();
                AnotherSansyusakiDialog();
            }
        }
    }

    //基礎データに登録していない消防署に参集アラートダイアログ
    private void AnotherSansyusakiDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_anothersansyusaki, (ViewGroup) findViewById(R.id.dlgAnotherSansyusaki));
        //セット
        builder.setView(layout);
        builder.setNegativeButton("はい", null);
        builder.setCancelable(false);
        builder.create();
        builder.show();
    }

    //メール送信
    private void sendMail(){
        //メール本文に書く職員情報を呼び出し
        personalId = sp.getString("personalId", "9999999");
        personalClass = sp.getString("personalClass", "士");
        personalAge = sp.getString("personalAge", "99");
        personalDepartment = sp.getString("personalDepartment", "消防局");
        personalName = sp.getString("personalName", "名字名前");
        trimSpaces();
        personalRide = sp.getString( "personalRide", "ST");
        String Engineer = "";
        if (sp.getBoolean("personalEngineer", false)){
            Engineer = "有";
        } else {
            Engineer = "無";
        }
        String Paramedic = "";
        if (sp.getBoolean("personalParamedic", false)) {
            Paramedic = "有";
        } else {
            Paramedic = "無";
        }
        //すべてを半角スペースで結合
        String message = personalId + " " + personalClass + " " + personalAge + " " + personalDepartment + " " + personalName + " " + " " + personalRide + " " + Engineer + " " + Paramedic;
        //メール立ち上げ
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, mailAddress); // To:は配列でないとダメ
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        try {
            startActivity(Intent.createChooser(intent, "メールアプリを選択"));
        } catch (android.content.ActivityNotFoundException ex){
            Toast.makeText(mActivity, "メールアプリが見つかりません", Toast.LENGTH_LONG).show();
        }
    }
}

