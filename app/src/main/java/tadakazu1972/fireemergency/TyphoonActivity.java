package tadakazu1972.fireemergency;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.TextView;
import android.widget.Toast;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

//import android.database.sqlite.SQLiteDatabase;

public class TyphoonActivity extends AppCompatActivity {
    protected TyphoonActivity mActivity = null;
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
    //ダイアログ制御用
    private AlertDialog mDlg;
    private Boolean gaitousyo_visible = false; //該当署の表示フラグ
    //まとめてメール送信用
    private ArrayList<String> mailArray;
    //連絡網初回パスワード要求フラグ
    public boolean mPassFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this;
        mView = this.getWindow().getDecorView();
        setContentView(R.layout.activity_typhoon);
        initButtons();
        //基礎データ読み込み
        loadData();
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
    protected void onResume() {
        super.onResume();
        //基礎データを変更してActivity復帰した際に反映させないと前のままなので
        loadData();
    }

    //ボタン設定
    private void initButtons() {
        mView.findViewById(R.id.btnData).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, DataActivity.class);
                startActivity(intent);
            }
        });
        mView.findViewById(R.id.btnEarthquake).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, EarthquakeActivity.class);
                startActivity(intent);
            }
        });
        mView.findViewById(R.id.btnKokuminhogo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, KokuminhogoActivity.class);
                startActivity(intent);
            }
        });
        mView.findViewById(R.id.btnKinentai).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, KinentaiActivity.class);
                startActivity(intent);
            }
        });
        mView.findViewById(R.id.btnTyphoon1).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showTyphoon1();
            }
        });
        mView.findViewById(R.id.btnTyphoon2).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showTyphoon2();
            }
        });
        mView.findViewById(R.id.btnTyphoon3).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showTyphoon3();
            }
        });
        mView.findViewById(R.id.btnTyphoonWeather).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showWeather();
            }
        });
        mView.findViewById(R.id.btnTyphoonRiver).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showRiver();
            }
        });
        mView.findViewById(R.id.btnTyphoonRoad).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showRoad();
            }
        });
        mView.findViewById(R.id.btnTyphoonTel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showCheck();
            }
        });
        mView.findViewById(R.id.btnTyphoonCaution).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showCaution();
            }
        });
        mView.findViewById(R.id.btnTyphoonBousaiNet).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showBousaiNet();
            }
        });
    }

    //基礎データ読み込み
    private void loadData() {
        //勤務消防署
        SharedPreferences sp1 = PreferenceManager.getDefaultSharedPreferences(this);
        mMainStation = sp1.getString("mainStation", "消防局"); // 第２引数はkeyが存在しない時に返す初期値
        //大津波・津波警報時指定署
        SharedPreferences sp2 = PreferenceManager.getDefaultSharedPreferences(this);
        mTsunamiStation = sp2.getString("tsunamiStation", "消防局"); // 第２引数はkeyが存在しない時に返す初期値
        //招集区分
        SharedPreferences sp3 = PreferenceManager.getDefaultSharedPreferences(this);
        mKubun = sp3.getString("kubun", "１"); // 第２引数はkeyが存在しない時に返す初期値
    }

    //非常警備の基準（全て）
    private void showTyphoon1() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("非常警備の基準（全て）");
        //テキストファイル読み込み
        InputStream is = null;
        BufferedReader br = null;
        String text = "";
        try {
            try {
                //assetsフォルダ内のテキスト読み込み
                is = getAssets().open("typhoon1.txt");
                br = new BufferedReader(new InputStreamReader(is));
                //１行づつ読み込み、改行追加
                String str;
                while ((str = br.readLine()) != null) {
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

    //気象警報による非常警備
    private void showTyphoon2() {
        final CharSequence[] actions = {"■特別警報", "■暴風（雪）警報", "■大雨警報", "■大雪警報", "■洪水警報", "■波浪警報", "■高潮警報", "■高潮注意報"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("発令されている警報は？");
        builder.setItems(actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        showTyphoon21();
                        break;
                    case 1:
                        showTyphoon22();
                        break;
                    case 2:
                        showTyphoon23();
                        break;
                    case 3:
                        showTyphoon24();
                        break;
                    case 4:
                        showTyphoon25();
                        break;
                    case 5:
                        showTyphoon26();
                        break;
                    case 6:
                        showTyphoon27();
                        break;
                    case 7:
                        showTyphoon28();
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showTyphoon21() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■特別警報");
        String s;
        if (mMainStation.equals("消防局")||mMainStation.equals("教育訓練センター")) {
            s = mMainStation + "へ参集　所属担当者に確認すること";
        } else {
            s = mMainStation + "消防署へ参集";
        }
        builder.setMessage("１号非常招集\n\n" + s);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showTyphoon22() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■暴風（雪）警報");
        //４号招集なので、１号、２号、３号は参集なしの判定する
        String s;
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

    private void showTyphoon23() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■大雨警報");
        String s;
        if (mMainStation.equals("教育訓練センター")) {
            s = "ー";
        } else if (mMainStation.equals("消防局")) {
            s = mMainStation;
        } else {
            s = mMainStation + "消防署";
        }
        builder.setMessage("第５非常警備(全署、消防局)\n\n" + s + "\n\n招集なし");
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showTyphoon24() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■大雪警報");
        String s;
        if (mMainStation.equals("教育訓練センター")) {
            s = "ー";
        } else if (mMainStation.equals("消防局")) {
            s = mMainStation;
        } else {
            s = mMainStation + "消防署";
        }
        builder.setMessage("第５非常警備(全署、消防局)\n\n" + s + "\n\n招集なし");
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showTyphoon25() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■洪水警報");
        //勤務消防署がリストに該当するか判定
        String s;
        String[] a = {"北", "都島", "福島", "此花", "中央", "西淀川", "淀川", "東淀川", "東成", "生野", "旭", "城東", "鶴見", "住之江", "住吉", "東住吉", "平野", "消防局"};
        if (Arrays.asList(a).contains(mMainStation)) {
            if (mMainStation.equals("消防局")) {
                s = mMainStation;
            } else {
                s = mMainStation + "消防署";
            }
        } else {
            s = "ー";
        }
        builder.setMessage("第５非常警備(北、都島、福島、此花、中央、西淀川、淀川、東淀川、東成、生野、旭、城東、鶴見、住之江、住吉、東住吉、平野、消防局)\n\n" + s + "\n\n招集なし");
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showTyphoon26() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■波浪警報");
        //勤務消防署がリストに該当するか判定
        String s;
        String[] a = {"此花", "港", "大正", "西淀川", "住之江", "水上", "消防局"};
        if (Arrays.asList(a).contains(mMainStation)) {
            if (mMainStation.equals("消防局")||mMainStation.equals("教育訓練センター")) {
                s = mMainStation;
            } else {
                s = mMainStation + "消防署";
            }
        } else {
            s = "ー";
        }
        builder.setMessage("第５非常警備(此花、港、大正、西淀川、住之江、水上、消防局)\n\n" + s + "\n\n招集なし");
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showTyphoon27() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■高潮警報");
        //勤務消防署がリストに該当するか判定
        String s;
        String[] a = {"北", "都島", "福島", "此花", "中央", "西", "港", "大正", "浪速", "西淀川", "淀川", "住之江", "西成", "水上", "消防局"};
        if (Arrays.asList(a).contains(mMainStation)) {
            if (mMainStation.equals("消防局")) {
                s = mMainStation;
            } else {
                s = mMainStation + "消防署";
            }
        } else {
            s = "ー";
        }
        builder.setMessage("第５非常警備(北、都島、福島、此花、中央、西、港、大正、浪速、西淀川、淀川、住之江、西成、水上、消防局)\n\n" + s + "\n\n招集なし");
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void showTyphoon28() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■高潮注意報");
        //勤務消防署がリストに該当するか判定
        String s;
        String[] a = {"北", "都島", "福島", "此花", "中央", "西", "港", "大正", "浪速", "西淀川", "淀川", "住之江", "西成", "水上", "消防局"};
        if (Arrays.asList(a).contains(mMainStation)) {
            if (mMainStation.equals("消防局")) {
                s = mMainStation;
            } else {
                s = mMainStation + "消防署";
            }
        } else {
            s = "ー";
        }
        builder.setMessage("第５非常警備(北、都島、福島、此花、中央、西、港、大正、浪速、西淀川、淀川、住之江、西成、水上、消防局)\n\n" + s + "\n\n招集なし");
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //河川水位による非常警備
    private void showTyphoon3() {
        final CharSequence[] actions = {"■淀川（枚方）", "■大和川（柏原）", "■神崎川（三国）", "■安威川（千歳橋）", "■寝屋川（京橋）", "■第二寝屋川（昭明橋）", "■平野川（剣橋）", "■平野川分水路（今里大橋）", "■古川（桑才）", "■東除川（大堀上小橋）","■高潮区域（水防警報（出動））"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("河川を選択してください");
        builder.setItems(actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        showTyphoon31();
                        break;
                    case 1:
                        showTyphoon32();
                        break;
                    case 2:
                        showTyphoon33();
                        break;
                    case 3:
                        showTyphoon34();
                        break;
                    case 4:
                        showTyphoon35();
                        break;
                    case 5:
                        showTyphoon36();
                        break;
                    case 6:
                        showTyphoon37();
                        break;
                    case 7:
                        showTyphoon38();
                        break;
                    case 8:
                        showTyphoon39();
                        break;
                    case 9:
                        showTyphoon3A();
                        break;
                    case 10:
                        showTyphoon3B();
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //淀川（枚方）
    private void showTyphoon31() {
        final CharSequence[] actions = {"■氾濫注意水位(水位4.5m)、水防警報(出動)", "■避難準備・高齢者等避難開始発令の見込み(1時間以内に水位5.4mに到達)", "■避難準備・高齢者等避難開始(水位5.4m)、避難勧告発令の見込み", "■避難勧告(水位5.5m)", "■避難指示(緊急)(水位8.3m)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("水位の状況は？");
        builder.setItems(actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:{
                        String[] a = {"北", "都島", "福島", "此花", "西淀川", "淀川", "東淀川", "旭", "消防局"};
                        String gaitousyo = "北,都島,福島,此花,西淀川,淀川,東淀川,旭,消防局";
                        showTyphoonRiver1("■淀川（枚方） 氾濫注意水位(水位4.5m)、水防警報(出動)", a, gaitousyo, 1);
                        break;}
                    case 1:{
                        String[] a = {"北", "都島", "福島", "此花", "西淀川", "淀川", "東淀川", "旭", "消防局"};
                        String gaitousyo = "４号：北,都島,福島,此花,西淀川,淀川,東淀川,旭,消防局";
                        showTyphoonRiver2("■淀川（枚方） 避難準備・高齢者等避難開始発令の見込み(1時間以内に水位5.4mに到達)", a, gaitousyo, 1);
                        break;}
                    case 2:{
                        String[] a = {"北", "都島", "福島", "此花", "西淀川", "淀川", "東淀川", "旭", "消防局"};
                        String[] b = {"城東", "鶴見"};
                        String gaitousyo = "流域署３号：北,都島,福島,此花,西淀川,淀川,東淀川,旭,消防局\n流域周辺署４号：城東,鶴見";
                        showTyphoonRiver3("■淀川（枚方） 避難準備・高齢者等避難開始(水位5.4m)", a, b, gaitousyo, 1);
                        break;}
                    case 3:{
                        String[] a = {"北", "都島", "福島", "此花", "西淀川", "淀川", "東淀川", "旭", "消防局"};
                        String[] b = {"城東", "鶴見"};
                        String[] c = {"中央", "西", "港", "大正", "天王寺", "浪速", "東成", "生野", "阿倍野", "住之江", "住吉", "東住吉", "平野", "西成", "水上"};
                        String gaitousyo = "流域署２号：北,都島,福島,此花,西淀川,淀川,東淀川,旭,消防局\n流域周辺署３号：城東,鶴見\nその他の署４号：中央,西,港,大正,天王寺,浪速,東成,生野,阿倍野,住之江,住吉,東住吉,平野,西成,水上";
                        showTyphoonRiver4("■淀川（枚方） 避難勧告(水位5.5m)", a, b, c, gaitousyo, 1);
                        break;}
                    case 4:{
                        String gaitousyo = "２号：全署、消防局";
                        showTyphoonRiver5("■淀川（枚方） 避難指示(緊急)(水位8.3m)", gaitousyo, 1);
                        break;}
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //河川　氾濫注意水位
    private void showTyphoonRiver1(String title, String[] a, String gaitousyo, int number) {
        final int i = number;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_river, (ViewGroup) findViewById(R.id.dlgRiver));
        //テキスト放りこみ先準備
        final TextView text1 = (TextView)layout.findViewById(R.id.txtRiver1);
        final TextView text2 = (TextView)layout.findViewById(R.id.txtRiver2);
        final TextView text3 = (TextView)layout.findViewById(R.id.txtRiver3);
        //放り込むテキストの設定
        //勤務消防署がリストに該当するか判定
        String s1;
        String s2;
        final String s3 = gaitousyo;
        s1 = "第５非常警備";
        if (Arrays.asList(a).contains(mMainStation)) {
            if (mMainStation.equals("消防局")) {
                s2 = mMainStation;
            } else {
                s2 = mMainStation + "消防署";
            }
        } else {
            s2 = "ー";
        }
        s2 = s2 + "\n\n招集なし";
        //テキストセット
        text1.setText(s1);
        text2.setText(s2);
        //ボタン クリックリスナー設定
        layout.findViewById(R.id.btnGaitousyo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gaitousyo_visible) {
                    text3.setText(s3);
                    gaitousyo_visible = true;
                } else {
                    text3.setText("");
                    gaitousyo_visible = false;
                }
            }
        });
        builder.setTitle(title);
        builder.setView(layout);
        builder.setPositiveButton("戻る", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (i) {
                    case 1:
                        showTyphoon31();
                        break;
                    case 2:
                        showTyphoon32();
                        break;
                    case 3:
                        showTyphoon33();
                        break;
                    case 4:
                        showTyphoon34();
                        break;
                    case 5:
                        showTyphoon35();
                        break;
                    case 6:
                        showTyphoon36();
                        break;
                    case 7:
                        showTyphoon37();
                        break;
                    case 8:
                        showTyphoon38();
                        break;
                    case 9:
                        showTyphoon39();
                        break;
                    case 10:
                        showTyphoon3A();
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //河川　条件１つの場合
    private void showTyphoonRiver2(String title, String[] a, String gaitousyo, int number) {
        final int i = number;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_river, (ViewGroup) findViewById(R.id.dlgRiver));
        //テキスト放りこみ先準備
        final TextView text1 = (TextView)layout.findViewById(R.id.txtRiver1);
        final TextView text2 = (TextView)layout.findViewById(R.id.txtRiver2);
        final TextView text3 = (TextView)layout.findViewById(R.id.txtRiver3);
        //放り込むテキストの設定
        //勤務消防署がリストに該当するか判定
        String s1;
        String s2;
        final String s3 = gaitousyo;
        if (Arrays.asList(a).contains(mMainStation)) {
            //４号招集なので、１号、２号、３号は参集なしの判定する
            if (mKubun.equals("４号招集")) {
                s1 = "４号非常招集(非番・日勤)";
                if (mMainStation.equals("消防局")) {
                    s2 = mMainStation + "へ参集(所属担当者に確認すること)\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
                } else {
                    s2 = mMainStation + "消防署へ参集\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
                }
            } else {
                s1 = "招集なし";
                s2 = "";
            }
        } else {
            s1 = "招集なし";
            s2 = "";
        }
        //テキストセット
        text1.setText(s1);
        text2.setText(s2);
        //ボタン クリックリスナー設定
        layout.findViewById(R.id.btnGaitousyo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gaitousyo_visible) {
                    text3.setText(s3);
                    gaitousyo_visible = true;
                } else {
                    text3.setText("");
                    gaitousyo_visible = false;
                }
            }
        });
        builder.setTitle(title);
        builder.setView(layout);
        builder.setPositiveButton("戻る", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (i) {
                    case 1:
                        showTyphoon31();
                        break;
                    case 2:
                        showTyphoon32();
                        break;
                    case 3:
                        showTyphoon33();
                        break;
                    case 4:
                        showTyphoon34();
                        break;
                    case 5:
                        showTyphoon35();
                        break;
                    case 6:
                        showTyphoon36();
                        break;
                    case 7:
                        showTyphoon37();
                        break;
                    case 8:
                        showTyphoon38();
                        break;
                    case 9:
                        showTyphoon39();
                        break;
                    case 10:
                        showTyphoon3A();
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //河川　避難準備情報
    private void showTyphoonRiver3(String title, String[] a, String[] b, String gaitousyo, int number) {
        final int i = number;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_river, (ViewGroup) findViewById(R.id.dlgRiver));
        //テキスト放りこみ先準備
        final TextView text1 = (TextView)layout.findViewById(R.id.txtRiver1);
        final TextView text2 = (TextView)layout.findViewById(R.id.txtRiver2);
        final TextView text3 = (TextView)layout.findViewById(R.id.txtRiver3);
        //放り込むテキストの設定
        //勤務消防署がリストに該当するか判定
        String s1;
        String s2;
        final String s3 = gaitousyo;
        if (Arrays.asList(a).contains(mMainStation)) {
            //３号招集なので、１号、２号は参集なしの判定する
            if (mKubun.equals("３号招集") || mKubun.equals("４号招集")) {
                s1 = "３号非常招集(非番・日勤)";
                if (mMainStation.equals("消防局")) {
                    s2 = mMainStation + "へ参集(所属担当者に確認すること)\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
                } else {
                    s2 = mMainStation + "消防署へ参集\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
                }
            } else {
                s1 = "招集なし";
                s2 = "";
            }
        } else if (Arrays.asList(b).contains(mMainStation)) {
            //４号招集なので、１号、２号、３号は参集なしの判定する
            if (mKubun.equals("４号招集")) {
                s1 = "４号非常招集(非番・日勤)";
                if (mMainStation.equals("消防局")) {
                    s2 = mMainStation + "へ参集(所属担当者に確認すること)\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
                } else {
                    s2 = mMainStation + "消防署へ参集\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
                }
            } else {
                s1 = "招集なし";
                s2 = "";
            }
        } else {
            s1 = "招集なし";
            s2 = "";
        }
        //テキストセット
        text1.setText(s1);
        text2.setText(s2);
        //ボタン クリックリスナー設定
        layout.findViewById(R.id.btnGaitousyo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gaitousyo_visible) {
                    text3.setText(s3);
                    gaitousyo_visible = true;
                } else {
                    text3.setText("");
                    gaitousyo_visible = false;
                }
            }
        });
        builder.setTitle(title);
        builder.setView(layout);
        builder.setPositiveButton("戻る", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (i) {
                    case 1:
                        showTyphoon31();
                        break;
                    case 2:
                        showTyphoon32();
                        break;
                    case 3:
                        showTyphoon33();
                        break;
                    case 4:
                        showTyphoon34();
                        break;
                    case 5:
                        showTyphoon35();
                        break;
                    case 6:
                        showTyphoon36();
                        break;
                    case 7:
                        showTyphoon37();
                        break;
                    case 8:
                        showTyphoon38();
                        break;
                    case 9:
                        showTyphoon39();
                        break;
                    case 10:
                        showTyphoon3A();
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //河川　避難準備情　カスタマイズ版　３号しかない　神崎川、東除川
    private void showTyphoonRiver3b(String title, String[] a, String gaitousyo, int number) {
        final int i = number;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_river, (ViewGroup) findViewById(R.id.dlgRiver));
        //テキスト放りこみ先準備
        final TextView text1 = (TextView)layout.findViewById(R.id.txtRiver1);
        final TextView text2 = (TextView)layout.findViewById(R.id.txtRiver2);
        final TextView text3 = (TextView)layout.findViewById(R.id.txtRiver3);
        //放り込むテキストの設定
        //勤務消防署がリストに該当するか判定
        String s1;
        String s2;
        final String s3 = gaitousyo;
        if (Arrays.asList(a).contains(mMainStation)) {
            //３号招集なので、１号、２号は参集なしの判定する
            if (mKubun.equals("３号招集") || mKubun.equals("４号招集")) {
                s1 = "３号非常招集(非番・日勤)";
                if (mMainStation.equals("消防局")) {
                    s2 = mMainStation + "へ参集(所属担当者に確認すること)\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
                } else {
                    s2 = mMainStation + "消防署へ参集\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
                }
            } else {
                s1 = "招集なし";
                s2 = "";
            }
        } else {
            s1 = "招集なし";
            s2 = "";
        }
        //テキストセット
        text1.setText(s1);
        text2.setText(s2);
        //ボタン クリックリスナー設定
        layout.findViewById(R.id.btnGaitousyo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gaitousyo_visible) {
                    text3.setText(s3);
                    gaitousyo_visible = true;
                } else {
                    text3.setText("");
                    gaitousyo_visible = false;
                }
            }
        });
        builder.setTitle(title);
        builder.setView(layout);
        builder.setPositiveButton("戻る", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (i) {
                    case 1:
                        showTyphoon31();
                        break;
                    case 2:
                        showTyphoon32();
                        break;
                    case 3:
                        showTyphoon33();
                        break;
                    case 4:
                        showTyphoon34();
                        break;
                    case 5:
                        showTyphoon35();
                        break;
                    case 6:
                        showTyphoon36();
                        break;
                    case 7:
                        showTyphoon37();
                        break;
                    case 8:
                        showTyphoon38();
                        break;
                    case 9:
                        showTyphoon39();
                        break;
                    case 10:
                        showTyphoon3A();
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //河川　避難勧告
    private void showTyphoonRiver4(String title, String[] a, String[] b, String[] c, String gaitousyo, int number) {
        final int i = number;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_river, (ViewGroup) findViewById(R.id.dlgRiver));
        //テキスト放りこみ先準備
        final TextView text1 = (TextView)layout.findViewById(R.id.txtRiver1);
        final TextView text2 = (TextView)layout.findViewById(R.id.txtRiver2);
        final TextView text3 = (TextView)layout.findViewById(R.id.txtRiver3);
        //放り込むテキストの設定
        //勤務消防署がリストに該当するか判定
        String s1;
        String s2;
        final String s3 = gaitousyo;
        if (Arrays.asList(a).contains(mMainStation)) {
            //２号招集なので、１号は参集なしの判定する
            if (mKubun.equals("１号招集")) {
                s1 = "招集なし";
                s2 = "";
            } else {
                s1 = "２号非常招集";
                if (mMainStation.equals("消防局")) {
                    s2 = mMainStation + "へ参集(所属担当者に確認すること)";
                } else {
                    s2 = mMainStation + "消防署へ参集";
                }
            }
        } else if (Arrays.asList(b).contains(mMainStation)){
            //３号招集なので、１号、２号は参集なしの判定する
            if (mKubun.equals("３号招集") || mKubun.equals("４号招集")) {
                s1 = "３号非常招集(非番・日勤)";
                if (mMainStation.equals("消防局")) {
                    s2 = mMainStation + "へ参集(所属担当者に確認すること)\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
                } else {
                    s2 = mMainStation + "消防署へ参集\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
                }
            } else {
                s1 = "招集なし";
                s2 = "";
            }
        } else if (Arrays.asList(c).contains(mMainStation)) {
            //４号招集なので、１号、２号、３号は参集なしの判定する
            if (mKubun.equals("４号招集")) {
                s1 = "４号非常招集(非番・日勤)";
                if (mMainStation.equals("消防局")) {
                    s2 = mMainStation + "へ参集(所属担当者に確認すること)\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
                } else {
                    s2 = mMainStation + "消防署へ参集\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
                }
            } else {
                s1 = "招集なし";
                s2 = "";
            }
        } else {
            s1 = "招集なし";
            s2 = "";
        }
        //テキストセット
        text1.setText(s1);
        text2.setText(s2);
        //ボタン クリックリスナー設定
        layout.findViewById(R.id.btnGaitousyo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gaitousyo_visible) {
                    text3.setText(s3);
                    gaitousyo_visible = true;
                } else {
                    text3.setText("");
                    gaitousyo_visible = false;
                }
            }
        });
        builder.setTitle(title);
        builder.setView(layout);
        builder.setPositiveButton("戻る", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (i) {
                    case 1:
                        showTyphoon31();
                        break;
                    case 2:
                        showTyphoon32();
                        break;
                    case 3:
                        showTyphoon33();
                        break;
                    case 4:
                        showTyphoon34();
                        break;
                    case 5:
                        showTyphoon35();
                        break;
                    case 6:
                        showTyphoon36();
                        break;
                    case 7:
                        showTyphoon37();
                        break;
                    case 8:
                        showTyphoon38();
                        break;
                    case 9:
                        showTyphoon39();
                        break;
                    case 10:
                        showTyphoon3A();
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //河川　避難勧　カスタマイズ版　２号、４号判定　神崎川、東除川
    private void showTyphoonRiver4b(String title, String[] a, String[] b, String gaitousyo, int number) {
        final int i = number;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_river, (ViewGroup) findViewById(R.id.dlgRiver));
        //テキスト放りこみ先準備
        final TextView text1 = (TextView)layout.findViewById(R.id.txtRiver1);
        final TextView text2 = (TextView)layout.findViewById(R.id.txtRiver2);
        final TextView text3 = (TextView)layout.findViewById(R.id.txtRiver3);
        //放り込むテキストの設定
        //勤務消防署がリストに該当するか判定
        String s1;
        String s2;
        final String s3 = gaitousyo;
        if (Arrays.asList(a).contains(mMainStation)) {
            //２号招集なので、１号は参集なしの判定する
            if (mKubun.equals("１号招集")) {
                s1 = "招集なし";
                s2 = "";
            } else {
                s1 = "２号非常招集";
                if (mMainStation.equals("消防局")) {
                    s2 = mMainStation + "へ参集(所属担当者に確認すること)";
                } else {
                    s2 = mMainStation + "消防署へ参集";
                }
            }
        } else if (Arrays.asList(b).contains(mMainStation)) {
            //４号招集なので、１号、２号、３号は参集なしの判定する
            if (mKubun.equals("４号招集")) {
                s1 = "４号非常招集(非番・日勤)";
                if (mMainStation.equals("消防局")) {
                    s2 = mMainStation + "へ参集(所属担当者に確認すること)\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
                } else {
                    s2 = mMainStation + "消防署へ参集\n\n※平日の9時～17時30分は、原則、勤務中の毎日勤務者で活動体制を確保する";
                }
            } else {
                s1 = "招集なし";
                s2 = "";
            }
        } else {
            s1 = "招集なし";
            s2 = "";
        }
        //テキストセット
        text1.setText(s1);
        text2.setText(s2);
        //ボタン クリックリスナー設定
        layout.findViewById(R.id.btnGaitousyo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gaitousyo_visible) {
                    text3.setText(s3);
                    gaitousyo_visible = true;
                } else {
                    text3.setText("");
                    gaitousyo_visible = false;
                }
            }
        });
        builder.setTitle(title);
        builder.setView(layout);
        builder.setPositiveButton("戻る", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (i) {
                    case 1:
                        showTyphoon31();
                        break;
                    case 2:
                        showTyphoon32();
                        break;
                    case 3:
                        showTyphoon33();
                        break;
                    case 4:
                        showTyphoon34();
                        break;
                    case 5:
                        showTyphoon35();
                        break;
                    case 6:
                        showTyphoon36();
                        break;
                    case 7:
                        showTyphoon37();
                        break;
                    case 8:
                        showTyphoon38();
                        break;
                    case 9:
                        showTyphoon39();
                        break;
                    case 10:
                        showTyphoon3A();
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //河川　全署
    private void showTyphoonRiver5(String title, String gaitousyo, int number) {
        final int i = number;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_river, (ViewGroup) findViewById(R.id.dlgRiver));
        //テキスト放りこみ先準備
        final TextView text1 = (TextView)layout.findViewById(R.id.txtRiver1);
        final TextView text2 = (TextView)layout.findViewById(R.id.txtRiver2);
        final TextView text3 = (TextView)layout.findViewById(R.id.txtRiver3);
        //放り込むテキストの設定
        //勤務消防署がリストに該当するか判定
        String s1;
        String s2;
        final String s3 = gaitousyo;
        //２号招集なので、１号は参集なしの判定する
        if (mKubun.equals("１号招集")) {
            s1 = "招集なし";
            s2 = "";
        } else {
            s1 = "２号非常招集";
            if (mMainStation.equals("消防局")||mMainStation.equals("教育訓練センター")) {
                s2 = mMainStation + "へ参集(所属担当者に確認すること)";
            } else {
                s2 = mMainStation + "消防署へ参集";
            }
        }
        //テキストセット
        text1.setText(s1);
        text2.setText(s2);
        //ボタン クリックリスナー設定
        layout.findViewById(R.id.btnGaitousyo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gaitousyo_visible) {
                    text3.setText(s3);
                    gaitousyo_visible = true;
                } else {
                    text3.setText("");
                    gaitousyo_visible = false;
                }
            }
        });
        builder.setTitle(title);
        builder.setView(layout);
        builder.setPositiveButton("戻る", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (i) {
                    case 1:
                        showTyphoon31();
                        break;
                    case 2:
                        showTyphoon32();
                        break;
                    case 3:
                        showTyphoon33();
                        break;
                    case 4:
                        showTyphoon34();
                        break;
                    case 5:
                        showTyphoon35();
                        break;
                    case 6:
                        showTyphoon36();
                        break;
                    case 7:
                        showTyphoon37();
                        break;
                    case 8:
                        showTyphoon38();
                        break;
                    case 9:
                        showTyphoon39();
                        break;
                    case 10:
                        showTyphoon3A();
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //大和川（柏原）
    private void showTyphoon32() {
        final CharSequence[] actions = {"■氾濫注意水位(水位3.2m)、水防警報(出動)", "■避難準備・高齢者等避難開始発令の見込み(1時間以内に水位4.7mに到達)", "■避難準備・高齢者等避難開始(水位4.7m)、避難勧告発令の見込み", "■避難勧告(水位5.3m)", "■避難指示(緊急)(水位6.8m)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("水位の状況は？");
        builder.setItems(actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:{
                        String[] a = {"住之江", "住吉", "東住吉", "平野", "消防局"};
                        String gaitousyo = "住之江,住吉,東住吉,平野,消防局";
                        showTyphoonRiver1("■大和川（柏原） 氾濫注意水位(水位3.2m)、水防警報(出動)", a, gaitousyo, 2);
                        break;}
                    case 1:{
                        String[] a = {"住之江", "住吉", "東住吉", "平野", "消防局"};
                        String gaitousyo = "４号：住之江,住吉,東住吉,平野,消防局";
                        showTyphoonRiver2("■大和川（柏原） 避難準備・高齢者等避難開始発令の見込み(1時間以内に水位4.7mに到達)", a, gaitousyo, 2);
                        break;}
                    case 2:{
                        String[] a = {"住之江", "住吉", "東住吉", "平野", "消防局"};
                        String[] b = {"天王寺", "浪速", "東成", "生野", "城東", "阿倍野", "西成"};
                        String gaitousyo = "流域署３号：住之江,住吉,東住吉,平野,消防局\n流域周辺署４号：天王寺,浪速,東成,生野,城東,阿倍野,西成";
                        showTyphoonRiver3("■大和川（柏原） 避難準備・高齢者等避難開始(水位4.7m)", a, b, gaitousyo, 2);
                        break;}
                    case 3:{
                        String[] a = {"住之江", "住吉", "東住吉", "平野", "消防局"};
                        String[] b = {"天王寺", "浪速", "東成", "生野", "城東", "阿倍野", "西成"};
                        String[] c = {"北", "都島", "福島", "此花", "中央", "西", "港", "大正", "西淀川", "淀川", "東淀川", "旭", "鶴見", "水上"};
                        String gaitousyo = "流域署２号：住之江,住吉,東住吉,平野,消防局\n流域周辺署３号：天王寺,浪速,東成,生野,城東,阿倍野,西成\nその他の署４号：北,都島,福島,此花,中央,西,港,大正,西淀川,淀川,東淀川,旭,鶴見,水上";
                        showTyphoonRiver4("■大和川（柏原） 避難勧告(水位5.3m)", a, b, c, gaitousyo, 2);
                        break;}
                    case 4:{
                        String gaitousyo = "２号：全署、消防局";
                        showTyphoonRiver5("■大和川（柏原） 避難指示(緊急)(水位6.8m)", gaitousyo, 2);
                        break;}
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //神崎川（三国）
    private void showTyphoon33() {
        final CharSequence[] actions = {"■氾濫注意水位(水位3.8m)、水防警報(出動)", "■避難準備・高齢者等避難開始発令の見込み(1時間以内に水位4.8mに到達)", "■避難準備・高齢者等避難開始(水位4.8m)、避難勧告発令の見込み", "■避難勧告(水位5m)", "■避難指示(緊急)(水位5.8m)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("水位の状況は？");
        builder.setItems(actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:{
                        String[] a = {"淀川", "東淀川", "消防局"};
                        String gaitousyo = "淀川,東淀川,消防局";
                        showTyphoonRiver1("■神崎川（三国） 氾濫注意水位(水位3.8m)、水防警報(出動)", a, gaitousyo, 3);
                        break;}
                    case 1:{
                        String[] a = {"西淀川", "淀川", "東淀川", "消防局"};
                        String gaitousyo = "４号：西淀川,淀川,東淀川,消防局";
                        showTyphoonRiver2("■神崎川（三国） 避難準備・高齢者等避難開始発令の見込み(1時間以内に水位4.8mに到達)", a, gaitousyo, 3);
                        break;}
                    case 2:{
                        String[] a = {"西淀川", "淀川", "東淀川", "消防局"};
                        String gaitousyo = "西淀川,淀川,東淀川,消防局";
                        showTyphoonRiver3b("■神崎川（三国） 避難準備・高齢者等避難開始(水位4.8m)", a, gaitousyo, 3);
                        break;}
                    case 3:{
                        String[] a = {"西淀川", "淀川", "東淀川", "消防局"};
                        String[] b = {"北", "都島", "福島", "此花", "中央", "西", "港", "大正", "天王寺", "浪速", "東成", "生野", "旭", "城東", "鶴見", "阿倍野", "住之江", "住吉", "東住吉", "平野", "西成", "水上"};
                        String gaitousyo = "流域署２号：西淀川,淀川,東淀川,消防局\nその他の署４号：北,都島,福島,此花,中央,西,港,大正,天王寺,浪速,東成,生野,旭,城東,鶴見,阿倍野,住之江,住吉,東住吉,平野,西成,水上";
                        showTyphoonRiver4b("■神崎川（三国） 避難勧告(水位5m)", a, b, gaitousyo, 3);
                        break;}
                    case 4:{
                        String gaitousyo = "２号：全署、消防局";
                        showTyphoonRiver5("■神崎川（三国） 避難指示(緊急)(水位5.8m)", gaitousyo, 3);
                        break;}
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //安威川（千歳橋）
    private void showTyphoon34() {
        final CharSequence[] actions = {"■氾濫注意水位(水位3.25m)、水防警報(出動)", "■避難準備・高齢者等避難開始発令の見込み(1時間以内に水位3.5mに到達)", "■避難準備・高齢者等避難開始(水位3.5m)、避難勧告発令の見込み", "■避難勧告(水位4.25m)", "■避難指示(緊急)(水位5.1m)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("水位の状況は？");
        builder.setItems(actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:{
                        String[] a = {"東淀川", "消防局"};
                        String gaitousyo = "東淀川,消防局";
                        showTyphoonRiver1("■安威川（千歳橋） 氾濫注意水位(水位3.25m)、水防警報(出動)", a, gaitousyo, 4);
                        break;}
                    case 1:{
                        String[] a = {"東淀川", "消防局"};
                        String gaitousyo = "４号：東淀川,消防局";
                        showTyphoonRiver2("■安威川（千歳橋） 避難準備・高齢者等避難開始発令の見込み(1時間以内に水位3.5mに到達)", a, gaitousyo, 4);
                        break;}
                    case 2:{
                        String[] a = {"東淀川", "消防局"};
                        String[] b = {"西淀川", "淀川"};
                        String gaitousyo = "流域署３号：東淀川,消防局\n流域周辺署４号：西淀川,淀川";
                        showTyphoonRiver3("■安威川（千歳橋） 避難準備・高齢者等避難開始(水位3.5m)", a, b, gaitousyo, 4);
                        break;}
                    case 3:{
                        String[] a = {"東淀川", "消防局"};
                        String[] b = {"西淀川", "淀川"};
                        String[] c = {"北", "都島", "福島", "此花", "中央", "西", "港", "大正", "天王寺", "浪速", "東成", "生野", "旭", "城東", "鶴見", "阿倍野", "住之江", "住吉", "東住吉", "平野", "西成", "水上"};
                        String gaitousyo = "流域署２号：東淀川,消防局\n流域周辺署３号：西淀川,淀川\nその他の署４号：北, 都島,福島,此花,中央,西,港,大正,天王寺,浪速,東成,生野,旭,城東,鶴見,阿倍野,住之江,住吉,東住吉,平野,西成,水上";
                        showTyphoonRiver4("■安威川（千歳橋） 避難勧告(水位4.25m)", a, b, c, gaitousyo, 4);
                        break;}
                    case 4:{
                        String gaitousyo = "２号：全署、消防局";
                        showTyphoonRiver5("■安威川（千歳橋） 避難指示(緊急)(水位5.1m)", gaitousyo, 4);
                        break;}
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //寝屋川（京橋）
    private void showTyphoon35() {
        final CharSequence[] actions = {"■氾濫注意水位(水位3m)、水防警報(出動)", "■避難準備・高齢者等避難開始発令の見込み(1時間以内に水位3.1mに到達)", "■避難準備・高齢者等避難開始(水位3.1m)、避難勧告発令の見込み", "■避難勧告(水位3.3m)", "■避難指示(緊急)(水位3.5m)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("水位の状況は？");
        builder.setItems(actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:{
                        String[] a = {"都島", "中央", "城東", "鶴見", "消防局"};
                        String gaitousyo = "都島,中央,城東,鶴見,消防局";
                        showTyphoonRiver1("■寝屋川（京橋） 氾濫注意水位(水位3m)、水防警報(出動)", a, gaitousyo, 5);
                        break;}
                    case 1:{
                        String[] a = {"都島", "東成", "生野", "旭", "城東", "鶴見", "東住吉", "平野", "消防局"};
                        String gaitousyo = "４号：都島,東成,生野,旭,城東,鶴見,東住吉,平野,消防局";
                        showTyphoonRiver2("■寝屋川（京橋） 避難準備・高齢者等避難開始発令の見込み(1時間以内に水位3.1mに到達)", a, gaitousyo, 5);
                        break;}
                    case 2:{
                        String[] a = {"都島", "東成", "生野", "旭", "城東", "鶴見", "東住吉", "平野", "消防局"};
                        String[] b = {"中央", "天王寺", "阿倍野", "住吉"};
                        String gaitousyo = "流域署３号：都島,東成,生野,旭,城東,鶴見,東住吉,平野,消防局\n流域周辺署４号：中央,天王寺,阿倍野,住吉";
                        showTyphoonRiver3("■寝屋川（京橋） 避難準備・高齢者等避難開始(水位3.1m)", a, b, gaitousyo, 5);
                        break;}
                    case 3:{
                        String[] a = {"都島", "東成", "生野", "旭", "城東", "鶴見", "東住吉", "平野", "消防局"};
                        String[] b = {"中央", "天王寺", "阿倍野", "住吉"};
                        String[] c = {"北", "福島", "此花", "西", "港", "大正", "浪速", "西淀川", "淀川", "東淀川", "住之江", "西成", "水上"};
                        String gaitousyo = "流域署２号：都島,東成,生野,旭,城東,鶴見,東住吉,平野,消防局\n流域周辺署３号：中央,天王寺,阿倍野,住吉\nその他の署４号：北,福島,此花,西,港,大正,浪速,西淀川,淀川,東淀川,住之江,西成,水上";
                        showTyphoonRiver4("■寝屋川（京橋） 避難勧告(水位3.3m)", a, b, c, gaitousyo, 5);
                        break;}
                    case 4:{
                        String gaitousyo = "２号：全署、消防局";
                        showTyphoonRiver5("■寝屋川（京橋） 避難指示(緊急)(水位3.5m)", gaitousyo, 5);
                        break;}
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //第二寝屋川（昭明橋）
    private void showTyphoon36() {
        final CharSequence[] actions = {"■氾濫注意水位(水位3.4m)、水防警報(出動)", "■避難準備・高齢者等避難開始発令の見込み(1時間以内に水位4.25mに到達)", "■避難準備・高齢者等避難開始(水位4.25m)、避難勧告発令の見込み", "■避難勧告(水位4.55m)", "■避難指示(緊急)(水位4.85m)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("水位の状況は？");
        builder.setItems(actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:{
                        String[] a = {"中央", "城東", "鶴見", "消防局"};
                        String gaitousyo = "中央,城東,鶴見,消防局";
                        showTyphoonRiver1("■第二寝屋川（昭明橋） 氾濫注意水位(水位3.4m)、水防警報(出動)", a, gaitousyo, 6);
                        break;}
                    case 1:{
                        String[] a = {"東成", "城東", "鶴見", "消防局"};
                        String gaitousyo = "４号：東成,城東,鶴見,消防局";
                        showTyphoonRiver2("■第二寝屋川（昭明橋） 避難準備・高齢者等避難開始発令の見込み(1時間以内に水位4.25mに到達)", a, gaitousyo, 6);
                        break;}
                    case 2:{
                        String[] a = {"東成", "城東", "鶴見", "消防局"};
                        String[] b = {"都島", "中央", "天王寺", "生野", "旭", "阿倍野", "住吉", "東住吉", "平野"};
                        String gaitousyo = "流域署３号：東成,城東,鶴見,消防局\n流域周辺署４号：都島,中央,天王寺,生野,旭,阿倍野,住吉,東住吉,平野";
                        showTyphoonRiver3("■第二寝屋川（昭明橋） 避難準備・高齢者等避難開始(水位4.25m)", a, b, gaitousyo, 6);
                        break;}
                    case 3:{
                        String[] a = {"東成", "城東", "鶴見", "消防局"};
                        String[] b = {"都島", "中央", "天王寺", "生野", "旭", "阿倍野", "住吉", "東住吉", "平野"};
                        String[] c = {"北", "福島", "此花", "西", "港", "大正", "浪速", "西淀川", "淀川", "東淀川", "住之江", "西成", "水上"};
                        String gaitousyo = "流域署２号：東成,城東,鶴見,消防局\n流域周辺署３号：都島,中央,天王寺,生野,旭,阿倍野,住吉,東住吉,平野\nその他の署４号：北,福島,此花,西,港,大正,浪速,西淀川,淀川,東淀川,住之江,西成,水上";
                        showTyphoonRiver4("■第二寝屋川（昭明橋） 避難勧告(水位4.55m)", a, b, c, gaitousyo, 6);
                        break;}
                    case 4:{
                        String gaitousyo = "２号：全署、消防局";
                        showTyphoonRiver5("■第二寝屋川（昭明橋） 避難指示(緊急)(水位4.85m)", gaitousyo, 6);
                        break;}
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //平野川（剣橋）
    private void showTyphoon37() {
        final CharSequence[] actions = {"■氾濫注意水位(水位3.3m)、水防警報(出動)", "■避難準備・高齢者等避難開始発令の見込み(1時間以内に水位3.9mに到達)", "■避難準備・高齢者等避難開始(水位3.9m)、避難勧告発令の見込み", "■避難勧告(水位4.15m)", "■避難指示(緊急)(水位4.4m)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("水位の状況は？");
        builder.setItems(actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:{
                        String[] a = {"東成", "生野", "城東", "東住吉", "平野", "消防局"};
                        String gaitousyo = "東成,生野,城東,東住吉,平野,消防局";
                        showTyphoonRiver1("■平野川（剣橋） 氾濫注意水位(水位3.3m)、水防警報(出動)", a, gaitousyo, 7);
                        break;}
                    case 1:{
                        String[] a = {"東成", "生野", "城東", "東住吉", "平野", "消防局"};
                        String gaitousyo = "４号：東成,生野,城東,東住吉,平野,消防局";
                        showTyphoonRiver2("■平野川（剣橋） 避難準備・高齢者等避難開始発令の見込み(1時間以内に水位3.9mに到達)", a, gaitousyo, 7);
                        break;}
                    case 2:{
                        String[] a = {"東成", "生野", "城東", "東住吉", "平野", "消防局"};
                        String[] b = {"都島", "中央", "天王寺", "旭", "鶴見", "阿倍野", "住吉"};
                        String gaitousyo = "流域署３号：東成,生野,城東,東住吉,平野,消防局\n流域周辺署４号：都島,中央,天王寺,旭,鶴見,阿倍野,住吉";
                        showTyphoonRiver3("■平野川（剣橋） 避難準備・高齢者等避難開始(水位3.9m)", a, b, gaitousyo, 7);
                        break;}
                    case 3:{
                        String[] a = {"東成", "生野", "城東", "東住吉", "平野", "消防局"};
                        String[] b = {"都島", "中央", "天王寺", "旭", "鶴見", "阿倍野", "住吉"};
                        String[] c = {"北", "福島", "此花", "西", "港", "大正", "浪速", "西淀川", "淀川", "東淀川", "住之江", "西成", "水上"};
                        String gaitousyo = "流域署２号：東成,生野,城東,東住吉,平野,消防局\n流域周辺署３号：都島,中央,天王寺,旭,鶴見,阿倍野,住吉\nその他の署４号：北,福島,此花,西,港,大正,浪速,西淀川,淀川,東淀川,住之江,西成,水上";
                        showTyphoonRiver4("■平野川（剣橋） 避難勧告(水位4.15m)", a, b, c, gaitousyo, 7);
                        break;}
                    case 4:{
                        String gaitousyo = "２号：全署、消防局";
                        showTyphoonRiver5("■平野川（剣橋） 避難指示(緊急)(水位4.4m)", gaitousyo, 7);
                        break;}
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //平野川分水路（今里大橋）
    private void showTyphoon38() {
        final CharSequence[] actions = {"■氾濫注意水位(水位3.3m)、水防警報(出動)", "■避難準備・高齢者等避難開始発令の見込み(1時間以内に水位3.4mに到達)", "■避難準備・高齢者等避難開始(水位3.4m)、避難勧告発令の見込み", "■避難勧告(水位3.85m)", "■避難指示(緊急)(水位4.63m)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("水位の状況は？");
        builder.setItems(actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:{
                        String[] a = {"東成", "生野", "城東", "消防局"};
                        String gaitousyo = "東成,生野,城東,消防局";
                        showTyphoonRiver1("■平野川分水路（今里大橋） 氾濫注意水位(水位3.3m)、水防警報(出動)", a, gaitousyo, 8);
                        break;}
                    case 1:{
                        String[] a = {"東成", "生野", "城東", "東住吉", "平野", "消防局"};
                        String gaitousyo = "４号：東成,生野,城東,東住吉,平野,消防局";
                        showTyphoonRiver2("■平野川分水路（今里大橋） 避難準備・高齢者等避難開始発令の見込み(1時間以内に水位3.4mに到達)", a, gaitousyo, 8);
                        break;}
                    case 2:{
                        String[] a = {"東成", "生野", "城東", "東住吉", "平野", "消防局"};
                        String[] b = {"都島", "中央", "天王寺", "旭", "鶴見", "阿倍野", "住吉"};
                        String gaitousyo = "流域署３号：東成,生野,城東,東住吉,平野,消防局\n流域周辺署４号：都島,中央,天王寺,旭,鶴見,阿倍野,住吉";
                        showTyphoonRiver3("■平野川分水路（今里大橋） 避難準備・高齢者等避難開始(水位3.4m)", a, b, gaitousyo, 8);
                        break;}
                    case 3:{
                        String[] a = {"東成", "生野", "城東", "東住吉", "平野", "消防局"};
                        String[] b = {"都島", "中央", "天王寺", "旭", "鶴見", "阿倍野", "住吉"};
                        String[] c = {"北", "福島", "此花", "西", "港", "大正", "浪速", "西淀川", "淀川", "東淀川", "住之江", "西成", "水上"};
                        String gaitousyo = "流域署２号：東成,生野,城東,東住吉,平野,消防局\n流域周辺署３号：都島,中央,天王寺,旭,鶴見,阿倍野,住吉\nその他の署４号：北,福島,此花,西,港,大正,浪速,西淀川,淀川,東淀川,住之江,西成,水上";
                        showTyphoonRiver4("■平野川分水路（今里大橋） 避難勧告(水位3.85m)", a, b, c, gaitousyo, 8);
                        break;}
                    case 4:{
                        String gaitousyo = "２号：全署、消防局";
                        showTyphoonRiver5("■平野川分水路（今里大橋） 避難指示(緊急)(水位4.63m)", gaitousyo, 8);
                        break;}
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //古川（桑才）
    private void showTyphoon39() {
        final CharSequence[] actions = {"■氾濫注意水位(水位3.2m)、水防警報(出動)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("水位の状況は？");
        builder.setItems(actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        String[] a = {"鶴見", "消防局"};
                        String gaitousyo = "鶴見,消防局";
                        showTyphoonRiver1("■古川（桑才） 氾濫注意水位(水位3.2m)、水防警報(出動)", a, gaitousyo, 9);
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //東除川（大堀上小橋）
    private void showTyphoon3A() {
        final CharSequence[] actions = {"■氾濫注意水位(水位2.9m)、水防警報(出動)", "■避難準備・高齢者等避難開始発令の見込み(1時間以内に水位3.2mに到達)", "■避難準備・高齢者等避難開始(水位3.2m)、避難勧告発令の見込み", "■避難勧告(水位3.9m)", "■避難指示(緊急)(水位5.3m)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("水位の状況は？");
        builder.setItems(actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:{
                        String[] a = {"平野", "消防局"};
                        String gaitousyo = "平野,消防局";
                        showTyphoonRiver1("■東除川（大堀上小橋） 氾濫注意水位(水位2.9m)、水防警報(出動)", a, gaitousyo, 10);
                        break;}
                    case 1:{
                        String[] a = {"平野", "消防局"};
                        String gaitousyo = "４号：平野,消防局";
                        showTyphoonRiver2("■東除川（大堀上小橋） 避難準備・高齢者等避難開始発令の見込み(1時間以内に水位3.2mに到達)", a, gaitousyo, 10);
                        break;}
                    case 2:{
                        String[] a = {"平野", "消防局"};
                        String gaitousyo = "平野,消防局";
                        showTyphoonRiver3b("■東除川（大堀上小橋） 避難準備・高齢者等避難開始(水位3.2m)", a, gaitousyo, 10);
                        break;}
                    case 3:{
                        String[] a = {"平野", "消防局"};
                        String[] b = {"北", "都島", "福島", "此花", "中央", "西", "港", "大正", "天王寺", "浪速", "西淀川", "淀川", "東淀川", "東成", "生野", "旭", "城東", "鶴見", "阿倍野", "住之江", "住吉", "東住吉", "西成", "水上"};
                        String gaitousyo = "流域署２号：平野,消防局\nその他の署４号：北,都島,福島,此花,中央,西,港,大正,天王寺,浪速,西淀川,淀川,東淀川,東成,生野,旭,城東,鶴見,阿倍野,住之江,住吉,東住吉,西成,水上";
                        showTyphoonRiver4b("■東除川（大堀上小橋） 避難勧告(水位3.9m)", a, b, gaitousyo, 10);
                        break;}
                    case 4:{
                        String gaitousyo = "２号：全署、消防局";
                        showTyphoonRiver5("■東除川（大堀上小橋） 避難指示(緊急)(水位5.3m)", gaitousyo, 10);
                        break;}
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //高潮区域
    private void showTyphoon3B(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■高潮区域");
        builder.setMessage("第５非常警備(北、都島、福島、此花、中央、西、港、大正、浪速、西淀川、淀川、住之江、西成、水上、消防局)\n\n招集なし");
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //情報（気象）
    private void showWeather() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("URLをタップしてください");
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.info_weather, (ViewGroup) findViewById(R.id.infoWeather));
        builder.setView(layout);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //情報（河川）
    private void showRiver() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("URLをタップしてください");
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.info_river, (ViewGroup) findViewById(R.id.infoRiver));
        builder.setView(layout);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //情報（道路）
    private void showRoad() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("URLをタップしてください");
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.info_road, (ViewGroup) findViewById(R.id.infoRoad));
        builder.setView(layout);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //連絡網データ表示
    private void showCheck(){
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
    public void showCaution() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("留意事項");
        //テキストファイル読み込み
        InputStream is = null;
        BufferedReader br = null;
        String text = "";
        try {
            try {
                //assetsフォルダ内のテキスト読み込み
                is = getAssets().open("typhoon_caution.txt");
                br = new BufferedReader(new InputStreamReader(is));
                //１行づつ読み込み、改行追加
                String str;
                while ((str = br.readLine()) != null) {
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
    private void showBousaiNet() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("URLまたはボタンをタップしてください");
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.info_osaka, (ViewGroup) findViewById(R.id.infoOsaka));
        //ボタン クリックリスナー設定
        layout.findViewById(R.id.btnOsakaBousaiApp).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
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
