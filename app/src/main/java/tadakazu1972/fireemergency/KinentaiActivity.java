package tadakazu1972.fireemergency;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.CheckBox;

import com.opencsv.CSVParser;
import com.opencsv.CSVReader;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

//import android.database.sqlite.SQLiteDatabase;

/**
 * Created by tadakazu on 2016/07/12.
 */
public class KinentaiActivity extends AppCompatActivity {
    protected KinentaiActivity mActivity = null;
    protected View mView = null;
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
    //複数都道府県選択
    protected ArrayList<Integer> mSelectedPrefectureIndexList; //選択した都道府県のインデックス(showCSVで使う)格納用
    protected ArrayList<String> mSelectedPrefectureScaleList; //選択した都道府県の最大深度文字列格納用
    protected ArrayList<String> mSelectedPrefectureCSVList; //選択した都道府県の震度のcsvファイル名格納用
    //Boolean isAlertDialogExist = false; //複数ド道府県選択AlertDialogのキャンセルボタンが選択した数を押下しないともとにもどらないのを防ぐ用

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mActivity = this;
        mView = this.getWindow().getDecorView();
        setContentView(R.layout.activity_kinentai);
        initButtons();
        //連絡網データ作成
        mListView = new ListView(this);
        mDBHelper = new DBHelper(this);
        SQLiteDatabase.loadLibs(this);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String mKey = sp.getString("key", null);
        db = mDBHelper.getWritableDatabase(mKey);
        mailArray = new ArrayList<String>();
        mSelectedPrefectureIndexList = new ArrayList<Integer>();
        mSelectedPrefectureScaleList = new ArrayList<String>();
        mSelectedPrefectureCSVList = new ArrayList<String>();
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
        mView.findViewById(R.id.btnPersonal).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(mActivity, PersonalActivity.class);
                startActivity(intent);
            }
        });
        mView.findViewById(R.id.btnEarthquake).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(mActivity, EarthquakeActivity.class);
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
        mView.findViewById(R.id.btnKinentai1).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                selectSingleMultipleLand();
            }
        });
        mView.findViewById(R.id.btnKinentai2).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                selectSingleMultipleKaiiki();
            }
        });
        mView.findViewById(R.id.btnKinentai3).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                showKinentai3();
            }
        });
        mView.findViewById(R.id.btnKinentaiOtsunami).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                selectSingleMultipleOtsunami();
            }
        });
        mView.findViewById(R.id.btnKinentai4).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                showKinentai42();
            }
        });
        mView.findViewById(R.id.btnKinentaiKinen).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                showKinen();
            }
        });
        mView.findViewById(R.id.btnKinentaiEarthquake).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                showEarthquake();
            }
        });
        mView.findViewById(R.id.btnKinentaiBlackout).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                showBlackout();
            }
        });
        mView.findViewById(R.id.btnKinentaiRoad).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                showRoad();
            }
        });
        mView.findViewById(R.id.btnKinentaiTel).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                showCheck();
            }
        });
        mView.findViewById(R.id.btnKinentaiRiver).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                showRiver();
            }
        });
        mView.findViewById(R.id.btnKinentaiWeather).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                showWeather();
            }
        });
    }

    //2021.9追加
    //地震（震央「陸」）ボタンを押下したら、都道府県の選択を単一か複数かを選択させる
    private void selectSingleMultipleLand(){
        final CharSequence[] actions = {"■単一都道府県で発生","■複数の都道府県で発生"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("最大震度６弱(政令市等は震度５強)以上の地震が発生した都道府県は？");
        builder.setItems(actions, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                switch(which){
                    case 0:
                        showKinentai1();
                        break;
                    case 1:
                        //初回は念のため複数都道府県選択のインデックスと配列をクリア
                        mSelectedPrefectureIndexList.clear();
                        mSelectedPrefectureScaleList.clear();
                        mSelectedPrefectureCSVList.clear();
                        selectMultiplePrefecture();
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //2021.9追加
    //複数都道府県の選択と最大深度の選択を終わるまでさせる
    private void selectMultiplePrefecture(){
        // res/values/arrays.xmlにprefectureとして47都道府県を設定している。それを読み込む。
        int resourceId = getResources().getIdentifier("prefecture", "array", getPackageName());
        //取得した配列リソースIDを文字列配列に格納
        final String[] mList = getResources().getStringArray(resourceId);
        GridView gridView = new GridView(this);
        gridView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mList));
        gridView.setNumColumns(3);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>parent, View view, int position, long id){
                //選択した都道府県のインデックスをリストに格納
                Toast.makeText(getApplicationContext(), mList[position], Toast.LENGTH_SHORT).show();
                mSelectedPrefectureIndexList.add(position);
                selectScale(mList[position]);
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■都道府県選択");
        builder.setView(gridView);
        builder.setPositiveButton("選択終了", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                //結果表示へ
                showSelectedPrefectureResult();
            }
        });
        builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
            }
        });
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //複数都道府県の選択の都道府県ごとの最大震度を選択させるループ
    private void selectScale(String _prefecture){
        final CharSequence[] actions = {"■震度７(特別区６強)","■震度６強(特別区６弱)","■震度６弱(特別区５強、政令市５強)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(_prefecture + "の最大震度は？");
        builder.setItems(actions, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                switch(which){
                    case 0:
                        Toast.makeText(getApplicationContext(), "震度７", Toast.LENGTH_SHORT).show();
                        mSelectedPrefectureScaleList.add("震度７");
                        mSelectedPrefectureCSVList.add("riku7_multi.csv");
                        //複数都道府県選択へ再帰
                        selectMultiplePrefecture();
                        break;
                    case 1:
                        Toast.makeText(getApplicationContext(), "震度６強", Toast.LENGTH_SHORT).show();
                        mSelectedPrefectureScaleList.add("震度６強");
                        mSelectedPrefectureCSVList.add("riku6strong_multi.csv");
                        //複数都道府県選択へ再帰
                        selectMultiplePrefecture();
                        break;
                    case 2:
                        Toast.makeText(getApplicationContext(), "震度６弱", Toast.LENGTH_SHORT).show();
                        mSelectedPrefectureScaleList.add("震度６弱");
                        mSelectedPrefectureCSVList.add("riku6weak_multi.csv");
                        //複数都道府県選択へ再帰
                        selectMultiplePrefecture();
                        break;
                }
            }
        });
        //NegativeButtonをつくっていはいけない。後でCSV読みに行くときにファイル名設定が必ず必要なので、キャンセルすると止まる
        builder.setCancelable(false);
        builder.create();
        builder.show();
    }

    //複数選択した都道府県の結果表示
    private void showSelectedPrefectureResult(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("【複数選択した都道府県】");
        //テキストファイル読み込み
        String text = "";
        for(int i = 0; i < mSelectedPrefectureIndexList.size(); i++){
            //CSVファイル読み込みに行く
            text = text + readCSV(mSelectedPrefectureIndexList.get(i), mSelectedPrefectureScaleList.get(i), mSelectedPrefectureCSVList.get(i), i);
            //text = text + "index:" + mSelectedPrefectureIndexList.get(i) + " file:" + mSelectedPrefectureCSVList.get(i) + "\n";
        }
        builder.setMessage(text);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //選択した都道府県と震度に対応したCSVの結果を読み込んで返すルーチン
    private String readCSV(int _index, String _scale, String _filename, int _i){
        //csvファイル読み込み
        InputStream is = null;
        String pref = ""; //都道府県
        String scale = _scale; //最大深度
        String data1 = ""; //指揮支援隊
        String data2 = ""; //大阪府大隊(陸上)
        String data3 = ""; //大阪府隊(航空小隊)
        String result = "";
        try {
            try {
                //assetsフォルダ内のcsvファイル読み込み
                is = getAssets().open(_filename);
                InputStreamReader ir = new InputStreamReader(is,"UTF-8");
                CSVReader csvreader = new CSVReader(ir, CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, 1); //ヘッダー0行読み込まないため1行から
                List<String[]> csv = csvreader.readAll();
                String line = Arrays.toString(csv.get(_index));
                String[] data = line.split(Pattern.quote(","),0);
                //データ代入　先頭と最後に[]がついてくるのでreplaceで削除している
                pref = data[0]; pref = pref.replace("[","");
                data1 = data[1]; data1 = data1.replaceAll("、","\n     "); //２行になる答えなので改行とスペースを挿入
                data2 = data[2]; data2 = data2.replaceAll("、","\n     "); //２行になる答えなので改行とスペースを挿入
                data3 = data[3]; data3 = data3.replace("]","").replaceAll("、","\n     "); //２行になる答えなので改行とスペースを挿入;
                result = (_i + 1) +". "+pref + "：" + scale + "\n" + "・指揮支援部隊\n　"+data1+"\n・大阪府大隊(陸上)\n　"+data2+"\n・大阪府大隊(航空)\n　"+data3+"\n====================\n";
            } finally {
                if (is != null) is.close();
            }
        } catch (Exception e) {
            //エラーメッセージ
            Toast.makeText(this, "テキスト読込エラー", Toast.LENGTH_LONG).show();
        }
        //結果を返す
        return result;
    }

    //震央「陸」
    private void showKinentai1(){
        final CharSequence[] actions = {"■震度７(特別区６強)","■震度６強(特別区６弱)","■震度６弱(特別区５強、政令市５強)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("最大震度は？");
        builder.setItems(actions, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                switch(which){
                    case 0:
                        showRiku7();
                        break;
                    case 1:
                        showRiku6strong();
                        break;
                    case 2:
                        showRiku6weak();
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //震度７
    private void showRiku7(){
        /*List<String> mList = Arrays.asList("北海道", "青森県", "岩手県", "宮城県", "秋田県", "山形県", "福島県", "茨城県", "栃木県", "群馬県", "埼玉県", "千葉県", "東京都", "神奈川県", "新潟県", "富山県", "石川県", "福井県", "山梨県", "長野県", "岐阜県", "静岡県", "愛知県", "三重県", "滋賀県", "京都府", "大阪府", "兵庫県", "奈良県", "和歌山県", "鳥取県", "島根県", "岡山県", "広島県", "山口県", "徳島県", "香川県", "愛媛県", "高知県", "福岡県", "佐賀県", "長崎県", "熊本県", "大分県", "宮崎県", "鹿児島県", "沖縄県");*/
        // res/values/arrays.xmlにprefectureとして47都道府県を設定している。それを読み込む。
        int resourceId = getResources().getIdentifier("prefecture", "array", getPackageName());
        //取得した配列リソースIDを文字列配列に格納
        String[] mList = getResources().getStringArray(resourceId);
        GridView gridView = new GridView(this);
        gridView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mList));
        gridView.setNumColumns(3);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>parent, View view, int position, long id){
                showCSV(position, "■最大震度７(特別区６強)", "riku7.csv");
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■最大震度７(特別区６強)\n   震央管轄都道府県は？");
        builder.setView(gridView);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //震度６強
    private void showRiku6strong(){
        // res/values/arrays.xmlにprefectureとして47都道府県を設定している。それを読み込む。
        int resourceId = getResources().getIdentifier("prefecture", "array", getPackageName());
        //取得した配列リソースIDを文字列配列に格納
        String[] mList = getResources().getStringArray(resourceId);
        GridView gridView = new GridView(this);
        gridView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mList));
        gridView.setNumColumns(3);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>parent, View view, int position, long id){
                showCSV(position, "■最大震度６強(特別区６弱)","riku6strong.csv");
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■最大震度６強(特別区６弱)\n   震央管轄都道府県は？");
        builder.setView(gridView);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //震度６弱
    private void showRiku6weak(){
        // res/values/arrays.xmlにprefectureとして47都道府県を設定している。それを読み込む。
        int resourceId = getResources().getIdentifier("prefecture", "array", getPackageName());
        //取得した配列リソースIDを文字列配列に格納
        String[] mList = getResources().getStringArray(resourceId);
        GridView gridView = new GridView(this);
        gridView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mList));
        gridView.setNumColumns(3);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>parent, View view, int position, long id){
                showCSV(position, "■最大震度６弱(特別区５強、政令市５強)", "riku6weak.csv");
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■最大震度６弱(特別区５強、政令市５強)   震央管轄都道府県は？");
        builder.setView(gridView);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //2021.9追加
    //地震（震央「海域」）ボタンを押下したら、都道府県の選択を単一か複数かを選択させる
    private void selectSingleMultipleKaiiki(){
        final CharSequence[] actions = {"■単一都道府県で発生","■複数の都道府県で発生"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("最大震度６弱(政令市等は震度５強)以上の地震が発生した都道府県は？");
        builder.setItems(actions, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                switch(which){
                    case 0:
                        showKinentai2();
                        break;
                    case 1:
                        //初回は念のため複数都道府県選択のインデックスと配列をクリア
                        mSelectedPrefectureIndexList.clear();
                        mSelectedPrefectureScaleList.clear();
                        mSelectedPrefectureCSVList.clear();
                        selectMultiplePrefectureKaiiki();
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //2021.9追加
    //複数都道府県の選択と最大深度の選択を終わるまでさせる
    private void selectMultiplePrefectureKaiiki(){
        // res/values/arrays.xmlにprefectureとして47都道府県を設定している。それを読み込む。
        int resourceId = getResources().getIdentifier("prefecture", "array", getPackageName());
        //取得した配列リソースIDを文字列配列に格納
        final String[] mList = getResources().getStringArray(resourceId);
        GridView gridView = new GridView(this);
        gridView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mList));
        gridView.setNumColumns(3);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>parent, View view, int position, long id){
                //選択した都道府県のインデックスをリストに格納
                Toast.makeText(getApplicationContext(), mList[position], Toast.LENGTH_SHORT).show();
                mSelectedPrefectureIndexList.add(position);
                selectScaleKaiiki(mList[position]);
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■都道府県選択");
        builder.setView(gridView);
        builder.setPositiveButton("選択終了", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                //結果表示へ
                showSelectedPrefectureResultKaiiki();
            }
        });
        builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
            }
        });
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //複数都道府県の選択の都道府県ごとの最大震度を選択させるループ
    private void selectScaleKaiiki(String _prefecture){
        final CharSequence[] actions = {"■震度７(特別区６強)","■震度６強(特別区６弱)","■震度６弱(特別区５強、政令市５強)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(_prefecture + "の最大震度は？");
        builder.setItems(actions, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                switch(which){
                    case 0:
                        Toast.makeText(getApplicationContext(), "震度７", Toast.LENGTH_SHORT).show();
                        mSelectedPrefectureScaleList.add("震度７");
                        mSelectedPrefectureCSVList.add("kaiiki7_multi.csv");
                        //複数都道府県選択へ再帰
                        selectMultiplePrefectureKaiiki();
                        break;
                    case 1:
                        Toast.makeText(getApplicationContext(), "震度６強", Toast.LENGTH_SHORT).show();
                        mSelectedPrefectureScaleList.add("震度６強");
                        mSelectedPrefectureCSVList.add("kaiiki6strong_multi.csv");
                        //複数都道府県選択へ再帰
                        selectMultiplePrefectureKaiiki();
                        break;
                    case 2:
                        Toast.makeText(getApplicationContext(), "震度６弱", Toast.LENGTH_SHORT).show();
                        mSelectedPrefectureScaleList.add("震度６弱");
                        mSelectedPrefectureCSVList.add("kaiiki6weak_multi.csv");
                        //複数都道府県選択へ再帰
                        selectMultiplePrefectureKaiiki();
                        break;
                }
            }
        });
        //NegativeButtonをつくってはいけない。ファイル名が設定されないままCSV読みに行くと落ちる
        builder.setCancelable(false);
        builder.create();
        builder.show();
    }

    //複数選択した都道府県の結果表示
    private void showSelectedPrefectureResultKaiiki(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("【複数選択した都道府県】");
        //テキストファイル読み込み
        String text = "";
        for(int i = 0; i < mSelectedPrefectureIndexList.size(); i++){
            //CSVファイル読み込みに行く
            text = text + readCSV(mSelectedPrefectureIndexList.get(i), mSelectedPrefectureScaleList.get(i), mSelectedPrefectureCSVList.get(i), i);
            //text = text + "index:" + mSelectedPrefectureIndexList.get(i) + " file:" + mSelectedPrefectureCSVList.get(i) + "\n";
        }
        builder.setMessage(text);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //震央「海域」
    private void showKinentai2(){
        final CharSequence[] actions = {"■震度７(特別区６強)","■震度６強(特別区６弱)","■震度６弱(特別区５強、政令市５強)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("最大震度は？");
        builder.setItems(actions, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                switch(which){
                    case 0:
                        showKaiiki7();
                        break;
                    case 1:
                        showKaiiki6strong();
                        break;
                    case 2:
                        showKaiiki6weak();
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //震度７
    private void showKaiiki7(){
        // res/values/arrays.xmlにprefectureとして47都道府県を設定している。それを読み込む。
        int resourceId = getResources().getIdentifier("prefecture", "array", getPackageName());
        //取得した配列リソースIDを文字列配列に格納
        String[] mList = getResources().getStringArray(resourceId);
        GridView gridView = new GridView(this);
        gridView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mList));
        gridView.setNumColumns(3);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>parent, View view, int position, long id){
                showCSV(position, "■最大震度７(特別区６強)","kaiiki7.csv");
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■最大震度７(特別区６強)\n   最大震度都道府県は？");
        builder.setView(gridView);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //震度６強
    private void showKaiiki6strong(){
        // res/values/arrays.xmlにprefectureとして47都道府県を設定している。それを読み込む。
        int resourceId = getResources().getIdentifier("prefecture", "array", getPackageName());
        //取得した配列リソースIDを文字列配列に格納
        String[] mList = getResources().getStringArray(resourceId);
        GridView gridView = new GridView(this);
        gridView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mList));
        gridView.setNumColumns(3);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>parent, View view, int position, long id){
                showCSV(position,"■最大震度６強(特別区６弱)","kaiiki6strong.csv");
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■最大震度６強(特別区６弱)\n   最大震度都道府県は？");
        builder.setView(gridView);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //震度６弱
    private void showKaiiki6weak(){
        // res/values/arrays.xmlにprefectureとして47都道府県を設定している。それを読み込む。
        int resourceId = getResources().getIdentifier("prefecture", "array", getPackageName());
        //取得した配列リソースIDを文字列配列に格納
        String[] mList = getResources().getStringArray(resourceId);
        GridView gridView = new GridView(this);
        gridView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mList));
        gridView.setNumColumns(3);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>parent, View view, int position, long id){
                showCSV(position, "■最大震度６弱(特別区５強、政令市５強)","kaiiki6weak.csv");
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■最大震度６弱(特別区５強、政令市５強)   最大震度都道府県は？");
        builder.setView(gridView);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //アクションプラン
    private void showKinentai3(){
        final CharSequence[] actions = {"東海地震","首都直下地震","南海トラフ"}; //"東南海・南海地震"削除　2018/08/28
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("アクションプラン");
        builder.setItems(actions, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                switch(which){
                    case 0:
                        showToukai1(); //showActionPlan((String)actions[which],"kinentai_toukai.txt");
                        break;
                    case 1:
                        showShutochokka1(); //showActionPlan((String)actions[which],"kinentai_syutochokka.txt");
                        break;
                    /* case 2:
                        showActionPlan((String)actions[which],"kinentai_tounankai.txt");
                        break; */
                    case 2: //3:
                        showNankaitraf1(); //showNankaitraf(); 2018/08/29 showNankaitrafを経ずに直接実行
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //アクションプラン表示
    public void showActionPlan(String title, String filename){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        //テキストファイル読み込み
        InputStream is = null;
        BufferedReader br = null;
        String text = "";
        try {
            try {
                //assetsフォルダ内のテキスト読み込み
                is = getAssets().open(filename);
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

    // 2018-08-30 作成　東海地震
    int countToukai1Checked = 0;
    private void showToukai1(){

        //カスタムビュー設定
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("東海地震アクションプラン");
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.plan_toukai, (ViewGroup)findViewById(R.id.plan_toukai));
        final Spinner toukai1 = (Spinner)layout.findViewById(R.id.spnToukai1);
        builder.setView(layout);

        //checkbox
        countToukai1Checked = 0;
        layout.findViewById(R.id.checkBox1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox chk = (CheckBox) v;
                if(chk.isChecked() == true) {
                    countToukai1Checked += 1;
                }
                else {
                    countToukai1Checked -= 1;
                }
            }
        });
        layout.findViewById(R.id.checkBox2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox chk = (CheckBox) v;
                if(chk.isChecked() == true) {
                    countToukai1Checked += 1;
                }
                else {
                    countToukai1Checked -= 1;
                }
            }
        });
        layout.findViewById(R.id.checkBox3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox chk = (CheckBox) v;
                if(chk.isChecked() == true) {
                    countToukai1Checked += 1;
                }
                else {
                    countToukai1Checked -= 1;
                }
            }
        });
        layout.findViewById(R.id.checkBox4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox chk = (CheckBox) v;
                if(chk.isChecked() == true) {
                    countToukai1Checked += 1;
                }
                else {
                    countToukai1Checked -= 1;
                }
            }
        });
        layout.findViewById(R.id.checkBox5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox chk = (CheckBox) v;
                if(chk.isChecked() == true) {
                    countToukai1Checked += 1;
                }
                else {
                    countToukai1Checked -= 1;
                }
            }
        });
        layout.findViewById(R.id.checkBox6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox chk = (CheckBox) v;
                if(chk.isChecked() == true) {
                    countToukai1Checked += 1;
                }
                else {
                    countToukai1Checked -= 1;
                }
            }
        });
        layout.findViewById(R.id.checkBox7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox chk = (CheckBox) v;
                if(chk.isChecked() == true) {
                    countToukai1Checked += 1;
                }
                else {
                    countToukai1Checked -= 1;
                }
            }
        });
        layout.findViewById(R.id.checkBox8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox chk = (CheckBox) v;
                if(chk.isChecked() == true) {
                    countToukai1Checked += 1;
                }
                else {
                    countToukai1Checked -= 1;
                }
            }
        });

        // button
        builder.setPositiveButton("判定", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                String check1 = (String)toukai1.getSelectedItem();
                //いざ、判定
                if (!check1.equals("その他") && countToukai1Checked >= 2){
                    showToukai2();
                } else {
                    showActionPlan("東海地震アクションプラン","kinentai_toukai2.txt");
                }
            }
        });
        builder.setNegativeButton("キャンセル",null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    // 2018-08-30 作成　東海適用　部隊選択 -> 2019-02-05 修正
    private void showToukai2(){
        //カスタムビュー設定
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("東海地震アクションプラン適用");
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.plan_toukai2, (ViewGroup)findViewById(R.id.plan_toukai));
        //ボタン設定
        layout.findViewById(R.id.btnToukai1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDestination("kinentai_toukai110.txt");
            }
        });
        layout.findViewById(R.id.btnToukai2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDestination("kinentai_toukai111.txt");
            }
        });
        layout.findViewById(R.id.btnToukai3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDestination("kinentai_toukai112.txt");
            }
        });
        builder.setView(layout);
        builder.setNegativeButton("閉じる", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //2019-02-05 追加
    //出動先表示
    public void showDestination(String filename){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //テキストファイル読み込み
        InputStream is = null;
        BufferedReader br = null;
        String text = "";
        try {
            try {
                //assetsフォルダ内のテキスト読み込み
                is = getAssets().open(filename);
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
        builder.setNegativeButton("戻る", null);
        builder.setCancelable(false);
        builder.create();
        builder.show();
    }

    // 2018-08-30 作成　首都直下
    private void showShutochokka1(){
        final CharSequence[] actions = {"東京23区において、震度6強以上が観測された場合"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("首都直下地震アクションプラン");
        builder.setItems(actions, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                switch(which){
                    case 0:
                        showShutochokka2();
                        break;
                    /* case 1:
                        showNankaitraf2();
                        break; */
                }
            }
        });
        builder.setPositiveButton("該当", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                    showShutochokka2();
            }
        });
        builder.setNegativeButton("閉じる", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    // 2018-08-30 作成　首都直下適用　部隊選択 -> 2019-02-05 修正
    private void showShutochokka2(){
        //カスタムビュー設定
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("首都直下地震アクションプラン適用");
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.plan_shutochokka2, (ViewGroup)findViewById(R.id.plan_shutochokka));
        //ボタン設定
        layout.findViewById(R.id.btnShutochokka1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDestination("kinentai_shutochokka110.txt");
            }
        });
        layout.findViewById(R.id.btnShutochokka2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDestination("kinentai_shutochokka111.txt");
            }
        });
        layout.findViewById(R.id.btnShutochokka3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDestination("kinentai_shutochokka112.txt");
            }
        });
        builder.setView(layout);
        builder.setNegativeButton("閉じる", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //南海トラフ　ケース選択  => 2018-08-29 使用しない
    /*private void showNankaitraf(){
        final CharSequence[] actions = {"ケース１(条件判定)","ケース２(同程度被害)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("南海トラフ");
        builder.setItems(actions, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                switch(which){
                    case 0:
                        showNankaitraf1();
                        break;
                    case 1:
                        showNankaitraf2();
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }*/

    //南海トラフ　ケース１  => 2018-08-29 showNankaitrafを経ずに直接実行, R.layout を nankaitraf => plan_nankaitraf に変更
    //-> 2019-02-05 スピナーをチェックボックスに改修
    int countNankaitrafChecked = 0;
    private void showNankaitraf1(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("南海トラフ地震アクションプラン");
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.plan_nankaitraf, (ViewGroup)findViewById(R.id.plan_nankaitraf));
        //判定準備
        final Spinner nankaitraf1 = (Spinner)layout.findViewById(R.id.spnNankaitraf1);
        //checkbox
        countNankaitrafChecked = 0;
        layout.findViewById(R.id.chkNankai1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox chk = (CheckBox) v;
                if(chk.isChecked() == true) {
                    countNankaitrafChecked += 1;
                }
                else {
                    countNankaitrafChecked -= 1;
                }
            }
        });
        layout.findViewById(R.id.chkNankai2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox chk = (CheckBox) v;
                if(chk.isChecked() == true) {
                    countNankaitrafChecked += 1;
                }
                else {
                    countNankaitrafChecked -= 1;
                }
            }
        });
        layout.findViewById(R.id.chkNankai3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox chk = (CheckBox) v;
                if(chk.isChecked() == true) {
                    countNankaitrafChecked += 1;
                }
                else {
                    countNankaitrafChecked -= 1;
                }
            }
        });
        builder.setView(layout);
        builder.setPositiveButton("判定", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                String check1 = (String)nankaitraf1.getSelectedItem();
                //いざ、判定
                if (!check1.equals("その他")&& countNankaitrafChecked==3){
                    // showActionPlan("南海トラフ地震アクションプラン","kinentai_nankaitraf.txt");
                    showNankaitraf2();
                } else {
                    showActionPlan("南海トラフ地震アクションプラン","kinentai_nankaitraf2.txt");
                }
            }
        });
        builder.setNegativeButton("キャンセル",null);
        /* builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                showNankaitraf();
            }
        }); */
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    // 2018-08-29 作成　南海トラフ　部隊選択 -> 2019-02-05 修正
    private void showNankaitraf2(){
        //カスタムビュー設定
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("南海トラフ地震アクションプラン適用");
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.plan_nankaitraf2, (ViewGroup)findViewById(R.id.plan_nankaitraf));
        //ボタン設定
        layout.findViewById(R.id.btnNankaitraf1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDestination("kinentai_nankaitraf110.txt");
            }
        });
        layout.findViewById(R.id.btnNankaitraf2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDestination("kinentai_nankaitraf111.txt");
            }
        });
        layout.findViewById(R.id.btnNankaitraf3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDestination("kinentai_nankaitraf112.txt");
            }
        });
        builder.setView(layout);
        builder.setNegativeButton("閉じる", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //南海トラフ　ケース２  => 2018/08/29 使用しない
    /*private void showNankaitraf2(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("南海トラフ　ケース２");
        //テキストファイル読み込み
        InputStream is = null;
        BufferedReader br = null;
        String text = "";
        try {
            try {
                //assetsフォルダ内のテキスト読み込み
                is = getAssets().open("nankaitraf_case2.txt");
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
        builder.setPositiveButton("対応", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                showActionPlan("南海トラフ","kinentai_nankaitraf.txt");
            }
        });
        builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                showNankaitraf();
            }
        });
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }*/

    //2021.9追加
    //大津波警報ボタンを押下したら、都道府県の選択を単一か複数かを選択させる
    private void selectSingleMultipleOtsunami(){
        final CharSequence[] actions = {"■単一都道府県で発生","■複数の都道府県で発生"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("大津波警報が発表された都道府県は？");
        builder.setItems(actions, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                switch(which){
                    case 0:
                        showKinentai41();
                        break;
                    case 1:
                        //初回は念のため複数都道府県選択のインデックスと配列をクリア
                        mSelectedPrefectureIndexList.clear();
                        mSelectedPrefectureScaleList.clear();
                        mSelectedPrefectureCSVList.clear();
                        selectMultiplePrefectureOtsunami();
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //2021.9追加
    //複数都道府県の選択と最大深度の選択を終わるまでさせる
    private void selectMultiplePrefectureOtsunami(){
        // res/values/arrays.xmlにprefectureとして47都道府県を設定している。それを読み込む。
        int resourceId = getResources().getIdentifier("prefecture", "array", getPackageName());
        //取得した配列リソースIDを文字列配列に格納
        final String[] mList = getResources().getStringArray(resourceId);
        GridView gridView = new GridView(this);
        gridView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mList));
        gridView.setNumColumns(3);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>parent, View view, int position, long id){
                //選択した都道府県のインデックスをリストに格納
                Toast.makeText(getApplicationContext(), mList[position], Toast.LENGTH_SHORT).show();
                mSelectedPrefectureIndexList.add(position);
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■都道府県選択");
        builder.setView(gridView);
        builder.setPositiveButton("選択終了", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                //結果表示へ
                showSelectedPrefectureResultOtsunami();
            }
        });
        builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
            }
        });
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //選択した都道府県と震度に対応したCSVの結果を読み込んで返すルーチン
    private String readCSVOtsunami(int _index, String _filename, int _i){
        //csvファイル読み込み
        InputStream is = null;
        String pref = ""; //都道府県
        String data1 = ""; //指揮支援隊
        String data2 = ""; //大阪府大隊(陸上)
        String data3 = ""; //大阪府隊(航空小隊)
        String result = "";
        try {
            try {
                //assetsフォルダ内のcsvファイル読み込み
                is = getAssets().open(_filename);
                InputStreamReader ir = new InputStreamReader(is,"UTF-8");
                CSVReader csvreader = new CSVReader(ir, CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, 1); //ヘッダー0行読み込まないため1行から
                List<String[]> csv = csvreader.readAll();
                String line = Arrays.toString(csv.get(_index));
                String[] data = line.split(Pattern.quote(","),0);
                //データ代入　先頭と最後に[]がついてくるのでreplaceで削除している
                pref = data[0]; pref = pref.replace("[","");
                data1 = data[1]; data1 = data1.replaceAll("、","\n     "); //２行になる答えなので改行とスペースを挿入
                data2 = data[2]; data2 = data2.replaceAll("、","\n     "); //２行になる答えなので改行とスペースを挿入
                data3 = data[3]; data3 = data3.replace("]","").replaceAll("、","\n     "); //２行になる答えなので改行とスペースを挿入;
                result = (_i + 1) +". "+pref + "：大津波警報" + "\n" + "・指揮支援隊\n　"+data1+"\n・大阪府大隊(陸上)\n　"+data2+"\n・大阪府大隊(航空)\n　"+data3+"\n====================\n";
            } finally {
                if (is != null) is.close();
            }
        } catch (Exception e) {
            //エラーメッセージ
            Toast.makeText(this, "テキスト読込エラー", Toast.LENGTH_LONG).show();
        }
        //結果を返す
        return result;
    }

    //複数選択した都道府県の結果表示
    private void showSelectedPrefectureResultOtsunami(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("【複数選択した都道府県】");
        //テキストファイル読み込み
        String text = "";
        for(int i = 0; i < mSelectedPrefectureIndexList.size(); i++){
            //CSVファイル読み込みに行く
            text = text + readCSVOtsunami(mSelectedPrefectureIndexList.get(i), "otsunami_multi.csv", i);
            //text = text + "index:" + mSelectedPrefectureIndexList.get(i) + " file:" + mSelectedPrefectureCSVList.get(i) + "\n";
        }
        builder.setMessage(text);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //大津波・噴火　2021.9　削除
    /*private void showKinentai4(){
        final CharSequence[] actions = {"大津波警報","噴火"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("大津波警報・噴火");
        builder.setItems(actions, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                switch(which){
                    case 0:
                        showKinentai41();
                        break;
                    case 1:
                        showKinentai42();
                        break;
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }*/

    //大津波　都道府県選択
    private void showKinentai41(){
        // res/values/arrays.xmlにprefectureとして47都道府県を設定している。それを読み込む。
        int resourceId = getResources().getIdentifier("prefecture", "array", getPackageName());
        //取得した配列リソースIDを文字列配列に格納
        String[] mList = getResources().getStringArray(resourceId);
        GridView gridView = new GridView(this);
        gridView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mList));
        gridView.setNumColumns(3);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>parent, View view, int position, long id){
                showCSV(position, "■大津波警報", "otsunami.csv");
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■大津波警報\n   都道府県は？");
        builder.setView(gridView);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //噴火　都道府県選択
    private void showKinentai42(){
        // res/values/arrays.xmlにprefectureとして47都道府県を設定している。それを読み込む。
        int resourceId = getResources().getIdentifier("prefecture", "array", getPackageName());
        //取得した配列リソースIDを文字列配列に格納
        String[] mList = getResources().getStringArray(resourceId);
        GridView gridView = new GridView(this);
        gridView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mList));
        gridView.setNumColumns(3);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>parent, View view, int position, long id){
                showCSV(position, "■噴火", "hunka.csv");
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■噴火\n   都道府県は？");
        builder.setView(gridView);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //特殊災害(NBC含む)
    private void showKinentai5(){
        final CharSequence[] actions = {"北海道","青森県","岩手県","宮城県","秋田県","山形県","福島県","茨城県","栃木県","群馬県","埼玉県","千葉県","東京都","神奈川県","新潟県","富山県","石川県","福井県","山梨県","長野県","岐阜県","静岡県","愛知県","三重県","滋賀県","京都府","大阪府","兵庫県","奈良県","和歌山県","鳥取県","島根県","岡山県","広島県","山口県","徳島県","香川県","愛媛県","高知県","福岡県","佐賀県","長崎県","熊本県","大分県","宮崎県","鹿児島県","沖縄県"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("■特殊災害(NBC含む)\n   都道府県は？");
        builder.setItems(actions, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                showCSV(which, "■特殊災害(NBC含む)","nbc.csv");
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //CSVファイル表示ダイアログ
    public void showCSV(int which, String title, String filename){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //csvファイル読み込み
        InputStream is = null;
        String pref = ""; //都道府県
        String data1 = ""; //指揮支援隊
        String data2 = ""; //大阪府大隊(陸上)
        String data3 = ""; //大阪府隊(航空小隊)
        try {
            try {
                //assetsフォルダ内のcsvファイル読み込み
                is = getAssets().open(filename);
                InputStreamReader ir = new InputStreamReader(is,"UTF-8");
                CSVReader csvreader = new CSVReader(ir, CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, 1); //ヘッダー0行読み込まないため1行から
                List<String[]> csv = csvreader.readAll();
                String line = Arrays.toString(csv.get(which));
                String[] data = line.split(Pattern.quote(","),0);
                //データ代入　先頭と最後に[]がついてくるのでreplaceで削除している
                pref = data[0]; pref = pref.replace("[","");
                data1 = data[1]; data1 = data1.replaceAll("、","\n     "); //２行になる答えなので改行とスペースを挿入
                data2 = data[2]; data2 = data2.replaceAll("、","\n     "); //２行になる答えなので改行とスペースを挿入
                data3 = data[3]; data3 = data3.replace("]","").replaceAll("、","\n     "); //２行になる答えなので改行とスペースを挿入;
            } finally {
                if (is != null) is.close();
            }
        } catch (Exception e) {
            //エラーメッセージ
            Toast.makeText(this, "テキスト読込エラー", Toast.LENGTH_LONG).show();
        }
        builder.setTitle(title+"　"+pref);
        builder.setMessage("・指揮支援部隊\n\n　"+data1+"\n\n・大阪府大隊(陸上)\n\n　"+data2+"\n\n・大阪府大隊(航空)\n\n　"+data3);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //情報（緊援）
    private void showKinen(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("URLをタップしてください");
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.info_kinen, (ViewGroup)findViewById(R.id.infoKinen));
        builder.setView(layout);
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
        String[] from = {"name", "tel", "mail", "kubun", "syozoku0","syozoku", "kinmu"};
        int[] to = {R.id.record_name, R.id.record_tel, R.id.record_mail, R.id.record_kubun, R.id.record_syozoku0, R.id.record_syozoku, R.id.record_kinmu};
        //初回のみ起動。そうしないと、すべて選択した後の２回目がまたnewされて意味ない
        if (mAdapter2 == null) {
            mActivity.mAdapter2 = new CustomCursorAdapter(mActivity,R.layout.record_view2,c,from,to,0);
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

    //情報（河川）
    private void showRiver(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("URLをタップしてください");
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.info_river, (ViewGroup)findViewById(R.id.infoRiver));
        builder.setView(layout);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    //情報（気象）
    private void showWeather(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("URLをタップしてください");
        //カスタムビュー設定
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.info_weather, (ViewGroup)findViewById(R.id.infoWeather));
        builder.setView(layout);
        builder.setNegativeButton("キャンセル", null);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }
}
