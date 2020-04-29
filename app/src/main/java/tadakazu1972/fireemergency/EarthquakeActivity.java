package tadakazu1972.fireemergency;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

//import android.database.sqlite.SQLiteDatabase;

/**
 * Created by tadakazu on 2016/07/17.
 */
public class EarthquakeActivity extends AppCompatActivity {
    protected EarthquakeActivity mActivity = null;
    protected View mView = null;
    //基礎データ保存用変数
    protected String mMainStation;
    protected String mTsunamiStation;
    protected String mKubun;
    //連絡網データ操作用変数
    protected ListView mListView = null;
    protected DBHelper mDBHelper = null;
    protected SQLiteDatabase db = null;
    protected SimpleCursorAdapter mAdapter = null;
    protected CustomCursorAdapter mAdapter2 = null;
    //連絡網データ入力用　親所属スピナー文字列保存用
    private static String mSelected;
    private static String[] mArray;
    //連絡網データ検索用
    protected String resKubun = "すべて";
    protected String resSyozoku0 = "すべて";
    protected String resSyozoku = "すべて";
    protected String resKinmu = "すべて";
    //まとめてメール送信用
    private ArrayList<String> mailArray;
    //連絡網初回パスワード要求フラグ
    public boolean mPassFlag = false;

    @Override
    protected void onCreate(Bundle savedInstaceState){
        super.onCreate(savedInstaceState);

        mActivity = this;
        mView = this.getWindow().getDecorView();
        setContentView(R.layout.activity_earthquake);
        initButtons();
        //基礎データ読み込み
        loadData();
        //インストール語初回起動判定->パスワード設定へ
        checkFirstLaunch();
        //連絡網データ作成
        mListView = new ListView(this);
        mDBHelper = new DBHelper(this);
        SQLiteDatabase.loadLibs(this);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String mKey = sp.getString("key", null);
        db = mDBHelper.getWritableDatabase(mKey);
        mailArray = new ArrayList<String>();
    }

    @Override
    protected void onResume(){
        super.onResume();
        //基礎データを変更してActivity復帰した際に反映させないと前のままなので
        loadData();
    }

    //ボタン設定
    private void initButtons(){
        mView.findViewById(R.id.btnData).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(mActivity, DataActivity.class);
                startActivity(intent);
            }
        });
        mView.findViewById(R.id.btnTyphoon).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(mActivity, TyphoonActivity.class);
                startActivity(intent);
            }
        });
        mView.findViewById(R.id.btnKokuminhogo).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(mActivity, KokuminhogoActivity.class);
                startActivity(intent);
            }
        });
        mView.findViewById(R.id.btnKinentai).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(mActivity, KinentaiActivity.class);
                startActivity(intent);
            }
        });
        mView.findViewById(R.id.btnEarthquake1).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                showEarthquake1();
            }
        });
        mView.findViewById(R.id.btnEarthquake2).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                showEarthquake2();
            }
        });
        mView.findViewById(R.id.btnEarthquake3).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                showEarthquake3();
            }
        });
        mView.findViewById(R.id.btnEarthquake4).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                showEarthquake4();
            }
        });
        mView.findViewById(R.id.btnEarthquake5).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                showEarthquake5();
            }
        });
        mView.findViewById(R.id.btnEarthquake6).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                showEarthquake6();
            }
        });
        mView.findViewById(R.id.btnEarthquakeEarthquake).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                showEarthquake();
            }
        });
        mView.findViewById(R.id.btnEarthquakeBlackout).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                showBlackout();
            }
        });
        mView.findViewById(R.id.btnEarthquakeRoad).setOnClickListener(new OnClickListener(){
           @Override
            public void onClick(View v){
               showRoad();
           }
        });
        mView.findViewById(R.id.btnEarthquakeTel).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                showCheck();
            }
        });
        mView.findViewById(R.id.btnEarthquakeCaution).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                showCaution();
            }
        });
        mView.findViewById(R.id.btnEarthquakeBousaiNet).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                showBousaiNet();
            }
        });
    }

    //インストール後初回起動かチェック->パスワード設定へ
    private void checkFirstLaunch(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String check = sp.getString("firstLaunch","yet"); // 第２引数はkeyが存在しない時に返す初期値　ゆえに、初回だった場合はyetが返される。次回以降はdoneを代入してここを回避させる
        if (check.equals("yet")){
            setPassword();
            sp.edit().putString("firstLaunch","done").apply(); //doneを代入することで以降は初回起動とは判定されなくなる
            String mKey = UUID.randomUUID().toString(); // key生成
            sp.edit().putString("key", mKey);
        }
    }

    //パスワード設定
    private void setPassword(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.setPassword);
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.check, (ViewGroup)findViewById(R.id.telCheck));
        //データ取得準備
        final EditText edit1 = (EditText)layout.findViewById(R.id.editCheck);
        builder.setView(layout);
        builder.setPositiveButton("設定", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                String word = edit1.getText().toString();
                if (!word.equals("")){
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mActivity);
                    sp.edit().putString("password",word).apply();
                    word = null; //これを入れて明示的に閉じないと次の画面でEditTextのインスタンスに反応してソフトキーボードが立ち上がり続ける端末あり
                    dialog.dismiss(); //これを入れて明示的に閉じないと次の画面でEditTextのインスタンスに反応してソフトキーボードが立ち上がり続ける端末あり
                } else {
                    Toast.makeText(mActivity, "空白は受け付けません。パスワードの設定をお願いします。", Toast.LENGTH_SHORT).show();
                    setPassword(); //再帰
                }
            }
        });
        builder.setCancelable(false);
        builder.create();
        builder.show();
    }

    //基礎データ読み込み
    private void loadData(){
        //勤務消防署
        SharedPreferences sp1 = PreferenceManager.getDefaultSharedPreferences(this);
        mMainStation = sp1.getString("mainStation","消防局"); // 第２引数はkeyが存在しない時に返す初期値
        //大津波・津波警報時指定署
        SharedPreferences sp2 = PreferenceManager.getDefaultSharedPreferences(this);
        mTsunamiStation = sp2.getString("tsunamiStation", "消防局"); // 第２引数はkeyが存在しない時に返す初期値
        //招集区分
        SharedPreferences sp3 = PreferenceManager.getDefaultSharedPreferences(this);
        mKubun = sp3.getString("kubun", "１"); // 第２引数はkeyが存在しない時に返す初期値
    }

    //震度５強以上
    private void showEarthquake1(){
        final CharSequence[] actions = {"■大津波警報","■津波警報","■警報なし"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("発令されている警報は？");
        builder.setItems(actions, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                switch(which){
                    case 0:
                        showEarthquake11();
                        break;
                    case 1:
                        showEarthquake12();
                        break;
                    case 2:
                        showEarthquake13();
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showEarthquake11(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■大津波警報");
        String s;
        if (mTsunamiStation.equals("消防局")||mTsunamiStation.equals("教育訓練センター")){
            s = mTsunamiStation+"へ参集";
        } else {
            s = mTsunamiStation+"消防署へ参集";
        }
        builder.setMessage("１号非常招集\n\n"+s);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showEarthquake12(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■津波警報");
        String s;
        if (mTsunamiStation.equals("消防局")||mTsunamiStation.equals("教育訓練センター")){
            s = mTsunamiStation+"へ参集";
        } else {
            s = mTsunamiStation+"消防署へ参集";
        }
        builder.setMessage("１号非常招集\n\n"+s);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showEarthquake13(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■警報なし");
        String s;
        if (mMainStation.equals("消防局")||mMainStation.equals("教育訓練センター")){
            s = mMainStation+"へ参集";
        } else {
            s = mMainStation+"消防署へ参集";
        }
        builder.setMessage("１号非常招集\n\n"+s);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //震度５弱
    private void showEarthquake2(){
        final CharSequence[] actions = {"■大津波警報","■津波警報","■警報なし"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("発令されている警報は？");
        builder.setItems(actions, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                switch(which){
                    case 0:
                        showEarthquake21();
                        break;
                    case 1:
                        showEarthquake22();
                        break;
                    case 2:
                        showEarthquake23();
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showEarthquake21(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■大津波警報");
        String s;
        if (mTsunamiStation.equals("消防局")||mTsunamiStation.equals("教育訓練センター")) {
            s = mTsunamiStation+"へ参集";
        } else {
            s = mTsunamiStation+"消防署へ参集";
        }
        builder.setMessage("１号非常招集\n\n"+s);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showEarthquake22(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■津波警報");
        String s;
        //２号招集なので、１号は参集なしの判定する
        if (mKubun.equals("１号招集")){
            s = "招集なし";
        } else {
            if (mTsunamiStation.equals("消防局")||mTsunamiStation.equals("教育訓練センター")) {
                s = mTsunamiStation+"へ参集";
            } else {
                s = mTsunamiStation+"消防署へ参集";
            }
        }
        builder.setMessage("２号非常招集\n\n"+s);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showEarthquake23(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■警報なし");
        String s;
        //２号招集なので、１号は参集なしの判定する
        if (mKubun.equals("１号招集")){
            s = "招集なし";
        } else {
            if (mMainStation.equals("消防局")||mMainStation.equals("教育訓練センター")) { //勤務消防署であることに注意!
                s = mMainStation+"へ参集";
            } else {
                s = mMainStation+"消防署へ参集";
            }
        }
        builder.setMessage("２号非常招集\n\n"+s);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //震度４
    private void showEarthquake3(){
        final CharSequence[] actions = {"■大津波警報","■津波警報","■警報なし"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("発令されている警報は？");
        builder.setItems(actions, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                switch(which){
                    case 0:
                        showEarthquake31();
                        break;
                    case 1:
                        showEarthquake32();
                        break;
                    case 2:
                        showEarthquake33();
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showEarthquake31(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■大津波警報");
        String s;
        if (mTsunamiStation.equals("消防局")||mTsunamiStation.equals("教育訓練センター")){
            s = mTsunamiStation+"へ参集";
        } else {
            s = mTsunamiStation+"消防署へ参集";
        }
        builder.setMessage("１号非常招集\n\n"+s);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showEarthquake32(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■津波警報");
        //以下の勤務消防署に該当する場合は第5非常警備(大津波・津波警報時参集指定署ではないことに注意)、それ以外は3号招集
        String s, message;
        String[] a = {"天王寺","東淀川","東成","生野","阿倍野","東住吉","平野"};
        if (Arrays.asList(a).contains(mMainStation)){
            //４号招集なので、１号、２号、３号は参集なしの判定する
            if (mKubun.equals("４号招集")) {
                if (mMainStation.equals("消防局")||mMainStation.equals("教育訓練センター")) { //勤務消防署であることに注意!
                    s = mMainStation + "へ参集　所属担当者に確認すること\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
                } else {
                    s = mMainStation + "消防署へ参集\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
                }
            } else {
                s = "招集なし";
            }
            message = "４号非常招集(非番・日勤)\n(天王寺,東淀川,東成,生野,阿倍野,東住吉,平野)\n\n" + s + "\n\n招集なし";
        } else {
            //３号招集なので、１号、２号は参集なしの判定する
            if (mKubun.equals("１号招集") || mKubun.equals("２号招集")) {
                s = "招集なし";
            } else {
                if (mTsunamiStation.equals("消防局") || mTsunamiStation.equals("教育訓練センター")) {
                    s = mTsunamiStation + "へ参集\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
                } else {
                    s = mTsunamiStation + "消防署へ参集\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
                }
            }
            message = "３号非常招集(非番・日勤)\n(北,都島,福島,此花,中央,西,港,大正,浪速,西淀川,淀川,旭,城東,鶴見,住之江,住吉,西成,水上,消防局)\n\n" + s;
        }
        builder.setMessage(message);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showEarthquake33(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■警報なし");
        String s;
        //４号招集なので、１号、２号、３号は参集なしの判定する
        if (mKubun.equals("４号招集")) {
            if (mMainStation.equals("消防局")||mMainStation.equals("教育訓練センター")) { //勤務消防署であることに注意!
                s = mMainStation + "へ参集　所属担当者に確認すること\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
            } else {
                s = mMainStation + "消防署へ参集\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
            }
        } else {
            s = "招集なし";
        }
        builder.setMessage("４号非常招集(非番・日勤)\n\n" + s);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //震度３以下
    private void showEarthquake4(){
        final CharSequence[] actions = {"■大津波警報","■津波警報","■津波注意報","■警報なし"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("発令されている警報は？");
        builder.setItems(actions, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                switch(which){
                    case 0:
                        showEarthquake41();
                        break;
                    case 1:
                        showEarthquake42();
                        break;
                    case 2:
                        showEarthquake43();
                        break;
                    case 3:
                        showEarthquake44();
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showEarthquake41(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■大津波警報");
        String s;
        if (mTsunamiStation.equals("消防局")||mTsunamiStation.equals("教育訓練センター")){
            s = mTsunamiStation+"へ参集";
        } else {
            s = mTsunamiStation+"消防署へ参集";
        }
        builder.setMessage("１号非常招集\n\n"+s);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showEarthquake42(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■津波警報");
        //以下の勤務消防署に該当する場合は第5非常警備(大津波・津波警報時参集指定署ではないことに注意)、それ以外は3号招集
        String s, message;
        String[] a = {"天王寺","東淀川","東成","生野","阿倍野","東住吉","平野"};
        if (Arrays.asList(a).contains(mMainStation)){
            if (mMainStation.equals("消防局")||mMainStation.equals("教育訓練センター")) { //勤務消防署であることに注意!
                s = mMainStation;
            } else {
                s = mMainStation + "消防署";
            }
            message = "第５非常警備(全署、消防局)\n(天王寺,東淀川,東成,生野,阿倍野,東住吉,平野)\n\n" + s + "\n\n招集なし";
        } else {
            //３号招集なので、１号、２号は参集なしの判定する
            if (mKubun.equals("１号招集") || mKubun.equals("２号招集")) {
                s = "招集なし";
            } else {
                if (mTsunamiStation.equals("消防局") || mTsunamiStation.equals("教育訓練センター")) {
                    s = mTsunamiStation + "へ参集\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
                } else {
                    s = mTsunamiStation + "消防署へ参集\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
                }
            }
            message = "３号非常招集(非番・日勤)\n(北,都島,福島,此花,中央,西,港,大正,浪速,西淀川,淀川,旭,城東,鶴見,住之江,住吉,西成,水上,消防局)\n\n" + s;
        }
        builder.setMessage(message);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showEarthquake43(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■津波注意報");
        //勤務消防署がリストに該当するか判定
        String s;
        String[] a = {"北","都島","福島","此花","中央","西","港","大正","浪速","西淀川","淀川","旭","城東","鶴見","住之江","住吉","西成","水上","消防局"};
        if (Arrays.asList(a).contains(mMainStation)) {
            if (mMainStation.equals("消防局")||mMainStation.equals("教育訓練センター")) { //勤務消防署であることに注意!
                s = mMainStation;
            } else {
                s = mMainStation + "消防署";
            }
        } else {
            s = "招集なし";
        }
        builder.setMessage("第５非常警備\n(北,都島,福島,此花,中央,西,港,大正,浪速,西淀川,淀川,旭,城東,鶴見,住之江,住吉,西成,水上,消防局)\n\n"+s);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showEarthquake44(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■警報なし");
        builder.setMessage("招集なし\n\n");
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //東海地震に伴う非常招集
    private void showEarthquake5(){
        final CharSequence[] actions = {"■警戒宣言が発令されたとき（東海地震予知情報）","■東海地震注意報が発表されたとき","■東海地震に関連する調査情報（臨時）が発表されたとき"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("発令されている警報は？");
        builder.setItems(actions, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                switch(which){
                    case 0:
                        showEarthquake51();
                        break;
                    case 1:
                        showEarthquake52();
                        break;
                    case 2:
                        showEarthquake53();
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showEarthquake51(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■警戒宣言が発令されたとき（東海地震予知情報）");
        String s;
        //３号招集なので、１号、２号は参集なしの判定する
        if (mKubun.equals("１号招集")||mKubun.equals("２号招集")){
            s = "招集なし";
        } else {
            if (mMainStation.equals("消防局")||mMainStation.equals("教育訓練センター")) {
                s = mMainStation+"へ参集\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
            } else {
                s = mMainStation+"消防署へ参集\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
            }
        }
        builder.setMessage("３号非常招集(非番・日勤)\n\n"+s);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showEarthquake52(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■東海地震注意報が発表されたとき");
        String s;
        //４号招集なので、１号、２号、３号は参集なしの判定する
        if (mKubun.equals("４号招集")) {
            if (mMainStation.equals("消防局")||mMainStation.equals("教育訓練センター")) { //勤務消防署であることに注意!
                s = mMainStation + "へ参集　所属担当者に確認すること\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
            } else {
                s = mMainStation + "消防署へ参集\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
            }
        } else {
            s = "招集なし";
        }
        builder.setMessage("４号非常招集(非番・日勤)\n\n"+s);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showEarthquake53(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■東海地震に関連する調査情報（臨時）が発表されたとき");
        String s;
        if (mMainStation.equals("消防局")||mMainStation.equals("教育訓練センター")) { //勤務消防署であることに注意!
            s = mMainStation;
        } else {
            s = mMainStation + "消防署";
        }
        builder.setMessage("第５非常警備(全署、消防局)\n\n"+ s + "\n\n招集なし");
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //東海地震に伴う非常招集
    private void showEarthquake6(){
        final CharSequence[] actions = {"■調査中","■巨大地震注意","■巨大地震警戒"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("発令されている警報は？");
        builder.setItems(actions, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                switch(which){
                    case 0:
                        showEarthquake61();
                        break;
                    case 1:
                        showEarthquake62();
                        break;
                    case 2:
                        showEarthquake63();
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showEarthquake61(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■調査中");
        String s;
        if (mMainStation.equals("消防局")||mMainStation.equals("教育訓練センター")) { //勤務消防署であることに注意!
            s = mMainStation;
        } else {
            s = mMainStation + "消防署";
        }
        builder.setMessage("第５非常警備(全署、消防局)\n\n"+ s + "\n\n招集なし");
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showEarthquake62(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■巨大地震注意");
        String s;
        //４号招集なので、１号、２号、３号は参集なしの判定する
        if (mKubun.equals("４号招集")) {
            if (mMainStation.equals("消防局")||mMainStation.equals("教育訓練センター")) { //勤務消防署であることに注意!
                s = mMainStation + "へ参集　所属担当者に確認すること\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
            } else {
                s = mMainStation + "消防署へ参集\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
            }
        } else {
            s = "招集なし";
        }
        builder.setMessage("４号非常招集(非番・日勤)\n\n"+s);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showEarthquake63(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■巨大地震警戒");
        String s;
        //４号招集なので、１号、２号、３号は参集なしの判定する
        if (mKubun.equals("４号招集")) {
            if (mMainStation.equals("消防局")||mMainStation.equals("教育訓練センター")) { //勤務消防署であることに注意!
                s = mMainStation + "へ参集　所属担当者に確認すること\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
            } else {
                s = mMainStation + "消防署へ参集\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
            }
        } else {
            s = "招集なし";
        }
        builder.setMessage("４号非常招集(非番・日勤)\n\n"+s);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //情報（地震）
    private void showEarthquake(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("URLをタップしてください");
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.info_earthquake, (ViewGroup)findViewById(R.id.infoEarthquake));
        builder.setView(layout);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //情報（停電）
    private void showBlackout(){
        final CharSequence[] actions = {"■関西電力","■四国電力","■中国電力","■九州電力","■中部電力","■北陸電力","■東京電力","■東北電力"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("電力会社を選択してください");
        builder.setItems(actions, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                switch(which){
                    case 0:
                        showURL(R.layout.info_kanden, R.id.infoKanden);
                        break;
                    case 1:
                        showURL(R.layout.info_yonden, R.id.infoYonden);
                        break;
                    case 2:
                        showURL(R.layout.info_energia, R.id.infoEnergia);
                        break;
                    case 3:
                        showURL(R.layout.info_kyuden, R.id.infoKyuden);
                        break;
                    case 4:
                        showURL(R.layout.info_chuden, R.id.infoChuden);
                        break;
                    case 5:
                        showURL(R.layout.info_rikuden, R.id.infoRikuden);
                        break;
                    case 6:
                        showURL(R.layout.info_touden, R.id.infoTouden);
                        break;
                    case 7:
                        showURL(R.layout.info_touhokuden, R.id.infoTouhokuden);
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showURL(int xml, int id){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("URLをタップしてください");
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(xml, (ViewGroup)findViewById(id));
        builder.setView(layout);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //情報（道路）
    private void showRoad(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("URLをタップしてください");
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.info_road, (ViewGroup)findViewById(R.id.infoRoad));
        builder.setView(layout);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //連絡網データ表示
    private void showCheck(){
        //初回判定　２回目以降はスルー
        if (!mPassFlag) {
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
                        showTel();
                    }
                }
            });
            builder.setNegativeButton("キャンセル", null);
            builder.setCancelable(true);
            builder.create();
            builder.show();
        } else {
            showTel();
        }
    }

    private void showTel(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("連絡網");
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.tel_show2, (ViewGroup)findViewById(R.id.telShow2));
        //全件表示ボタン設定
        final Button btnAll = (Button)layout.findViewById(R.id.btnTel);
        btnAll.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                showTelAll();
            }
        });
        //検索条件取得準備
        final Spinner  editKubun = (Spinner)layout.findViewById(R.id.editKubun);
        final Spinner  editSyozoku = (Spinner)layout.findViewById(R.id.editSyozoku);
        final Spinner  editSyozoku2 = (Spinner)layout.findViewById(R.id.editSyozoku2);
        final Spinner  editKinmu = (Spinner)layout.findViewById(R.id.editKinmu);
        //親所属スピナー選択時の処理
        editSyozoku.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                //親所属スピナーの選択した位置をint取得
                int i = parent.getSelectedItemPosition();
                //Toast.makeText(mActivity, String.valueOf(i)+"番目を選択", Toast.LENGTH_SHORT).show();
                //取得したintを配列リソース名に変換し、配列リソースIDを取得（なぜか日本語ではエラーが出るのでアルファベットと数字で対応））
                mSelected = "firestationB"+ String.valueOf(i);
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
        builder.setView(layout);
        builder.setPositiveButton("検索", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                //スピナーをさわっていたら取得
                resKubun = (String)editKubun.getSelectedItem();
                resSyozoku0 = (String)editSyozoku.getSelectedItem();
                resSyozoku = (String)editSyozoku2.getSelectedItem();
                resKinmu = (String)editKinmu.getSelectedItem();
                showTelResult(resKubun, resSyozoku0, resSyozoku, resKinmu);
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showTelAll(){
        //データ準備
        mailArray.clear(); //前回の残りを消去しておく
        final String order = "select * from records order by _id";
        final Cursor c = mActivity.db.rawQuery(order, null);
        String[] from = {"name", "tel", "mail", "kubun", "syozoku0","syozoku", "kinmu"};
        int[] to = {R.id.record_name, R.id.record_tel, R.id.record_mail, R.id.record_kubun, R.id.record_syozoku0, R.id.record_syozoku, R.id.record_kinmu};
        //初回のみ起動。そうしないと、すべて選択した後の２回目がまたnewされて意味ない
        if (mAdapter2 == null) {
            mActivity.mAdapter2 = new CustomCursorAdapter(mActivity, R.layout.record_view2, c, from, to, 0);
        }
        mListView.setAdapter(mActivity.mAdapter2);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                //タップした位置のデータをチェック処理
                mActivity.mAdapter2.clickData(position, view);
            }
        });
        //ダイアログ生成
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("連絡網データ\n(メールはチェックしてから送信)");
        ViewGroup parent = (ViewGroup)mListView.getParent();
        if ( parent!=null) {
            parent.removeView(mListView);
        }
        builder.setView(mListView);
        builder.setPositiveButton("メール送信", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                //第一段階　メール送信対象リストに格納
                //CustomCursorAdapterのメンバ変数であるitemCheckedを見に行く
                c.moveToFirst(); //カーソルを先頭に
                for (int i=0; i < mAdapter2.itemChecked.size(); i++){
                    //チェックされていたら対応するカーソルのmailアドレス文字列をメール送信対象文字列に格納
                    if (mAdapter2.itemChecked.get(i)){
                        mailArray.add(c.getString(c.getColumnIndex("mail")));
                    }
                    c.moveToNext();
                }
                //第二段階　メールアプリのsendに渡す文字列にArrayListに格納した各アドレスを格納
                final String[] sendMails = new String[mailArray.size()];
                for (int i=0; i < mailArray.size(); i++){
                    sendMails[i] = mailArray.get(i);
                }
                //メール立ち上げ
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, sendMails);
                intent.putExtra(Intent.EXTRA_SUBJECT, "参集アプリ　一斉送信メール");
                intent.putExtra(Intent.EXTRA_TEXT, "緊急連絡");
                try {
                    startActivity(Intent.createChooser(intent, "メールアプリを選択"));
                } catch (android.content.ActivityNotFoundException ex){
                    Toast.makeText(mActivity, "メールアプリが見つかりません", Toast.LENGTH_LONG).show();
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
                showTelAll();
            }
        });
        builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                mailArray.clear(); //きちんと後片付け
                mAdapter2 = null;
            }
        });
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showTelResult(String _kubun, String _syozoku0, String _syozoku, String _kinmu){
        //データ準備
        mailArray.clear(); //前回の残りを消去
        //再帰するときにfinalで使用するため別変数にして保存
        final String kubun2 = _kubun;
        final String syozoku02 = _syozoku0;
        final String syozoku2 = _syozoku;
        final String kinmu2 = _kinmu;
        //ここからSQL文作成
        String kubun;
        if (_kubun.equals("すべて")){
            kubun = "is not null";
        } else {
            kubun = "='" + _kubun + "'";
        }
        String syozoku0;
        if (_syozoku0.equals("すべて")){
            syozoku0 = "is not null";
        } else {
            syozoku0 = "='" + _syozoku0 + "'";
        }
        String syozoku;
        if (_syozoku.equals("すべて")){
            syozoku = "is not null";
        } else {
            syozoku = "='" + _syozoku + "'";
        }
        String kinmu;
        if (_kinmu.equals("すべて")){
            kinmu = "is not null";
        } else {
            kinmu = "='" + _kinmu + "'";
        }
        final String order = "select * from records where kubun " + kubun + " and syozoku0 " + syozoku0 + " and syozoku " + syozoku + " and kinmu " + kinmu + " order by _id";
        final Cursor c = mActivity.db.rawQuery(order, null);
        String[] from = {"name", "tel", "mail", "kubun", "syozoku0", "syozoku", "kinmu"};
        int[] to = {R.id.record_name, R.id.record_tel, R.id.record_mail, R.id.record_kubun, R.id.record_syozoku0, R.id.record_syozoku, R.id.record_kinmu};
        //初回のみ起動。そうしないと、すべて選択した後の２回目がまたnewされて意味ない
        if (mAdapter2 == null) {
            mActivity.mAdapter2 = new CustomCursorAdapter(mActivity, R.layout.record_view2, c, from, to, 0);
        }
        mListView.setAdapter(mActivity.mAdapter2);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                //タップした位置のデータをチェック処理
                mActivity.mAdapter2.clickData(position, view);
            }
        });
        //ダイアログ生成
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("連絡網データ\n (メールはチェックしてから送信)");
        ViewGroup parent = (ViewGroup)mListView.getParent();
        if ( parent!=null) {
            parent.removeView(mListView);
        }
        builder.setView(mListView);
        builder.setPositiveButton("メール送信", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                //第一段階　メール送信対象リストに格納
                //CustomCursorAdapterのメンバ変数であるitemCheckedを見に行く
                c.moveToFirst(); //カーソルを先頭に
                for (int i=0; i < mAdapter2.itemChecked.size(); i++){
                    //チェックされていたら対応するカーソルのmailアドレス文字列をメール送信対象文字列に格納
                    if (mAdapter2.itemChecked.get(i)){
                        mailArray.add(c.getString(c.getColumnIndex("mail")));
                    }
                    c.moveToNext();
                }
                //第二段階　メールアプリのsendに渡す文字列にArrayListに格納した各アドレスを格納
                final String[] sendMails = new String[mailArray.size()];
                for (int i=0; i < mailArray.size(); i++){
                    sendMails[i] = mailArray.get(i);
                }
                //メール立ち上げ
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, sendMails);
                intent.putExtra(Intent.EXTRA_SUBJECT, "参集アプリ　一斉送信メール");
                intent.putExtra(Intent.EXTRA_TEXT, "緊急連絡");
                try {
                    startActivity(Intent.createChooser(intent, "メールアプリを選択"));
                } catch (android.content.ActivityNotFoundException ex){
                    Toast.makeText(mActivity, "メールアプリが見つかりません", Toast.LENGTH_LONG).show();
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
                showTelResult(kubun2, syozoku02, syozoku2, kinmu2);
            }
        });
        builder.setNegativeButton("戻る", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                mailArray.clear(); //きちんと後片付け
                mAdapter2 = null;
                //前の画面に戻る
                showTel();
            }
        });
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //留意事項
    public void showCaution(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("留意事項");
        //テキストファイル読み込み
        InputStream is = null;
        BufferedReader br = null;
        String text = "";
        try {
            try {
                //assetsフォルダ内のテキスト読み込み
                is = getAssets().open("caution.txt");
                br = new BufferedReader(new InputStreamReader(is));
                //１行づつ読み込み、改行追加
                String str;
                while((str = br.readLine()) !=null){
                    text += str + "\n";
                }
            } finally {
                if (is != null) is.close();
                if (br != null) br.close();
            }
        } catch (Exception e) {
            //エラーメッセージ
            Toast.makeText(this, "テキスト読込エラー", Toast.LENGTH_LONG).show();
        }
        builder.setMessage(text);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //防災ネット
    private void showBousaiNet(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("URLまたはボタンをタップしてください");
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.info_osaka, (ViewGroup)findViewById(R.id.infoOsaka));
        //ボタン クリックリスナー設定
        layout.findViewById(R.id.btnOsakaBousaiApp).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                startOsakaBousaiApp();
            }
        });
        builder.setView(layout);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //大阪市防災アプリ
    public void startOsakaBousaiApp() {
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage("jp.ne.goo.bousai.osakaapp");
        try {
            startActivity(intent);
        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("大阪市防災アプリがありません");
            //カスタムビュー設定
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.info_osakabousaiapp, (ViewGroup) findViewById(R.id.infoOsakaBousai));
            builder.setView(layout);
            builder.setNegativeButton("キャンセル", null);
            builder.setCancelable(true);
            builder.create();
            builder.show();
        }
    }

}
