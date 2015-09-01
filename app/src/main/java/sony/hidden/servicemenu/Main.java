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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import java.util.List;

public class Main extends Activity {
    String sonyCode = "7378423";
    String androidCode = "4636";
    String action = "android.provider.Telephony.SECRET_CODE";
    Uri uri;
    Intent intent;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PackageManager pm = getPackageManager();

        uri = Uri.parse("android_secret_code://" + sonyCode);
        intent = new Intent(action, uri);
        List<ResolveInfo> msg = pm.queryBroadcastReceivers(intent, 0);

        if (!msg.isEmpty()) {
            openAlert();
        } else {
            chooseIntent(1);
            finish();
        }
    }

    private void openAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
        builder.setTitle(R.string.title);

        builder.setItems(R.array.options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                chooseIntent(i);
                switch (i) {
                    case 0: Toast.makeText(getApplicationContext(),
                            "Sony Service Menu", Toast.LENGTH_SHORT).show();
                        break;
                    case 1: Toast.makeText(getApplicationContext(),
                            "Android Test Menu", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

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

    public void chooseIntent(int choice) {

        if (choice == 0) {
            uri = Uri.parse("android_secret_code://" + sonyCode);
        } else {
            uri = Uri.parse("android_secret_code://" + androidCode);
        }
        intent = new Intent(action, uri);
        sendBroadcast(intent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        openAlert();
    }
}
