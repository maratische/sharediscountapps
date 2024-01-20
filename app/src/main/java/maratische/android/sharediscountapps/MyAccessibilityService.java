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
import android.os.Handler;
import android.os.Looper;
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
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

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

            handlerFindElementByText(handler, "android.widget.TextView", null, "₽", 7, 1,
                    (t) -> {
                        clickIsClicable(t);
                        handlerFindElementByText(handler, "android.widget.TextView", null, "штрих", 7, 1,
                                (t2) -> {
                                    takeScreenshot(new MyTakeScreenshotCallback("pyaterka.jpg", "pyaterka", false));
                                    return null;
                                },
                                (t2) -> {//бывает не срабатывает открытие, пробуем еще раз
                                    handlerFindElementByText(handler, "android.widget.TextView", null, "₽", 7, 1,
                                            (t3) -> {
                                                handlerFindElementByText(handler, "android.widget.TextView", null, "штрих", 7, 1,
                                                        (t4) -> {
                                                            takeScreenshot(new MyTakeScreenshotCallback("pyaterka.jpg", "pyaterka", false));
                                                            return null;
                                                        }, null);
                                                return null;
                                            },
                                            (t3) -> {
                                                return null;
                                            });
                                    return null;
                                });
                        return null;
                    }, (t) -> {
                        //ничего не делаем если не нашли кнопку с рублем
                        return null;
                    });

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

            handlerFindElementByText(handler, "android.widget.TextView", null, "бонус", 5, 1, (t) -> {
                clickIsClicable(t);
                handlerFindElementByText(handler, "android.widget.TextView", null, "Обновить", 5, 1, (t2) -> {
                    takeScreenshot(new MyTakeScreenshotCallback("spar.jpg", "spar", true));
                    return null;
                }, (t2) -> {
                    takeScreenshot(new MyTakeScreenshotCallback("spar.jpg", "spar", true));
                    return null;
                });
                return null;
            }, (t) -> {
                performGlobalAction(GLOBAL_ACTION_BACK);
                handlerFindElementByText(handler, "android.widget.TextView", null, "бонус", 5, 1, (t3) -> {
                    clickIsClicable(t3);
                    handlerFindElementByText(handler, "android.widget.TextView", null, "Обновить", 5, 1, (t4) -> {
                        takeScreenshot(new MyTakeScreenshotCallback("spar.jpg", "spar", true));
                        return null;
                    }, (t4) -> {
                        takeScreenshot(new MyTakeScreenshotCallback("spar.jpg", "spar", true));
                        return null;
                    });
                    return null;
                }, null);
                return null;
            });
//            AccessibilityNodeInfo today = waitAndFindElementByText("android.widget.TextView", null, "бонус", 5, 1);
//            if (today == null /*&& (
//                    findElementByText("android.widget.TextView", weatherRootNode, null, "Только сегодня") != null
//                    || findElementByText("android.widget.TextView", weatherRootNode, null, "Только для") != null)*/) {
//                performGlobalAction(GLOBAL_ACTION_BACK);
//            }
//            today = waitAndFindElementByText("android.widget.TextView", null, "бонус", 5, 1);
//            clickIsClicable(today);
//            today = waitAndFindElementByText("android.widget.TextView", null, "Обновить", 5, 1);
//
//            takeScreenshot(new MyTakeScreenshotCallback("spar.jpg", "spar", true));

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


            handlerFindElementByText(handler, "android.widget.TextView", "Подробнее", null, 7, 1,
                    (t) -> {
                        clickIsClicable(t);
                        handler.postDelayed(() -> {
                            takeScreenshot(new MyTakeScreenshotCallback("verniy.jpg", "verniy", true));
                        }, 3000);
                        return null;
                    }, null);
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

            handlerFindElementByText(handler, "android.widget.TextView", null, "Показать", 7, 1,
                    (t) -> {
                        handler.postDelayed(() -> {
                            takeScreenshot(new MyTakeScreenshotCallback("magnit.jpg", "magnit", true));
                        }, 3000);
                        return null;
                    }, null);
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

            handlerFindElementById(handler, "android.widget.ImageView", "ru.myauchan.droid:id/code_image_view", 7, 1,
                    (t) -> {
                        clickIsClicable(t);
                        handlerFindElementByText(handler, "android.widget.TextView", null, "Ваша карта", 7, 1,
                                (t2) -> {
                                    takeScreenshot(new MyTakeScreenshotCallback("auchan.jpg", "auchan", false));
                                    return null;
                                }, null);
                        return null;
                    }, (t) -> {
                        //не нашли кнопку, вдруг что на экране
                        performGlobalAction(GLOBAL_ACTION_BACK);
                        handler.postDelayed(() -> {
                            handlerFindElementById(handler, "android.widget.ImageView", "ru.myauchan.droid:id/code_image_view", 7, 1,
                                    (t3) -> {
                                        clickIsClicable(t3);
                                        handlerFindElementByText(handler, "android.widget.TextView", null, "Ваша карта", 7, 1,
                                                (t4) -> {
                                                    takeScreenshot(new MyTakeScreenshotCallback("auchan.jpg", "auchan", false));
                                                    return null;
                                                }, null);
                                        return null;
                                    }, null);
                        }, 3000);
                        return null;
                    });
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

            handlerFindElementByText(handler, "android.widget.TextView", "Today", null, 3, 1, (t) -> {
                clickIsClicable(t);
                handler.postDelayed(() -> {
                    takeScreenshot(new MyTakeScreenshotCallback("weather.jpg", "weather", false));
                }, 1000 * 3);
                return null;
            }, null);
        } catch (Exception e) {
            System.out.println(" " + e.getMessage());
            sendError(e.getMessage());
        }
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

    Handler handler = new Handler(Looper.getMainLooper());

    private void handlerFindElementByText(Handler handler, String elementType, String equalText, String containText, int maxCount, int delayInSeconds,
                                          Function<AccessibilityNodeInfo, Void> supplier,
                                          Function<AccessibilityNodeInfo, Void> supplierNull) {
        int finalMaxCount = maxCount - 1;
        handler.postDelayed(() -> {
            AccessibilityNodeInfo carta = findElementByText(elementType, getRootInActiveWindow(), equalText, containText);
            if (carta == null) {
                if (finalMaxCount > 0) {
                    handlerFindElementByText(handler, elementType, equalText, containText, finalMaxCount, delayInSeconds, supplier, supplierNull);
                } else {
                    if (supplierNull != null) supplierNull.apply(carta);
                }
            } else {
                if (supplier != null) supplier.apply(carta);
            }
        }, 1000 * delayInSeconds);
    }

    private void handlerFindElementById(Handler handler, String elementType, String id, int maxCount, int delayInSeconds,
                                        Function<AccessibilityNodeInfo, Void> supplier,
                                        Function<AccessibilityNodeInfo, Void> supplierNull) {
        int finalMaxCount = maxCount - 1;
        handler.postDelayed(() -> {
            AccessibilityNodeInfo carta = findElementById(elementType, getRootInActiveWindow(), id);
            if (carta == null) {
                if (finalMaxCount > 0) {
                    handlerFindElementById(handler, elementType, id, finalMaxCount, delayInSeconds, supplier, supplierNull);
                } else {
                    if (supplierNull != null) supplierNull.apply(carta);
                }
            } else {
                if (supplier != null) supplier.apply(carta);
            }
        }, 1000 * delayInSeconds);
    }

    private AccessibilityNodeInfo waitAndFindElementByText(String elementType, String equalText, String containText, int maxCount, int delayInSeconds) throws InterruptedException {
        Thread.sleep(1000);
        int counter = 0;
        AccessibilityNodeInfo carta = null;
        while (carta == null && counter++ < maxCount) {
            Thread.sleep(1000 * delayInSeconds);
            carta = findElementByText(elementType, getRootInActiveWindow(), equalText, containText);
        }
        return carta;
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

    private AccessibilityNodeInfo waitAndfindElementById(String elementType, String id, int maxCount, int delayInSeconds) throws InterruptedException {
        Thread.sleep(1000);
        int counter = 0;
        AccessibilityNodeInfo today = null;
        while (today == null && counter++ < maxCount) {
            Thread.sleep(1000 * delayInSeconds);
            today = findElementById(elementType, getRootInActiveWindow(), id);
        }
        return today;
    }

    private AccessibilityNodeInfo findElementById(String elementType, AccessibilityNodeInfo nodeInfo, String id) {
        var elements = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("ru.myauchan.droid:id/code_image_view");
        if (elements != null) {
            for (int i = 0; i < elements.size(); i++) {
                var child = elements.get(i);
                if (child.getClassName().toString().equals(elementType)) {
                    return child;
                }
            }
        }
        return null;
//        if (nodeInfo == null) {
//            return null;
//        }
//        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
//            AccessibilityNodeInfo child = nodeInfo.getChild(i);
//            if (child != null && child.getClassName() != null) {
//                if (child.getClassName().toString().equals(elementType)) {
//                    if (child.getViewIdResourceName() != null || child.getPaneTitle() != null) {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                            System.out.println(child.getViewIdResourceName() + " " + child.getUniqueId()+ " " + child.getWindowId() + " " + child.getPaneTitle());
//                        }
//                    }
//                    if (child.getViewIdResourceName() != null && child.getViewIdResourceName().equals(id)) {
//                        return child;
//                    }
//                }
//            }
//
//            AccessibilityNodeInfo button = findElementById(elementType, child, id);
//            if (button != null) {
//                return button;
//            }
//        }
//
//        return null;
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
                        startService(new Intent(getApplicationContext(), TelegramService.class).putExtra("key", timeSettingsName));
                    }
                } else {
                    saveTempBitmap(filename, bitmap);//,getApplicationContext(),"WhatsappIntegration");
                    bitmap.isMutable();
                    settings.setTimeLastSucessfull(System.currentTimeMillis());
                    startService(new Intent(getApplicationContext(), TelegramService.class).putExtra("key", timeSettingsName));
                }
            } catch (Exception e1) {
                sendError("error on decodeQRCode ad save" + e1.getMessage());
            }

            settings.setTimeLast(System.currentTimeMillis());
            SettingsUtil.Companion.saveSettings(timeSettingsName, settings, getApplicationContext());
            handler.postDelayed(() -> {
                performGlobalAction(GLOBAL_ACTION_BACK);
                handler.postDelayed(() -> {
                    performGlobalAction(GLOBAL_ACTION_BACK);
                    handler.postDelayed(() -> {
                        performGlobalAction(GLOBAL_ACTION_HOME);
                        handler.postDelayed(() -> {
                            Context context = getApplicationContext();
                            Intent intent = context.getPackageManager().getLaunchIntentForPackage("maratische.android.sharediscountapps");
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }, 1000);
                    }, 1000);
                }, 1000);
            }, 3000);
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

    public void takeScreenshot(TakeScreenshotCallback callback) {
        super.takeScreenshot(Display.DEFAULT_DISPLAY,
                getApplicationContext().getMainExecutor(), callback);
    }
}