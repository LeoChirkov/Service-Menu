/*
This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package sony.hidden.servicemenu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import java.util.List;

public class Main extends Activity {
    String sonyCode = "7378423";
    String androidCode = "4636";
    String htcCode = "3424";
    String motoCode = "2486";

    String lgPackage = "arima.com.hiddenmenu";
    String lgMainActivity = "arima.com.hiddenmenu.HiddenMenuEntryActivity";
    String action = "android.provider.Telephony.SECRET_CODE";
    String vendor;
    Intent intent;
    Uri uri;

    SharedPreferences prefs;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vendor = Build.MANUFACTURER;
        if (checkPrefs(0)) {
            Log.i("checkPrefs", "first start");
            switch (vendor.toLowerCase()) {
                case "sony":
                    checkIntent(sonyCode);
                    break;
                case "htc":
                    checkIntent(htcCode);
                    break;
                case "motorola":
                    checkIntent(motoCode);
                    break;
                case "lge":
                    checkLG();
                    break;
                default:
                    defaultHiddenMenu();
                    break;
            }
        } else if (checkPrefs(1)) {
            Log.i("checkPrefs", "hidden menu found");
            switch (vendor.toLowerCase()) {
                case "sony":
                    openAlert(vendor, sonyCode);
                    break;
                case "htc":
                    openAlert(vendor, htcCode);
                    break;
                case "motorola":
                    openAlert(vendor, motoCode);
                    break;
                case "lge":
                    openAlert("LG", null);
                    break;
            }
        } else {
            Log.i("checkPrefs", "no hidden menu found since previous start");
            defaultHiddenMenu();
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("FirstStart", false);
        editor.commit();
    }

    public void checkIntent(String secretCode) {
        Log.i("checkIntent", "started");
        PackageManager pm = getPackageManager();

        uri = Uri.parse("android_secret_code://" + secretCode);
        intent = new Intent(action, uri);
        // create a list of Broadcast receivers able to receive specific intent
        List<ResolveInfo> msg = pm.queryBroadcastReceivers(intent, 0);
        if (!msg.isEmpty()) {
            Log.i("checkIntent", "hidden menu found");
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("HiddenMenu", true);
            editor.commit();
            openAlert(vendor, secretCode);
        } else {
            Log.i("checkIntent", "hidden menu not found");
            defaultHiddenMenu();
        }
    }

    // LG hidden menu differs from others and needs another check algorithm
    private void checkLG() {
        Log.i("checkLG", "started");

        final List<ApplicationInfo> packages;
        PackageManager pm = getPackageManager();
        packages = pm.getInstalledApplications(0);
        boolean x = false;
        // check if there's LG hidden menu among installed apps
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(lgPackage)) {
                Log.i("checkLG", "hidden menu found");
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("HiddenMenu", true);
                editor.commit();
                x = true;
                break;
            }
        }
        if (x) {
            openAlert("LG", null);
        } else {
            Log.i("checkLG", "hidden menu not found");
            defaultHiddenMenu();
        }
    }

    // execute LG hidden menu by creating explicit intent
    public void startLG() {
        intent = new Intent();
        intent.setComponent(new ComponentName(lgPackage, lgMainActivity));
        startActivity(intent);
    }

    private void openAlert(final String vendor, final String secretCode) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("Vendor", vendor);
        editor.putString("SecretCode", secretCode);
        editor.commit();

        AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
        builder.setTitle(R.string.title);

        builder.setItems(R.array.options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                chooseIntent(i, secretCode);
                switch (i) {
                    case 0:
                        Toast.makeText(getApplicationContext(),
                                vendor + " Service Menu", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(getApplicationContext(),
                                "Phone Information", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        // finish the app by clicking "back" button instead of dismissing dialog box
        // with blank screen left
        final AlertDialog dialog = builder.create();
        dialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    finish();
                    dialog.dismiss();
                }
                return true;
            }
        });
        dialog.show();
    }

    // configure, create and start an intent for specific manufacturer
    public void chooseIntent(int choice, String secretCode) {
        Log.i("chooseIntent", "started");
        Boolean lg = vendor.toLowerCase().contains("lg");
        if (!lg && choice == 0) {
            Log.i("checkIntent", "not LG device");
            uri = Uri.parse("android_secret_code://" + secretCode);
            intent = new Intent(action, uri);
            sendBroadcast(intent);
        } else if (lg && choice == 0) {
            Log.i("checkIntent", "LG device detected");
            startLG();
        } else {
            defaultHiddenMenu();
        }
    }

    // default hidden menu for any android device even with custom ROM
    public void defaultHiddenMenu() {
        uri = Uri.parse("android_secret_code://" + androidCode);
        intent = new Intent(action, uri);
        sendBroadcast(intent);
        finish();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        use shared prefs
        String vendor = prefs.getString("Vendor", null);
        String secretCode = prefs.getString("SecretCode", null);

        openAlert(vendor, secretCode);
    }

    public boolean checkPrefs(int i) {
        prefs = getPreferences(MODE_PRIVATE);
        boolean hiddenMenu = prefs.getBoolean("HiddenMenu", false);
        boolean firstStart = prefs.getBoolean("FirstStart", true);
        if (i == 0) {
            return firstStart;
        } else {
            return hiddenMenu;
        }
    }
}
