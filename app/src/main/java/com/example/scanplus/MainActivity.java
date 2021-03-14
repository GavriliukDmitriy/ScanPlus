package com.example.scanplus;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import android.view.View;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.TextView;
import java.io.File;


public class MainActivity extends AppCompatActivity{

    Button setBtn;
    TextView textViewScanInfo;
    TextView quantityTextView;
    EditText editQuantity;
    CheckBox checkBoxSetResult;
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    Cursor userCursor;
    String barcodeScanned;
    String itemBarItemIDValue;
    File file;
    final byte SCAN_MODE = 1;
    final byte INVENTORY_MODE = 2;
    byte mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setFile();
        if(!file.exists()) {
            fileNotFoundDialogShow();
        } else {
            openDB();
            setMode();
            scanCode();
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        setFile();
        if(!file.exists()) {
            fileNotFoundDialogShow();
        } else {
            setMode();
        }
    }

    private void setFile(){
        //принудительно создаем директорию приложения
        new File(getExternalFilesDir(null).getPath()).mkdir();
        //проверяем наличие файла бд
        file = new File(getExternalFilesDir(null).getPath() + "/" + "dcd2.db3");
    }

    private void fileNotFoundDialogShow(){
        AlertDialog aboutDialog = new AlertDialog.Builder(
                this).setMessage("Помести файл dcd2.db3 в директорию " + getExternalFilesDir(null).getPath())
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                    }
                }).create();

        aboutDialog.show();
    }

    private void openDB(){
        databaseHelper = new DatabaseHelper(getApplicationContext());
        // открываем подключение
        db = databaseHelper.open();
    }

    private void setMode(){
        //получаем данные из бд в виде курсора
        Cursor checkedCursor;
        checkedCursor = db.rawQuery("SELECT * " +
                                        "FROM " +
                                        "stocks"
                                        ,null);
        if(checkedCursor.getCount()>0){
            mode = INVENTORY_MODE;
        } else{
            mode = SCAN_MODE;
        }
    }

    private void initViews(){
        setBtn = (Button) findViewById(R.id.setBtn);
        textViewScanInfo = (TextView) findViewById(R.id.textViewScanInfo);
        textViewScanInfo.setMovementMethod(new ScrollingMovementMethod());
        quantityTextView = (TextView) findViewById(R.id.quantityTextView);
        editQuantity = (EditText) findViewById(R.id.editQuantity);
        checkBoxSetResult = (CheckBox) findViewById(R.id.checkBoxSetResult);
        if(mode == INVENTORY_MODE) {
            editQuantity = (EditText) findViewById(R.id.editQuantity);
            editQuantity.setText("1");
            quantityTextView.setText("Кол-во");
            checkBoxSetResult.setChecked(false);
            editQuantity.setVisibility(View.VISIBLE);
            checkBoxSetResult.setVisibility(View.VISIBLE);
            quantityTextView.setVisibility(View.VISIBLE);
        } else if (mode == SCAN_MODE) {
            setBtn.setText("СКАН");
            editQuantity.setVisibility(View.GONE);
            checkBoxSetResult.setVisibility(View.GONE);
            quantityTextView.setVisibility(View.GONE);
        }
    }

    private void scanCode(){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Наведи красную линию на код."+"\n"+"Если код не распознается," +"\n"+ "нажми назад для ручного ввода.");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result!=null&&result.getContents()!=null){
            barcodeScanned = result.getContents();
            initViews();
            if(mode == INVENTORY_MODE) {
                setResultAndNewScanListener();
            }  else if(mode == SCAN_MODE) {
                setNewScanListener();
            }
            showRes();
            // создаем базу данных (закомментировал код, создающий файл БД из assets, т.к. нужно выдавать пользователю ошибку, если он не положил файл в директорию)
            // databaseHelper.create_db();
        } else{
            //Получаем вид с файла keyboard_input_dialog.xml, который применим для диалогового окна:
            LayoutInflater li = LayoutInflater.from(MainActivity.this);
            View keyboardInputDialogView = li.inflate(R.layout.keyboard_input_dialog, null);
            //Создаем AlertDialog
            AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            //Настраиваем keyboard_input_dialog.xml для нашего AlertDialog:
            mDialogBuilder.setView(keyboardInputDialogView);
            //Настраиваем отображение поля для ввода текста в открытом диалоге:
            final EditText userInput = (EditText) keyboardInputDialogView.findViewById(R.id.input_text);
            //Настраиваем сообщение в диалоговом окне:
            mDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("К сканированию",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    scanCode();
                                }
                            })
                    .setNegativeButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    //Запускаем поиск по введенному вручную штрихкоду
                                    barcodeScanned = userInput.getText().toString();
                                    initViews();
                                    if(mode == INVENTORY_MODE) {
                                        setResultAndNewScanListener();
                                    }  else if(mode == SCAN_MODE) {
                                        setNewScanListener();
                                    }
                                    showRes();
                                }
                            });
            //Создаем AlertDialog:
            AlertDialog alertDialog = mDialogBuilder.create();
            //и отображаем его:
            alertDialog.show();
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showRes() {
        if(mode == INVENTORY_MODE) {
            //получаем данные из бд в виде курсора
            userCursor = db.rawQuery(
                            "SELECT barcodes.id, barcodes.item_id, items.name, stocks.quantity" +
                            " FROM" +
                            " barcodes" +
                            " LEFT JOIN" +
                            " stocks" +
                            " ON " +
                            " stocks.barcode_id = (SELECT barcodes.id" +
                                                 " FROM" +
                                                 " barcodes" +
                                                 " WHERE" +
                                                 " barcodes.item_id = (SELECT" +
                                                                      " barcodes.item_id" +
                                                                      " FROM" +
                                                                      " barcodes" +
                                                                      " WHERE" +
                                                                      " barcodes.id = '" + barcodeScanned + "'" +
                                                                      ")" +
                                                 ")" +
                            " INNER JOIN " +
                            "items" +
                            " ON " +
                            "SUBSTR(items.id,1,6) = SUBSTR(barcodes.item_id,1,6)" +
                            " WHERE " +
                            "barcodes.id = '" + barcodeScanned + "'"
                    , null);
            if (userCursor.getCount() > 0) {
                userCursor.moveToFirst();
                int indexBarItemID = userCursor.getColumnIndexOrThrow("item_id");
                int indexBarID = userCursor.getColumnIndexOrThrow("id");
                int indexItemsName = userCursor.getColumnIndexOrThrow("name");
                int indexStocksQuantity = userCursor.getColumnIndexOrThrow("quantity");
                itemBarItemIDValue = userCursor.getString(indexBarItemID).trim();
                String itemBarIDValue = userCursor.getString(indexBarID);
                String itemItemsNameValue = userCursor.getString(indexItemsName);
                String itemStocksQuantityValue = userCursor.getString(indexStocksQuantity);
                String res = "По ш/к   " + itemBarIDValue + "  найден товар с кодом  " + itemBarItemIDValue + "  с наименованием " + itemItemsNameValue + " в количестве  " + itemStocksQuantityValue + " шт.";
                textViewScanInfo.setText(res);
            } else {
                showAlertWithTwoButton();
            }
        } else if(mode == SCAN_MODE) {
            //получаем данные из бд в виде курсора
            userCursor = db.rawQuery("SELECT barcodes.id, barcodes.item_id, barcodes.price, items.name  " +
                            "FROM " +
                            "barcodes" +
                            " INNER JOIN " +
                            "items" +
                            " ON " +
                            "SUBSTR(items.id,1,6) = SUBSTR(barcodes.item_id,1,6)" +
                            " WHERE " +
                            "barcodes.id = '" + barcodeScanned + "'"
                    , null);
            if (userCursor.getCount() > 0) {
                userCursor.moveToFirst();
                int indexBarItemID = userCursor.getColumnIndexOrThrow("item_id");
                int indexBarID = userCursor.getColumnIndexOrThrow("id");
                int indexBarPrice = userCursor.getColumnIndexOrThrow("price");
                int indexItemsName = userCursor.getColumnIndexOrThrow("name");
                itemBarItemIDValue = userCursor.getString(indexBarItemID).trim();
                String itemBarIDValue = userCursor.getString(indexBarID);
                String itemBarPriceValue = userCursor.getString(indexBarPrice);
                String itemItemsNameValue = userCursor.getString(indexItemsName);
                String res = "По ш/к   " + itemBarIDValue + "  найден товар с кодом  " + itemBarItemIDValue + "  с наименованием " + itemItemsNameValue + ".\nЦена:  " + itemBarPriceValue + " руб.";
                textViewScanInfo.setText(res);
            } else {
                showAlertWithTwoButton();
            }
        }
    }

    private void showAlertWithTwoButton(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        // Указываем Title
        alertDialog.setTitle("Штрихкод " + barcodeScanned + " не найден");
        // Указываем текст сообщение
        alertDialog.setMessage("Поищем товар на сайте?");
        // Обработчик на нажатие ДА
        alertDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                // Код который выполнится после закрытия окна
                //открыть страницу браузера
                String url = "https://www.officemag.ru/search/?q="+barcodeScanned;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                onBackPressed();
            }
        });
        // Обработчик на нажатие НЕТ
        alertDialog.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Код который выполнится после закрытия окна
                scanCode();
            }
        });
        // показываем Alert
        alertDialog.show();
    }

    private void setResultAndNewScanListener(){
        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db = databaseHelper.open();
                int editNumber;
                String quan;
                try {
                    editNumber = Integer.parseInt(editQuantity.getText().toString());
                } catch (NumberFormatException e) {
                    editNumber = 0;
                }
                if(checkBoxSetResult.isChecked()){
                    quan = editNumber + "";
                } else{
                    quan = "quantity + " + editNumber;
                }
                db.execSQL("UPDATE stocks SET quantity = " + quan +
                          " WHERE barcode_id =  (SELECT stocks.barcode_id" +
                                                " FROM" +
                                                " barcodes" +
                                                " INNER JOIN" +
                                                " stocks" +
                                                " ON " +
                                                " barcodes.id = stocks.barcode_id" +
                                                " WHERE" +
                                                " SUBSTR(barcodes.item_id,1,6) = " + "'" + itemBarItemIDValue.trim() + "'" +
                                                " )");
                scanCode();
            }
        });
    }

    private void setNewScanListener(){
        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCode();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Закрываем подключение и курсор
        db.close();
        userCursor.close();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }


}