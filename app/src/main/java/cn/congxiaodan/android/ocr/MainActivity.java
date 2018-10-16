package cn.congxiaodan.android.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import com.googlecode.tesseract.android.TessBaseAPI;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static String DATA_PATH = "";
    private static final String DATA_FILE_NAME = "eng.traineddata";
    //private static final String DATA_FILE_NAME = "letsgodigital.traineddata";
    //private static final String DATA_FILE_NAME = "seg.traineddata";
    private static final String DATA_DIR_NAME = "tesseract";
    private static final String DATA_SUB_DIR_NAME = "tessdata";

    private static final String WHITE_LIST =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String BLACK_LIST =
        "\"!@#$%^&*()_+=-[]}{;:'\\\"\\\\|~`,./<>?\"";

    private Bitmap mImage;
    private WebView webResult;
    private boolean isInit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // init view
        webResult = findViewById(R.id.web_result);
        // init
        init();
    }

    private void init() {
        File folder = new File(getFilesDir(), DATA_DIR_NAME);
        if (!folder.exists()) {
            boolean b = folder.mkdir();
        }
        DATA_PATH = folder.getAbsolutePath();
        Log.d(TAG, "Data sub folder path: " + DATA_PATH);
        File subFolder = new File(folder, DATA_SUB_DIR_NAME);
        if (!subFolder.exists()) {
            boolean b = subFolder.mkdir();
        }
        File file = new File(subFolder, DATA_FILE_NAME);
        if (!file.exists()) {
            try {
                FileOutputStream fileOutputStream;
                byte[] bytes = readRawTrainingData(this);
                if (bytes == null) {
                    return;
                }

                fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(bytes);
                fileOutputStream.close();
                isInit = true;
                Log.d(TAG, "Prepared training data file, file path: " + file.getAbsolutePath());
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Error opening training data file\n" + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "Error opening training data file\n" + e.getMessage());
            }
        } else {
            isInit = true;
            Log.d(TAG, "Training data path: " + file.getAbsolutePath());
        }
        //mImage = BitmapFactory.decodeResource(getResources(), R.mipmap.text_image);
        mImage = BitmapFactory.decodeResource(getResources(), R.mipmap.eng_text);
    }

    // *note: this is sync result
    public void go(View view) {
        if (!isInit) return;
        TessBaseAPI api = new TessBaseAPI();

        api.setDebug(true);
        api.init(DATA_PATH, "eng");
        api.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, WHITE_LIST); // 白名单
        api.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, BLACK_LIST); // 黑名单
        api.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_OSD); // 识别模式
        api.setImage(mImage);
        String result = api.getHOCRText(0);
        api.end();
        webResult.loadData(result, "text/html", "UTF-8");
        Log.i(TAG, "OCR result: " + result);
    }

    private static byte[] readRawTrainingData(Context context) {

        try {
            InputStream fileInputStream = context.getResources()
                .openRawResource(R.raw.seg_traineddata);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] b = new byte[1024];

            int bytesRead;

            while ((bytesRead = fileInputStream.read(b)) != -1) {
                bos.write(b, 0, bytesRead);
            }

            fileInputStream.close();

            return bos.toByteArray();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Error reading raw training data file\n" + e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e(TAG, "Error reading raw training data file\n" + e.getMessage());
        }

        return null;
    }
}
