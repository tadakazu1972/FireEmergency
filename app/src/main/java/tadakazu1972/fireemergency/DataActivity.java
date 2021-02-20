package tadakazu1972.fireemergency;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.opencsv.CSVParser;
import com.opencsv.CSVReader;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.UUID;

//import android.database.sqlite.SQLiteDatabase;

/**
 * Created by tadakazu on 2016/07/18.
 */
public class DataActivity extends AppCompatActivity {
    protected DataActivity mActivity = null;
    protected View mView = null;
    private Spinner mSpn1 = null;
    private Spinner mSpn2 = null;
    private Spinner mSpn3 = null;
    //所属データ保存前格納用
    private String mainStation = null;
    private String tsunamiStation = null;
    private String kubun = null;
    //連絡網データ操作用変数
    protected ListView mListView = null;
    protected DBHelper mDBHelper = null;
    protected SQLiteDatabase db = null;
    protected SimpleCursorAdapter mAdapter = null;
    protected CustomCursorAdapter mAdapter2 = null;
    //連絡網データ入力用　親所属スピナー文字列保存用
    private static String mSelected;
    private static String[] mArray;
    //showTel2()ダイアログ閉じるための制御用
    private AlertDialog mDialogShowTel2 = null;
    //showUpdateTel()データ呼び出して対応スピナー当てはめ用
    private boolean mInitSpinner = false;
    private int _syozokuPos = 0;
    //削除選択データid格納用
    private ArrayList<String> deleteArray;
    //初回パスフラグ
    public boolean mPassFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mActivity = this;
        mView = this.getWindow().getDecorView();
        setContentView(R.layout.activity_data);

        //ボタン設定
        //非常参集　職員情報入力　ボタン
        mView.findViewById(R.id.btnPersonal).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(mActivity, PersonalActivity.class);
                startActivity(intent);
            }
        });
        //アプリ説明書　ボタン
        mView.findViewById(R.id.btnGuide).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(mActivity, GuideActivity.class);
                startActivity(intent);
            }
        });

        mSpn1 = (Spinner)findViewById(R.id.spnData1);
        mSpn2 = (Spinner)findViewById(R.id.spnData2);
        mSpn3 = (Spinner)findViewById(R.id.spnData3);

        //保存している基礎データを呼び出してスピナーにセット
        //勤務消防署
        SharedPreferences sp1 = PreferenceManager.getDefaultSharedPreferences(this);
        String compareValue1 = sp1.getString("mainStation","消防局"); // 第２引数はkeyが存在しない時に返す初期値
        mainStation = sp1.getString("mainStation","消防局"); //インストール直後誤動作防止
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.firestation, R.layout.custom_spinner_item);
        mSpn1.setAdapter(adapter1);
        if (!compareValue1.equals(null)){
            int spinnerPosition = adapter1.getPosition(compareValue1);
            mSpn1.setSelection(spinnerPosition);
        }
        //大津波・津波警報時指定署
        SharedPreferences sp2 = PreferenceManager.getDefaultSharedPreferences(this);
        String compareValue2 = sp2.getString("tsunamiStation", "消防局"); // 第２引数はkeyが存在しない時に返す初期値
        tsunamiStation = sp2.getString("tsunamiStation", "消防局"); //インストール直後誤動作防止
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.firestation, R.layout.custom_spinner_item);
        mSpn2.setAdapter(adapter2);
        if (!compareValue2.equals(null)){
            int spinnerPosition = adapter2.getPosition(compareValue2);
            mSpn2.setSelection(spinnerPosition);
        }
        //招集区分
        SharedPreferences sp3 = PreferenceManager.getDefaultSharedPreferences(this);
        String compareValue3 = sp3.getString("kubun", "１"); // 第２引数はkeyが存在しない時に返す初期値
        kubun = sp3.getString("kubun", "１"); //インストール直後誤動作防止
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this, R.array.kubun, R.layout.custom_spinner_item);
        mSpn3.setAdapter(adapter3);
        if (!compareValue3.equals(null)){
            int spinnerPosition = adapter3.getPosition(compareValue3);
            mSpn3.setSelection(spinnerPosition);
        }
        //スピナーの選択リスナー設定
        mSpn1.setOnItemSelectedListener(new OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                mainStation = (String)mSpn1.getSelectedItem();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){
                //何もしない
            }
        });
        mSpn2.setOnItemSelectedListener(new OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                tsunamiStation = (String)mSpn2.getSelectedItem();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){
                //何もしない
            }
        });
        mSpn3.setOnItemSelectedListener(new OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                kubun = (String)mSpn3.getSelectedItem();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){
                //何もしない
            }
        });

        mView.findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //保存
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mActivity);
                sp.edit().putString("mainStation",mainStation).apply();
                sp.edit().putString("tsunamiStation",tsunamiStation).apply();
                sp.edit().putString("kubun",kubun).apply();
                Toast.makeText(mActivity, "基礎データを登録しました", Toast.LENGTH_SHORT).show();
            }
        });

        //連絡網データ作成
        mListView = new ListView(this);
        mDBHelper = new DBHelper(this);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String mKey = sp.getString("key", null);
        db = mDBHelper.getWritableDatabase(mKey);
        //データまとめて削除用ArrayList初期化
        deleteArray = new ArrayList<String>();
        //連絡網データ確認ボタン
        mView.findViewById(R.id.btnTel).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                showCheck(0);
            }
        });
        //連絡網データ入力ボタン
        mView.findViewById(R.id.btnTelEdit).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                showEditTel();
            }
        });
        //連絡網データ修正ボタン
        mView.findViewById(R.id.btnTelUpdate).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                showCheck(1);
            }
        });
        //連絡網データ削除ボタン
        mView.findViewById(R.id.btnTelDelete).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                showCheck(2);
            }
        });
        mView.findViewById(R.id.btnImport).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                showImport();
            }
        });

        //復帰用ボタン
        mView.findViewById(R.id.btnEarthquake).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(mActivity, EarthquakeActivity.class);
                startActivity(intent);
            }
        });
        mView.findViewById(R.id.btnTyphoon).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(mActivity, TyphoonActivity.class);
                startActivity(intent);
            }
        });
        mView.findViewById(R.id.btnKokuminhogo).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(mActivity, KokuminhogoActivity.class);
                startActivity(intent);
            }
        });
        mView.findViewById(R.id.btnKinentai).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(mActivity, KinentaiActivity.class);
                startActivity(intent);
            }
        });
    }

    //連絡網データ表示
    private void showCheck(int i){
        if (!mPassFlag) {
            //分岐用変数
            final int fork = i;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.checkTitle);
            //カスタムビュー設定
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.check, (ViewGroup) findViewById(R.id.telCheck));
            //データ取得準備
            final EditText edit1 = (EditText) layout.findViewById(R.id.editCheck);
            builder.setView(layout);
            builder.setPositiveButton("入力", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String checked = edit1.getText().toString();
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mActivity);
                    String uuid = UUID.randomUUID().toString(); //念のためpasswordが空の時に返すダミーデータ生成。空の時にそのままエンター押して通過されるのを防止
                    String word = sp.getString("password", uuid);
                    if (checked.equals(word)) {
                        checked = null; //これを入れて明示的に閉じないと次の画面でEditTextのインスタンスに反応してソフトキーボードが立ち上がり続ける端末あり
                        dialog.dismiss(); //これを入れて明示的に閉じないと次の画面でEditTextのインスタンスに反応してソフトキーボードが立ち上がり続ける端末あり
                        //mPassFlagをオン
                        mPassFlag = true;
                        switch (fork) {
                            case 0:
                                showTel();
                                break;
                            case 1:
                                showTel2();
                                break;
                            case 2:
                                showTel3();
                                break;
                        }
                    }
                }
            });
            builder.setNegativeButton("キャンセル", null);
            builder.setCancelable(true);
            builder.create();
            builder.show();
        } else {
            switch (i) {
                case 0:
                    showTel();
                    break;
                case 1:
                    showTel2();
                    break;
                case 2:
                    showTel3();
                    break;
            }
        }
    }

    private void showTel(){
        //データ準備
        String order;
        order = "select * from records order by _id";
        Cursor c = mActivity.db.rawQuery(order, null);
        String[] from = {"name","tel","mail","kubun","syozoku0","syozoku","kinmu"};
        int[] to = {R.id.record_name,R.id.record_tel,R.id.record_mail,R.id.record_kubun,R.id.record_syozoku0,R.id.record_syozoku,R.id.record_kinmu};
        mActivity.mAdapter = new SimpleCursorAdapter(mActivity,R.layout.record_view,c,from,to,0);
        mListView.setAdapter(mActivity.mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                //残念だが以下は機能しない。電話させるautoLink="phone"を優先させる。そのため以下コメントアウトする。
                // ListView listView = (ListView)parent;
                //Cursor i = (Cursor)listView.getItemAtPosition(position);
                //String _id = i.getString(i.getColumnIndex("_id"));
                //String _name = i.getString(i.getColumnIndex("name"));
                //String _tel = i.getString(i.getColumnIndex("tel"));
                //String _mail = i.getString(i.getColumnIndex("mail"));
                //String _kubun = i.getString(i.getColumnIndex("kubun"));
                //String _syozoku = i.getString(i.getColumnIndex("syozoku"));
                //String _kinmu = i.getString(i.getColumnIndex("kinmu"));
                //showUpdateTel(_id, _name, _tel, _mail, _kubun, _syozoku, _kinmu);
            }
        });
        //ダイアログ生成
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("連絡網データ");
        ViewGroup parent = (ViewGroup)mListView.getParent();
        if ( parent!=null) {
            parent.removeView(mListView);
        }
        builder.setView(mListView);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //連絡網データ表示　修正用
    private void showTel2(){
        //データ準備
        String order;
        order = "select * from records order by _id";
        Cursor c = mActivity.db.rawQuery(order, null);
        String[] from = {"name","tel","kubun","syozoku0","syozoku","kinmu","mail"};
        int[] to = {R.id.record_name, R.id.record_tel, R.id.record_kubun, R.id.record_syozoku0, R.id.record_syozoku, R.id.record_kinmu, R.id.record_mail};
        mActivity.mAdapter = new SimpleCursorAdapter(mActivity,R.layout.record_view_update,c,from,to,0);
        mListView.setAdapter(mActivity.mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                ListView listView = (ListView)parent;
                Cursor i = (Cursor)listView.getItemAtPosition(position);
                String _id = i.getString(i.getColumnIndex("_id"));
                String _name = i.getString(i.getColumnIndex("name"));
                String _tel = i.getString(i.getColumnIndex("tel"));
                String _mail = i.getString(i.getColumnIndex("mail"));
                String _kubun = i.getString(i.getColumnIndex("kubun"));
                String _syozoku0 = i.getString(i.getColumnIndex("syozoku0"));
                String _syozoku = i.getString(i.getColumnIndex("syozoku"));
                String _kinmu = i.getString(i.getColumnIndex("kinmu"));
                //選択された行のデータを送る
                showUpdateTel(_id, _name, _tel, _mail, _kubun, _syozoku0, _syozoku, _kinmu);
                //明示的に消さないとあとで空のダイアログが残る
                mDialogShowTel2.dismiss();
            }
        });
        //ダイアログ生成
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("修正するデータを選択");
        ViewGroup parent = (ViewGroup)mListView.getParent();
        if ( parent!=null) {
            parent.removeView(mListView);
        }
        builder.setView(mListView);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        mDialogShowTel2 = builder.show(); //このやり方は知らなかった
    }

    //連絡網データ表示　削除用
    private void showTel3(){
        //データ準備
        final String order = "select * from records order by _id";
        final Cursor c = mActivity.db.rawQuery(order, null);
        String[] from = {"name","tel","kubun","syozoku0","syozoku","kinmu","mail"};
        int[] to = {R.id.record_name,R.id.record_tel, R.id.record_kubun, R.id.record_syozoku0, R.id.record_syozoku, R.id.record_kinmu, R.id.record_mail};
        //初回のみ起動。そうしないと、すべて選択した後の２回目がまたnewされて意味ない
        if (mAdapter2 == null) {
            mActivity.mAdapter2 = new CustomCursorAdapter(mActivity, R.layout.record_view_delete2, c, from, to, 0);
        }
        mListView.setAdapter(mActivity.mAdapter2);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        Integer itemcount = mListView.getCount();
        Toast.makeText(mActivity, "データ件数："+String.valueOf(itemcount), Toast.LENGTH_SHORT).show();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                //タップした位置のデータをチェック処理
                mActivity.mAdapter2.clickData(position, view);
            }
        });
        //ダイアログ生成
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("削除するデータを選択");
        ViewGroup parent = (ViewGroup)mListView.getParent();
        if ( parent!=null) {
            parent.removeView(mListView);
        }
        builder.setView(mListView);
        builder.setPositiveButton("削除", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                //第一段階　削除対象リストに格納
                //CustomCursorAdapterのメンバ変数であるitemCheckedを見に行く
                c.moveToFirst(); //カーソルを先頭に
                for (int i=0; i < mAdapter2.itemChecked.size(); i++){
                    //チェックされていたら対応するカーソルの_idを削除対象文字列に格納
                    if (mAdapter2.itemChecked.get(i)){
                        deleteArray.add(c.getString(c.getColumnIndex("_id")));
                    }
                    c.moveToNext();
                }
                //第二段階　削除実行
                if ( deleteArray.get(0) != null ) {
                    for(int i=0;i< deleteArray.size(); i++) {
                        mActivity.mDBHelper.delete(db, deleteArray.get(i));
                    }
                    Toast.makeText(mActivity, "データを削除しました。", Toast.LENGTH_SHORT).show();
                    //削除結果を見せるため再度呼び出し
                    mAdapter2 = null;
                    showTel3();
                } else {
                    Toast.makeText(mActivity, "データが選択されていません。", Toast.LENGTH_SHORT).show();
                    //再度呼び出し
                    showTel3();
                }
            }
        });
        builder.setNeutralButton("すべて選択/解除", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                c.moveToFirst();
                for (int i=0; i < mAdapter2.itemChecked.size(); i++){
                    if (!mAdapter2.itemChecked.get(i)) {
                        mActivity.mAdapter2.itemChecked.set(i, true);
                    } else {
                        mActivity.mAdapter2.itemChecked.set(i, false);
                    }
                }
                //再帰しないとsetNeutralButtonを押すとダイアログが自動で消えてしまって意味がないので・・・
                showTel3();
            }
        });
        builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which){
                mAdapter2 = null;
            }
        });
        builder.setCancelable(true);
        builder.create();
        mDialogShowTel2 = builder.show(); //このやり方は知らなかった
    }

    //連絡網データ入力
    private void showEditTel(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("新規データ入力");
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.tel_edit, (ViewGroup)findViewById(R.id.telEdit));
        //データ取得準備
        final EditText editName = (EditText)layout.findViewById(R.id.editName);
        final EditText editTel  = (EditText)layout.findViewById(R.id.editTel);
        final EditText editMail = (EditText)layout.findViewById(R.id.editMail);
        final Spinner  editKubun = (Spinner)layout.findViewById(R.id.editKubun);
        final Spinner  editSyozoku = (Spinner)layout.findViewById(R.id.editSyozoku);
        final Spinner  editSyozoku2 = (Spinner)layout.findViewById(R.id.editSyozoku2);
        //親所属スピナー選択時の処理
        editSyozoku.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                //親所属スピナーの選択した位置をint取得
                int i = parent.getSelectedItemPosition();
                //Toast.makeText(mActivity, String.valueOf(i)+"番目を選択", Toast.LENGTH_SHORT).show();
                //取得したintを配列リソース名に変換し、配列リソースIDを取得（なぜか日本語ではエラーが出るのでアルファベットと数字で対応））
                mSelected = "firestation"+ String.valueOf(i);
                int resourceId = getResources().getIdentifier(mSelected, "array", getPackageName());
                //Toast.makeText(mActivity, "resourceID="+String.valueOf(resourceId), Toast.LENGTH_SHORT).show();
                //取得した配列リソースIDを文字列配列に格納
                mArray = getResources().getStringArray(resourceId);
                //配列リソースIDから取得した文字列配列をアダプタに入れる
                ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item);
                for (String aMArray : mArray) {
                    mAdapter.add(aMArray);
                }
                mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //アダプタを子スピナーにセット
                editSyozoku2.setAdapter(mAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent){
                //nothing to do
            }
        });
        final Spinner  editKinmu = (Spinner)layout.findViewById(R.id.editKinmu);
        builder.setView(layout);
        builder.setPositiveButton("登録", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                String name = editName.getText().toString();
                String tel  = editTel.getText().toString();
                String mail = editMail.getText().toString();
                String kubun = (String)editKubun.getSelectedItem();
                String syozoku0 = (String)editSyozoku.getSelectedItem();
                String syozoku = (String)editSyozoku2.getSelectedItem();
                String kinmu = (String)editKinmu.getSelectedItem();
                mActivity.mDBHelper.insert(db, name, tel, mail, kubun, syozoku0, syozoku, kinmu);
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //連絡網データ修正
    private void showUpdateTel(String _id, String _name, String _tel, String _mail, String _kubun, String _syozoku0, String _syozoku, String _kinmu){
        final String id = _id;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("データ修正");
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.tel_edit, (ViewGroup)findViewById(R.id.telEdit));
        //データ取得準備
        final EditText editName = (EditText)layout.findViewById(R.id.editName);
        final EditText editTel  = (EditText)layout.findViewById(R.id.editTel);
        final EditText editMail = (EditText)layout.findViewById(R.id.editMail);
        final Spinner  editKubun = (Spinner)layout.findViewById(R.id.editKubun);
        final Spinner  editSyozoku = (Spinner)layout.findViewById(R.id.editSyozoku);
        final Spinner  editSyozoku2 = (Spinner)layout.findViewById(R.id.editSyozoku2);
        final Spinner  editKinmu = (Spinner)layout.findViewById(R.id.editKinmu);
        //送られてきたデータをはめこむ
        editName.setText(_name);
        editTel.setText(_tel);
        editMail.setText(_mail);
        //非常招集区分のスピナー初期値設定
        int _kubunPos=0;
        if (_kubun.equals("１号招集")){ _kubunPos =0; }
        if (_kubun.equals("２号招集")){ _kubunPos =1; }
        if (_kubun.equals("３号招集")){ _kubunPos =2; }
        if (_kubun.equals("４号招集")){ _kubunPos =3; }
        editKubun.setSelection(_kubunPos);
        //所属のスピナー初期設定
        int _syozokuParentPos=0;
        _syozokuPos=0;
        mInitSpinner = true;
        if (_syozoku.equals("総務課")){ _syozokuParentPos=0; _syozokuPos=0;}
        if (_syozoku.equals("人事課")){ _syozokuParentPos=0; _syozokuPos=1;}
        if (_syozoku.equals("施設課")){ _syozokuParentPos=0; _syozokuPos=2;}
        if (_syozoku.equals("予防課")){ _syozokuParentPos=0; _syozokuPos=3;}
        if (_syozoku.equals("規制課")){ _syozokuParentPos=0; _syozokuPos=4;}
        if (_syozoku.equals("警防課")){ _syozokuParentPos=0; _syozokuPos=5;}
        if (_syozoku.equals("司令課")){ _syozokuParentPos=0; _syozokuPos=6;}
        if (_syozoku.equals("救急課")){ _syozokuParentPos=0; _syozokuPos=7;}
        if (_syozoku.equals("北本署")){ _syozokuParentPos=1; _syozokuPos=0;}
        if (_syozoku.equals("梅田"))  { _syozokuParentPos=1; _syozokuPos=1;}
        if (_syozoku.equals("浮田"))  { _syozokuParentPos=1; _syozokuPos=2;}
        if (_syozoku.equals("南森町")){ _syozokuParentPos=1; _syozokuPos=3;}
        if (_syozoku.equals("与力"))  { _syozokuParentPos=1; _syozokuPos=4;}
        if (_syozoku.equals("大淀町")){ _syozokuParentPos=1; _syozokuPos=5;}
        if (_syozoku.equals("本庄"))  { _syozokuParentPos=1; _syozokuPos=6;}
        if (_syozoku.equals("都島本署")){ _syozokuParentPos=2; _syozokuPos=0;}
        if (_syozoku.equals("高倉"))  { _syozokuParentPos=2; _syozokuPos=1;}
        if (_syozoku.equals("東野田")){ _syozokuParentPos=2; _syozokuPos=2;}
        if (_syozoku.equals("福島本署")){ _syozokuParentPos=3; _syozokuPos=0;}
        if (_syozoku.equals("上福島")){ _syozokuParentPos=3; _syozokuPos=1;}
        if (_syozoku.equals("海老江")){ _syozokuParentPos=3; _syozokuPos=2;}
        if (_syozoku.equals("此花本署")){ _syozokuParentPos=4; _syozokuPos=0;}
        if (_syozoku.equals("桜島")){ _syozokuParentPos=4; _syozokuPos=1;}
        if (_syozoku.equals("西九条")){ _syozokuParentPos=4; _syozokuPos=2;}
        if (_syozoku.equals("中央本署")){ _syozokuParentPos=5; _syozokuPos=0;}
        if (_syozoku.equals("東雲")){ _syozokuParentPos=5; _syozokuPos=1;}
        if (_syozoku.equals("道頓堀")){ _syozokuParentPos=5; _syozokuPos=2;}
        if (_syozoku.equals("南坂町")){ _syozokuParentPos=5; _syozokuPos=3;}
        if (_syozoku.equals("上町")){ _syozokuParentPos=5; _syozokuPos=4;}
        if (_syozoku.equals("西本署")){ _syozokuParentPos=6; _syozokuPos=0;}
        if (_syozoku.equals("江戸堀")){ _syozokuParentPos=6; _syozokuPos=1;}
        if (_syozoku.equals("新町")){ _syozokuParentPos=6; _syozokuPos=2;}
        if (_syozoku.equals("港本署")){ _syozokuParentPos=7; _syozokuPos=0;}
        if (_syozoku.equals("田中")){ _syozokuParentPos=7; _syozokuPos=1;}
        if (_syozoku.equals("大正本署")){ _syozokuParentPos=8; _syozokuPos=0;}
        if (_syozoku.equals("泉尾")){ _syozokuParentPos=8; _syozokuPos=1;}
        if (_syozoku.equals("鶴町")){ _syozokuParentPos=8; _syozokuPos=2;}
        if (_syozoku.equals("天王寺本署")){ _syozokuParentPos=9; _syozokuPos=0;}
        if (_syozoku.equals("元町")){ _syozokuParentPos=9; _syozokuPos=1;}
        if (_syozoku.equals("浪速本署")){ _syozokuParentPos=10; _syozokuPos=0;}
        if (_syozoku.equals("恵美須")){ _syozokuParentPos=10; _syozokuPos=1;}
        if (_syozoku.equals("立葉")){ _syozokuParentPos=10; _syozokuPos=2;}
        if (_syozoku.equals("浪速出張所")){ _syozokuParentPos=10; _syozokuPos=3;}
        if (_syozoku.equals("西淀川本署")){ _syozokuParentPos=11; _syozokuPos=0;}
        if (_syozoku.equals("佃")){ _syozokuParentPos=11; _syozokuPos=1;}
        if (_syozoku.equals("大和田")){ _syozokuParentPos=11; _syozokuPos=2;}
        if (_syozoku.equals("竹島")){ _syozokuParentPos=11; _syozokuPos=3;}
        if (_syozoku.equals("淀川本署")){ _syozokuParentPos=12; _syozokuPos=0;}
        if (_syozoku.equals("十三橋")){ _syozokuParentPos=12; _syozokuPos=1;}
        if (_syozoku.equals("加島")){ _syozokuParentPos=12; _syozokuPos=2;}
        if (_syozoku.equals("東三国")){ _syozokuParentPos=12; _syozokuPos=3;}
        if (_syozoku.equals("東淀川本署")){ _syozokuParentPos=13; _syozokuPos=0;}
        if (_syozoku.equals("豊里")){ _syozokuParentPos=13; _syozokuPos=1;}
        if (_syozoku.equals("小松")){ _syozokuParentPos=13; _syozokuPos=2;}
        if (_syozoku.equals("井高野")){ _syozokuParentPos=13; _syozokuPos=3;}
        if (_syozoku.equals("柴島")){ _syozokuParentPos=13; _syozokuPos=4;}
        if (_syozoku.equals("西淡路")){ _syozokuParentPos=13; _syozokuPos=5;}
        if (_syozoku.equals("東成本署")){ _syozokuParentPos=14; _syozokuPos=0;}
        if (_syozoku.equals("中本")){ _syozokuParentPos=14; _syozokuPos=1;}
        if (_syozoku.equals("深江")){ _syozokuParentPos=14; _syozokuPos=2;}
        if (_syozoku.equals("生野本署")){ _syozokuParentPos=15; _syozokuPos=0;}
        if (_syozoku.equals("勝山")){ _syozokuParentPos=15; _syozokuPos=1;}
        if (_syozoku.equals("中川")){ _syozokuParentPos=15; _syozokuPos=2;}
        if (_syozoku.equals("巽")){ _syozokuParentPos=15; _syozokuPos=3;}
        if (_syozoku.equals("旭本署")){ _syozokuParentPos=16; _syozokuPos=0;}
        if (_syozoku.equals("新森")){ _syozokuParentPos=16; _syozokuPos=1;}
        if (_syozoku.equals("赤川")){ _syozokuParentPos=16; _syozokuPos=2;}
        if (_syozoku.equals("城東本署")){ _syozokuParentPos=17; _syozokuPos=0;}
        if (_syozoku.equals("放出")){ _syozokuParentPos=17; _syozokuPos=1;}
        if (_syozoku.equals("中浜")){ _syozokuParentPos=17; _syozokuPos=2;}
        if (_syozoku.equals("関目")){ _syozokuParentPos=17; _syozokuPos=3;}
        if (_syozoku.equals("鶴見本署")){ _syozokuParentPos=18; _syozokuPos=0;}
        if (_syozoku.equals("今津")){ _syozokuParentPos=18; _syozokuPos=1;}
        if (_syozoku.equals("茨田")){ _syozokuParentPos=18; _syozokuPos=2;}
        if (_syozoku.equals("住之江本署")){ _syozokuParentPos=19; _syozokuPos=0;}
        if (_syozoku.equals("平林")){ _syozokuParentPos=19; _syozokuPos=1;}
        if (_syozoku.equals("加賀屋")){ _syozokuParentPos=19; _syozokuPos=2;}
        if (_syozoku.equals("南港")){ _syozokuParentPos=19; _syozokuPos=3;}
        if (_syozoku.equals("阿倍野本署")){ _syozokuParentPos=20; _syozokuPos=0;}
        if (_syozoku.equals("清明通")){ _syozokuParentPos=20; _syozokuPos=1;}
        if (_syozoku.equals("阪南")){ _syozokuParentPos=20; _syozokuPos=2;}
        if (_syozoku.equals("住吉本署")){ _syozokuParentPos=21; _syozokuPos=0;}
        if (_syozoku.equals("苅田")){ _syozokuParentPos=21; _syozokuPos=1;}
        if (_syozoku.equals("万代")){ _syozokuParentPos=21; _syozokuPos=2;}
        if (_syozoku.equals("東住吉本署")){ _syozokuParentPos=22; _syozokuPos=0;}
        if (_syozoku.equals("北田辺")){ _syozokuParentPos=22; _syozokuPos=1;}
        if (_syozoku.equals("杭全")){ _syozokuParentPos=22; _syozokuPos=2;}
        if (_syozoku.equals("矢田")){ _syozokuParentPos=22; _syozokuPos=3;}
        if (_syozoku.equals("平野本署")){ _syozokuParentPos=23; _syozokuPos=0;}
        if (_syozoku.equals("加美")){ _syozokuParentPos=23; _syozokuPos=1;}
        if (_syozoku.equals("長吉")){ _syozokuParentPos=23; _syozokuPos=2;}
        if (_syozoku.equals("喜連")){ _syozokuParentPos=23; _syozokuPos=3;}
        if (_syozoku.equals("加美正覚寺")){ _syozokuParentPos=23; _syozokuPos=4;}
        if (_syozoku.equals("西成本署")){ _syozokuParentPos=24; _syozokuPos=0;}
        if (_syozoku.equals("海道")){ _syozokuParentPos=24; _syozokuPos=1;}
        if (_syozoku.equals("津守")){ _syozokuParentPos=24; _syozokuPos=2;}
        if (_syozoku.equals("水上")){ _syozokuParentPos=25; _syozokuPos=0;}
        editSyozoku.setSelection(_syozokuParentPos); //親スピナー設定 これでsetOnItemSelectedListenerが作動するので子スピナーのアイテムは設定される
        editSyozoku2.setSelection(_syozokuPos); //子スピナーのポジション設定
        //勤務区分のスピナー初期値設定
        int _kinmuPos=0;
        if (_kinmu.equals("日勤")){ _kinmuPos =0; }
        if (_kinmu.equals("１部")){ _kinmuPos =1; }
        if (_kinmu.equals("２部")){ _kinmuPos =2; }
        editKinmu.setSelection(_kinmuPos);
        //親所属スピナー選択時の処理
        editSyozoku.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                //親所属スピナーの選択した位置をint取得
                int i = parent.getSelectedItemPosition();
                //Toast.makeText(mActivity, String.valueOf(i)+"番目を選択", Toast.LENGTH_SHORT).show();
                //取得したintを配列リソース名に変換し、配列リソースIDを取得（なぜか日本語ではエラーが出るのでアルファベットと数字で対応））
                mSelected = "firestation"+ String.valueOf(i);
                int resourceId = getResources().getIdentifier(mSelected, "array", getPackageName());
                //Toast.makeText(mActivity, "resourceID="+String.valueOf(resourceId), Toast.LENGTH_SHORT).show();
                //取得した配列リソースIDを文字列配列に格納
                mArray = getResources().getStringArray(resourceId);
                //配列リソースIDから取得した文字列配列をアダプタに入れる
                ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item);
                for (String aMArray : mArray) {
                    mAdapter.add(aMArray);
                }
                mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //アダプタを子スピナーにセット
                editSyozoku2.setAdapter(mAdapter);
                if (mInitSpinner){
                    editSyozoku2.setSelection(_syozokuPos);
                    mInitSpinner = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent){
                //nothing to do
            }
        });
        builder.setView(layout);
        builder.setPositiveButton("修正", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                String name = editName.getText().toString();
                String tel  = editTel.getText().toString();
                String mail = editMail.getText().toString();
                String kubun = (String)editKubun.getSelectedItem();
                String syozoku0 = (String)editSyozoku.getSelectedItem();
                String syozoku = (String)editSyozoku2.getSelectedItem();
                String kinmu = (String)editKinmu.getSelectedItem();
                mActivity.mDBHelper.update(db, id, name, tel, mail, kubun, syozoku0, syozoku, kinmu);
                Toast.makeText(mActivity, "データを修正しました。", Toast.LENGTH_SHORT).show();
                //修正結果を見せるため再度呼び出し
                showTel2();
            }
        });
        builder.setNeutralButton("削除", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                mActivity.mDBHelper.delete(db, id);
                Toast.makeText(mActivity, "データを削除しました。", Toast.LENGTH_SHORT).show();
                //削除結果を見せるため再度呼び出し
                showTel2();
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //連絡網データ CSVファイルインポート
    private final static int CHOSE_FILE_CODE = 1;
    private void showImport(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == CHOSE_FILE_CODE && resultCode == RESULT_OK) {
            String filename = null;
            final Uri uri = data.getData();
            Cursor c = getContentResolver().query(uri, null, null, null, null);
            if (c != null) {
                c.moveToFirst();
                filename = c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                c.close();
            }
            final String filename2 = filename; //以下のダイアログで使うためわざわざfinalで別名で保存
            //本当にファイルを取り込むのか今一度確認する
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("■CSVファイル読込");
            builder.setMessage(filename + "を連絡網に取り込みますか？");
            builder.setPositiveButton("はい", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                    importCSV(uri);
                    Toast.makeText(mActivity, "取り込んだファイル："+filename2, Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("キャンセル", null);
            builder.setCancelable(true);
            builder.create();
            builder.show();
        }
    }

    private void importCSV(Uri uri){
        if (uri==null)
            return;

        InputStream is = null;
        String name = "";
        String tel = "";
        String mail = "";
        String kubun = "";
        String syozoku0 = "";
        String syozoku = "";
        String kinmu = "";

        try {
            try {
                is = getContentResolver().openInputStream(uri);
                InputStreamReader ir = new InputStreamReader(is,"UTF-8");
                CSVReader csvreader = new CSVReader(ir, CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER,0);
                String[] csv;
                while ((csv = csvreader.readNext()) != null) {
                    name = csv[0];
                    tel = csv[1];
                    mail = csv[2];
                    kubun = csv[3];
                    syozoku0 = csv[4];
                    syozoku = csv[5];
                    kinmu = csv[6];
                    mActivity.mDBHelper.insert(db, name, tel, mail, kubun, syozoku0, syozoku, kinmu);
                }
            } finally {
                if (is != null) is.close();
                Toast.makeText(this, "連絡網にデータを読み込みました。", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "CSV読込エラー", Toast.LENGTH_SHORT).show();
        }
    }
}
