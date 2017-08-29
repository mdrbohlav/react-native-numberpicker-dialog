package fr.bamlab.reactnativenumberpickerdialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

class RNNumberPickerDialogModule extends ReactContextBaseJavaModule {
    private Context context;

    public RNNumberPickerDialogModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
    }

    /**
     * @return the name of this module. This will be the name used to {@code require()} this module
     * from javascript.
     */
    @Override
    public String getName() {
        return "RNNumberPickerDialog";
    }

    @ReactMethod
    public void show(final ReadableMap options, final Callback onSuccess, final Callback onFailure) {
        LinearLayout llContainer = new LinearLayout(getCurrentActivity());
        LinearLayout.LayoutParams llContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        llContainer.setOrientation(LinearLayout.HORIZONTAL);
        llContainer.setLayoutParams(llContainerParams);

        ReadableArray columns = options.getArray("options");
        final ArrayList<NumberPicker> pickers = new ArrayList<>();

        for (int i = 0; i < columns.size(); i++) {
            LinearLayout llColContainer = new LinearLayout((getCurrentActivity()));
            LinearLayout.LayoutParams llColContainerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            );
            llColContainer.setLayoutParams(llColContainerParams);
            llColContainer.setOrientation(LinearLayout.HORIZONTAL);
            llColContainer.setGravity(Gravity.CENTER);

            ReadableMap colData = columns.getMap(i);
            ReadableArray colValues = colData.getArray("values");

            if (colValues.size() == 0) {
                onFailure.invoke("values array must not be empty");
                return;
            }

            NumberPicker npCol = new NumberPicker(getCurrentActivity());
            LinearLayout.LayoutParams npColParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            npCol.setLayoutParams(npColParams);

            npCol.setMinValue(0);
            npCol.setMaxValue(colValues.size() - 1);

            String[] colDisplayeValues = getColDisplayedValues(colValues);

            npCol.setDisplayedValues(colDisplayeValues);
            npCol.setWrapSelectorWheel(false);
            npCol.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

            if (colData.hasKey("selected")) {
                final int selected = colData.getInt("selected");

                if (selected >= colValues.size()) {
                    onFailure.invoke("selected index is out of range");
                    return;
                }

                npCol.setValue(selected);
            } else {
                npCol.setValue(0);
            }

            llColContainer.addView(npCol);
            pickers.add(npCol);

            if (colData.hasKey("label")) {
                TextView tvColLabel = new TextView(getCurrentActivity());
                LinearLayout.LayoutParams tvColLabelParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                tvColLabel.setLayoutParams(tvColLabelParams);

                tvColLabel.setText(colData.getString("label"));

                llColContainer.addView(tvColLabel);
            }

            llContainer.addView(llColContainer);
        }

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getCurrentActivity());

        if (options.hasKey("title")) {
            alertDialog.setTitle(options.getString("title"));
        }

        if (options.hasKey("message")) {
            alertDialog.setMessage(options.getString("message"));
        }

        alertDialog.setView(llContainer);

        if (options.hasKey("positiveButtonLabel")) {
          alertDialog.setPositiveButton(options.getString("positiveButtonLabel"), new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int whichButton) {
                  ArrayList<Integer> selected = new ArrayList<>();
                  for (int i = 0; i < pickers.size(); i++) {
                      selected.add(pickers.get(i).getValue());
                  }
                  onSuccess.invoke(selected.toString());
              }
          });
        }

        if (options.hasKey("negativeButtonLabel")) {
            alertDialog.setNegativeButton(options.getString("negativeButtonLabel"), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    onSuccess.invoke(-1);
                }
            });
        }

        alertDialog.create().show();
    }

    private String[] getColDisplayedValues(ReadableArray values) {
        String[] displayedValues = new String[values.size()];

        for(int i = 0; i < values.size(); i++) {
            ReadableType indexType = values.getType(i);
            switch(indexType) {
                case Boolean:
                    displayedValues[i] = String.valueOf(values.getBoolean(i));
                    break;
                case Number:
                    Double tmp = values.getDouble(i);
                    if ((tmp == Math.floor(tmp)) && !Double.isInfinite(tmp)) {
                        displayedValues[i] = String.valueOf(values.getInt(i));
                        break;
                    }

                    displayedValues[i] = String.valueOf(tmp);
                    break;
                case String:
                    displayedValues[i] = values.getString(i);
                    break;
                default:
                    displayedValues[i] = "ERROR: Unknown";
            }
        }

        return displayedValues;
    }
}
