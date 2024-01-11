package maratische.android.sharediscountapps;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.Path;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyAccessibilityService extends AccessibilityService {
    private String yandexWeatherplugin = "ru.yandex.weatherplugin";
    private String auchanDroid = "ru.myauchan.droid";
    private String myspar = "ru.myspar";
    private String verniy = "com.ru.verniy";
    private String magnit = "ru.tander.magnit";
    private String pyaterka = "ru.pyaterochka.app.browser";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Получаем корневой узел AccessibilityNodeInfo для текущего события
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();

        // Выполняем действия, например, находим и кликаем по кнопке с текстом "OK"
        performAction(rootNode);
    }

    @Override
    public void onInterrupt() {
        // Вызывается при прерывании сервиса доступности
    }

    // Метод для поиска EditText по идентификатору
    private AccessibilityNodeInfo findEditTextById(AccessibilityNodeInfo source, String editTextId) {
        if (source == null) {
            return null;
        }

        List<AccessibilityNodeInfo> editTextNodes = source.findAccessibilityNodeInfosByViewId(editTextId);

        if (editTextNodes != null && !editTextNodes.isEmpty()) {
            return editTextNodes.get(0);
        }

        return null;
    }

    private void performAction(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        var start = SettingsUtil.Companion.loadSettings("start", getApplicationContext());
        if (start.getTimeLast() + 1000 * 60 > System.currentTimeMillis()) {
            return;
        }
        start.setTimeLast(System.currentTimeMillis() + 1000 * 60);
        start.setTimeLastSucessfull(-1);
        SettingsUtil.Companion.saveSettings("start", start, getApplicationContext());
        sendBroadcast(new Intent(MainActivity4.UPDATE_UI));


        var pyaterka = SettingsUtil.Companion.loadSettings("pyaterka", getApplicationContext());
        if (pyaterka.getActive() && pyaterka.getTimeLast() + 1000 * 60 * 60 * 12 < System.currentTimeMillis()) {
            performActionPyaterka();
            return;
        }

        var spar = SettingsUtil.Companion.loadSettings("spar", getApplicationContext());
        if (spar.getActive() && spar.getTimeLast() + 1000 * 60 * 60 * 12 < System.currentTimeMillis()) {
            performActionSpar();
            return;
        }

        var verniy = SettingsUtil.Companion.loadSettings("verniy", getApplicationContext());
        if (verniy.getActive() && verniy.getTimeLast() + 1000 * 60 * 60 * 12 < System.currentTimeMillis()) {
            performActionVerniy();
            return;
        }

        var magnit = SettingsUtil.Companion.loadSettings("magnit", getApplicationContext());
        if (magnit.getActive() && magnit.getTimeLast() + 1000 * 60 * 60 * 12 < System.currentTimeMillis()) {
            performActionMagnit();
            return;
        }

        var auchan = SettingsUtil.Companion.loadSettings("auchan", getApplicationContext());
        if (auchan.getActive() && auchan.getTimeLast() + 1000 * 60 * 60 * 12 < System.currentTimeMillis()) {
            performActionAuchan();
            return;
        }

        var weather = SettingsUtil.Companion.loadSettings("weather", getApplicationContext());
        if (weather.getActive() && weather.getTimeLast() + 1000 * 60 * 60 < System.currentTimeMillis()) {
            performActionWeather();
            return;
        }
    }

    private void performActionPyaterka() {
        try {
            Context context = getApplicationContext();
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(pyaterka);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

            Thread.sleep(7000);

            AccessibilityNodeInfo weatherRootNode = getRootInActiveWindow();
            AccessibilityNodeInfo today = findElementByText("android.widget.TextView", weatherRootNode, null, "₽");
            clickIsClicable(today);

            Thread.sleep(5000);

            takeScreenshot(Display.DEFAULT_DISPLAY,
                    getApplicationContext().getMainExecutor(), new MyTakeScreenshotCallback("pyaterka.jpg", "pyaterka", false));

        } catch (Exception e) {
            System.out.println(" " + e.getMessage());
            sendError(e.getMessage());
        }
    }

    private void performActionSpar() {
        try {
            Context context = getApplicationContext();
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(myspar);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

            Thread.sleep(7000);

            AccessibilityNodeInfo weatherRootNode = getRootInActiveWindow();
            AccessibilityNodeInfo today = findElementByText("android.widget.TextView", weatherRootNode, "бонусов", null);
            clickIsClicable(today);

            takeScreenshot(Display.DEFAULT_DISPLAY,
                    getApplicationContext().getMainExecutor(), new MyTakeScreenshotCallback("spar.jpg", "spar", true));

        } catch (Exception e) {
            System.out.println(" " + e.getMessage());
            sendError(e.getMessage());
        }
    }

    private void performActionVerniy() {
        try {
            Context context = getApplicationContext();
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(verniy);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

            Thread.sleep(7000);

            AccessibilityNodeInfo weatherRootNode = getRootInActiveWindow();
            AccessibilityNodeInfo today = findElementByText("android.widget.TextView", weatherRootNode, "Подробнее", null);
            clickIsClicable(today);

            Thread.sleep(7000);

            takeScreenshot(Display.DEFAULT_DISPLAY,
                    getApplicationContext().getMainExecutor(), new MyTakeScreenshotCallback("verniy.jpg", "verniy", true));

        } catch (Exception e) {
            System.out.println(" " + e.getMessage());
            sendError(e.getMessage());
        }
    }

    private void performActionMagnit() {
        try {
            Context context = getApplicationContext();
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(magnit);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

            Thread.sleep(7000);

            AccessibilityNodeInfo weatherRootNode = getRootInActiveWindow();
            AccessibilityNodeInfo today = findElementByText("android.widget.TextView", weatherRootNode, null, "Показать");
            clickIsClicable(today);

            Thread.sleep(5000);

            takeScreenshot(Display.DEFAULT_DISPLAY,
                    getApplicationContext().getMainExecutor(), new MyTakeScreenshotCallback("magnit.jpg", "magnit", true));

        } catch (Exception e) {
            System.out.println(" " + e.getMessage());
            sendError(e.getMessage());
        }
    }

    private void performActionAuchan() {
        try {
            Context context = getApplicationContext();
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(auchanDroid);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

            Thread.sleep(7000);

            takeScreenshot(Display.DEFAULT_DISPLAY,
                    getApplicationContext().getMainExecutor(), new MyTakeScreenshotCallback("auchan.jpg", "auchan", false));

        } catch (Exception e) {
            System.out.println(" " + e.getMessage());
            sendError(e.getMessage());
        }
    }

    private void performActionWeather() {
        try {
            Context context = getApplicationContext();
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(yandexWeatherplugin);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

            Thread.sleep(3000);

        } catch (Exception e) {
            System.out.println(" " + e.getMessage());
            sendError(e.getMessage());
        }
        AccessibilityNodeInfo weatherRootNode = getRootInActiveWindow();
        AccessibilityNodeInfo today = findElementByText("android.widget.TextView", weatherRootNode, "Today", null);
        clickIsClicable(today);
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            System.out.println(" " + e.getMessage());
            sendError(e.getMessage());
        }
        takeScreenshot(Display.DEFAULT_DISPLAY,
                getApplicationContext().getMainExecutor(), new MyTakeScreenshotCallback("weather.jpg", "weather", false));
    }

    private static void clickIsClicable(AccessibilityNodeInfo today) {
        if (today != null) {
            for (int i = 0; i < 5; i++) {
                if (today.isClickable()) {
                    today.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    break;
                }
                today = today.getParent();
            }
        }
    }

    private AccessibilityNodeInfo findButtonByText(AccessibilityNodeInfo nodeInfo, String text) {
        return findElementByText("android.widget.Button", nodeInfo, text, null);
    }

    private AccessibilityNodeInfo findElementByText(String elementType, AccessibilityNodeInfo nodeInfo, String equalText, String containText) {
        if (nodeInfo == null) {
            return null;
        }

        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo child = nodeInfo.getChild(i);
            if (child != null && child.getClassName() != null) {
                if (child.getClassName().toString().equals(elementType)) {
                    if (child.getText() != null && equalText != null && child.getText().toString().equals(equalText)) {
                        return child;
                    }
                    if (child.getText() != null && containText != null && child.getText().toString().contains(containText)) {
                        return child;
                    }
                }
            }

            AccessibilityNodeInfo button = findElementByText(elementType, child, equalText, containText);
            if (button != null) {
                return button;
            }
        }

        return null;
    }

    private AccessibilityNodeInfo findElementById(String elementType, AccessibilityNodeInfo nodeInfo, String id) {
        if (nodeInfo == null) {
            return null;
        }

        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo child = nodeInfo.getChild(i);
            if (child != null && child.getClassName() != null) {
                if (child.getClassName().toString().equals(elementType)) {
                    if (child.getViewIdResourceName() != null && child.getViewIdResourceName().equals(id)) {
                        return child;
                    }
                }
            }

            AccessibilityNodeInfo button = findElementById(elementType, child, id);
            if (button != null) {
                return button;
            }
        }

        return null;
    }

    public void saveTempBitmap(String fname, Bitmap bitmap) {
        if (isExternalStorageWritable()) {
            saveImage(fname, bitmap);
        } else {
            //prompt the user or do something
        }
    }

    private void saveImage(String fname, Bitmap finalBitmap) {

        File myDir = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            sendError(e.getMessage());
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private String decodeQRCode(Bitmap bitmap) {

        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
//            Collection<BarcodeFormat> formats = new ArrayList<>();
//            formats.add(BarcodeFormat.PDF_417);
//            formats.add(BarcodeFormat.CODABAR);
//            Map<DecodeHintType, Collection<BarcodeFormat>> hints = new HashMap<>();
//            hints.put(DecodeHintType.POSSIBLE_FORMATS, formats);
//            hints.put(DecodeHintType.TRY_HARDER, formats);
//            com.google.zxing.Result result = new MultiFormatReader().decode(binaryBitmap, hints);
            com.google.zxing.Result result = new MultiFormatReader().decode(binaryBitmap);
            return result.getText();
        } catch (NotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    class MyTakeScreenshotCallback implements TakeScreenshotCallback {

        String filename;
        String timeSettingsName;
        boolean needScanQrCode;

        MyTakeScreenshotCallback(String filename, String timeSettingsName, boolean needScanQrCode) {
            this.filename = filename;
            this.timeSettingsName = timeSettingsName;
            this.needScanQrCode = needScanQrCode;
        }

        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        public void onSuccess(@NonNull ScreenshotResult screenshotResult) {

            Log.i("ScreenShotResult", "onSuccess");
            Bitmap bitmap = Bitmap.wrapHardwareBuffer(screenshotResult.getHardwareBuffer(), screenshotResult.getColorSpace());

            var settings = SettingsUtil.Companion.loadSettings(timeSettingsName, getApplicationContext());
            try {
                if (needScanQrCode) {
                    saveTempBitmap("temp.jpg", bitmap);//,getApplicationContext(),"WhatsappIntegration");
                    var myDir = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    var file = new File(myDir, "temp.jpg");
                    ImageDecoder.Source source = ImageDecoder.createSource(getApplicationContext().getContentResolver(),
                            Uri.fromFile(file));
                    Bitmap bitmap2 = ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.RGBA_F16, true);
                    String result = decodeQRCode(bitmap2);
                    if (result != null && result.length() > 0) {
                        saveTempBitmap(filename, bitmap);//,getApplicationContext(),"WhatsappIntegration");
                        bitmap.isMutable();
                        settings.setTimeLastSucessfull(System.currentTimeMillis());
                    }
                } else {
                    saveTempBitmap(filename, bitmap);//,getApplicationContext(),"WhatsappIntegration");
                    bitmap.isMutable();
                    settings.setTimeLastSucessfull(System.currentTimeMillis());
                }
            } catch (Exception e1) {
                sendError("error on decodeQRCode ad save" + e1.getMessage());
            }

            settings.setTimeLast(System.currentTimeMillis());
            SettingsUtil.Companion.saveSettings(timeSettingsName, settings, getApplicationContext());
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                System.out.println(" " + e.getMessage());
                sendError(e.getMessage());
            }
            performGlobalAction(GLOBAL_ACTION_BACK);
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                System.out.println(" " + e.getMessage());
                sendError(e.getMessage());
            }
            performGlobalAction(GLOBAL_ACTION_HOME);
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                System.out.println(" " + e.getMessage());
                sendError(e.getMessage());
            }

            Context context = getApplicationContext();
            Intent intent = context.getPackageManager().getLaunchIntentForPackage("maratische.android.sharediscountapps");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        @Override
        public void onFailure(int i) {
            Log.i("ScreenShotResult", "onFailure code is " + i);

        }
    }

    private void sendError(String message) {
        var intent = new Intent(getApplicationContext(), ErrorService.class);
        intent.putExtra("message", message);
        startService(intent);
    }

    private GestureDescription createClickGesture(float x, float y, long startTime) {
        final int DURATION = 1;

        Path clickPath = new Path();
        clickPath.moveTo(x, y);
        GestureDescription.StrokeDescription clickStroke =
                new GestureDescription.StrokeDescription(clickPath, 0, DURATION);
        GestureDescription.Builder clickBuilder = new GestureDescription.Builder();
        clickBuilder.addStroke(clickStroke);
        return clickBuilder.build();
    }
}
