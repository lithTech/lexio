package lt.ru.lexio.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import lt.ru.lexio.R;
import lt.ru.lexio.ui.GeneralCallback;
import lt.ru.lexio.ui.training.TrainingWordOrder;

/**
 * Created by lithTech on 26.12.2016.
 */

public class WordOrderPicker {

    public static void show(Activity activity, final GeneralCallback callback) {
        View content = LayoutInflater.from(activity).inflate(R.layout.dialog_word_orders, null);
        final ListView lvWordOrders = (ListView) content.findViewById(R.id.lvWordOrders);
        lvWordOrders.setAdapter(new Adapter(activity,
                activity.getResources().getColor(R.color.colorPrimaryDark),
                TrainingWordOrder.values()));

        final AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(content)
//                .setPositiveButton(R.string.dialog_Choose, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        TrainingWordOrder order = ((Adapter) lvWordOrders.getAdapter()).getSelected();
//                        if (order != null)
//                            callback.done(order);
//                    }
//                })
                //.setTitle(R.string.dialog_word_orders)
                .setCancelable(true)
                .show();


        lvWordOrders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TrainingWordOrder order = (TrainingWordOrder) lvWordOrders.getItemAtPosition(position);
                if (order != null) {
                    callback.done(order);
                    dialog.dismiss();
                }
            }
        });
    }

}

class Adapter extends ArrayAdapter<TrainingWordOrder>{

    private TrainingWordOrder selected;
    private int selectedColor;
    private Drawable defBkg = null;

    public Adapter(Context context, int selectedColor, TrainingWordOrder[]objects) {
        super(context, R.layout.content_word_order_item, objects);
        this.selectedColor = selectedColor;
    }

    public TrainingWordOrder getSelected() {
        return selected;
    }

    public void setSelected(TrainingWordOrder order) {
        selected = order;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.content_word_order_item, null, false);
            if (defBkg == null)
                defBkg = convertView.findViewById(R.id.tvTitle).getBackground();
        }

        TrainingWordOrder order = getItem(position);

        TextView tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
        TextView tvDescr = (TextView) convertView.findViewById(R.id.tvDescr);

        tvTitle.setText(order.getStringResTitleId());
        tvDescr.setText(order.getStringResDescrId());

        tvTitle.setBackground(defBkg);
        tvDescr.setBackground(defBkg);

        if (order == selected) {
            tvTitle.setBackgroundColor(selectedColor);
            tvDescr.setBackgroundColor(selectedColor);
        }

        return convertView;
    }
}