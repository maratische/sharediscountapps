package maratische.android.sharediscountapps;

public class PhoneUtils {
    public static String validPhoneNumber(String phone) {
        if (phone != null) {
            for (String key : new String[]{" ", "-", "(", ")"}) {
                while (phone.indexOf(key) > -1) {
                    phone = phone.replace(key, "");
                }
            }
        }
        return phone;
    }
}
