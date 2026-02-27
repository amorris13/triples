package com.antsapps.triples;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.widget.ImageView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceViewHolder;

import com.antsapps.triples.cardsview.DiamondShape;
import com.antsapps.triples.cardsview.HexagonShape;
import com.antsapps.triples.cardsview.TriangleShape;

public class CardCustomizationPreference extends ListPreference {

    public static final int TYPE_COLOR = 0;
    public static final int TYPE_SHAPE = 1;

    private final int mCustomizationType;

    public CardCustomizationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CardCustomizationPreference);
        mCustomizationType = a.getInt(R.styleable.CardCustomizationPreference_customizationType, TYPE_COLOR);
        a.recycle();
        setWidgetLayoutResource(R.layout.preference_preview_widget);
    }

    @Override
    protected void onClick() {
        if (getEntries() == null || getEntryValues() == null) {
            super.onClick();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getDialogTitle() != null ? getDialogTitle() : getTitle());

        ListAdapter adapter = new CustomizationAdapter(getContext(),
                R.layout.preference_dialog_item, getEntries(), getEntryValues());

        builder.setSingleChoiceItems(adapter, findIndexOfValue(getValue()), (dialog, which) -> {
            String value = getEntryValues()[which].toString();
            if (callChangeListener(value)) {
                setValue(value);
            }
            dialog.dismiss();
        });

        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private class CustomizationAdapter extends ArrayAdapter<CharSequence> {
        private final CharSequence[] mValues;

        public CustomizationAdapter(Context context, int resource, CharSequence[] objects, CharSequence[] values) {
            super(context, resource, objects);
            mValues = values;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.preference_dialog_item, parent, false);
            }

            ImageView previewImage = convertView.findViewById(R.id.preview);
            TextView titleText = convertView.findViewById(R.id.title);

            titleText.setText(getItem(position));

            String value = mValues[position].toString();
            Drawable drawable;
            if (mCustomizationType == TYPE_COLOR) {
                drawable = createColorDrawable(value);
            } else {
                drawable = createShapeDrawable(value);
            }
            previewImage.setImageDrawable(drawable);

            return convertView;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        ImageView previewImage = (ImageView) holder.findViewById(R.id.preview);
        if (previewImage != null) {
            updatePreview(previewImage);
        }
    }

    private void updatePreview(ImageView imageView) {
        String value = getValue();
        if (value == null) {
            return;
        }

        Drawable drawable;
        if (mCustomizationType == TYPE_COLOR) {
            drawable = createColorDrawable(value);
        } else {
            drawable = createShapeDrawable(value);
        }
        imageView.setImageDrawable(drawable);
    }

    private Drawable createColorDrawable(String hexColor) {
        ShapeDrawable drawable = new ShapeDrawable(new RectShape());
        try {
            drawable.getPaint().setColor(Color.parseColor(hexColor));
        } catch (IllegalArgumentException e) {
            drawable.getPaint().setColor(Color.BLACK);
        }
        return drawable;
    }

    private Drawable createShapeDrawable(String shapeName) {
        Shape shape;
        switch (shapeName) {
            case "square":
                shape = new RectShape();
                break;
            case "circle":
                shape = new OvalShape();
                break;
            case "triangle":
                shape = new TriangleShape();
                break;
            case "diamond":
                shape = new DiamondShape();
                break;
            case "hexagon":
                shape = new HexagonShape();
                break;
            default:
                shape = new RectShape();
                break;
        }
        ShapeDrawable drawable = new ShapeDrawable(shape);
        drawable.getPaint().setColor(Color.GRAY);
        return drawable;
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        super.onSetInitialValue(defaultValue);
        notifyChanged();
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        notifyChanged();
    }
}
