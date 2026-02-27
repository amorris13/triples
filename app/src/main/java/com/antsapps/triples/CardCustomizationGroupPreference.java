package com.antsapps.triples;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceViewHolder;

import com.antsapps.triples.backend.Card;
import com.antsapps.triples.cardsview.DiamondShape;
import com.antsapps.triples.cardsview.HexagonShape;
import com.antsapps.triples.cardsview.SymbolDrawable;
import com.antsapps.triples.cardsview.TriangleShape;

import java.util.Arrays;
import java.util.List;

public class CardCustomizationGroupPreference extends Preference {

    private Spinner[] mColorSpinners = new Spinner[3];
    private Spinner[] mShapeSpinners = new Spinner[3];
    private Spinner mPatternSpinner;
    private SampleCardView[] mSampleCards = new SampleCardView[3];

    private String[] mColorValues;
    private String[] mShapeValues;
    private String[] mPatternValues;

    private boolean mBinding = false;

    public CardCustomizationGroupPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_card_customization_group);

        mColorValues = context.getResources().getStringArray(R.array.entryvalues_color_pref);
        mShapeValues = context.getResources().getStringArray(R.array.entryvalues_shape_pref);
        mPatternValues = context.getResources().getStringArray(R.array.entryvalues_pattern_pref);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mBinding = true;

        mColorSpinners[0] = (Spinner) holder.findViewById(R.id.color_0_spinner);
        mColorSpinners[1] = (Spinner) holder.findViewById(R.id.color_1_spinner);
        mColorSpinners[2] = (Spinner) holder.findViewById(R.id.color_2_spinner);

        mShapeSpinners[0] = (Spinner) holder.findViewById(R.id.shape_0_spinner);
        mShapeSpinners[1] = (Spinner) holder.findViewById(R.id.shape_1_spinner);
        mShapeSpinners[2] = (Spinner) holder.findViewById(R.id.shape_2_spinner);

        mPatternSpinner = (Spinner) holder.findViewById(R.id.pattern_spinner);

        mSampleCards[0] = (SampleCardView) holder.findViewById(R.id.sample_card_0);
        mSampleCards[1] = (SampleCardView) holder.findViewById(R.id.sample_card_1);
        mSampleCards[2] = (SampleCardView) holder.findViewById(R.id.sample_card_2);

        setupSpinners();
        updateSampleCards();
        mBinding = false;
    }

    private void setupSpinners() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        setupColorSpinners(sharedPrefs);
        setupShapeSpinners(sharedPrefs);
        setupPatternSpinner(sharedPrefs);
    }

    private void setupColorSpinners(SharedPreferences sharedPrefs) {
        String[] keys = {
                getContext().getString(R.string.pref_color_0),
                getContext().getString(R.string.pref_color_1),
                getContext().getString(R.string.pref_color_2)
        };

        for (int i = 0; i < 3; i++) {
            final int index = i;
            mColorSpinners[i].setAdapter(new ColorAdapter(getContext(), mColorValues));
            String value = sharedPrefs.getString(keys[i], "");
            mColorSpinners[i].setSelection(Math.max(0, Arrays.asList(mColorValues).indexOf(value)));

            mColorSpinners[i].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (mBinding) return;
                    String newValue = mColorValues[position];
                    handleUniqueness(keys, mColorSpinners, index, newValue, mColorValues);
                    updateSampleCards();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }

    private void setupShapeSpinners(SharedPreferences sharedPrefs) {
        String[] keys = {
                getContext().getString(R.string.pref_shape_0),
                getContext().getString(R.string.pref_shape_1),
                getContext().getString(R.string.pref_shape_2)
        };

        for (int i = 0; i < 3; i++) {
            final int index = i;
            mShapeSpinners[i].setAdapter(new ShapeAdapter(getContext(), mShapeValues));
            String value = sharedPrefs.getString(keys[i], "");
            mShapeSpinners[i].setSelection(Math.max(0, Arrays.asList(mShapeValues).indexOf(value)));

            mShapeSpinners[i].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (mBinding) return;
                    String newValue = mShapeValues[position];
                    handleUniqueness(keys, mShapeSpinners, index, newValue, mShapeValues);
                    updateSampleCards();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }

    private void setupPatternSpinner(SharedPreferences sharedPrefs) {
        String key = getContext().getString(R.string.pref_shaded_pattern);
        mPatternSpinner.setAdapter(new PatternAdapter(getContext(), mPatternValues));
        String value = sharedPrefs.getString(key, "stripes");
        mPatternSpinner.setSelection(Math.max(0, Arrays.asList(mPatternValues).indexOf(value)));

        mPatternSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mBinding) return;
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                editor.putString(key, mPatternValues[position]);
                editor.apply();
                updateSampleCards();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void handleUniqueness(String[] keys, Spinner[] spinners, int index, String newValue, String[] allValues) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String oldValue = prefs.getString(keys[index], "");
        if (newValue.equals(oldValue)) return;

        SharedPreferences.Editor editor = prefs.edit();
        for (int i = 0; i < 3; i++) {
            if (i == index) continue;
            String otherValue = prefs.getString(keys[i], "");
            if (newValue.equals(otherValue)) {
                // Swap
                editor.putString(keys[i], oldValue);
                final int otherIndex = i;
                spinners[otherIndex].post(() -> {
                    mBinding = true;
                    spinners[otherIndex].setSelection(Arrays.asList(allValues).indexOf(oldValue));
                    mBinding = false;
                });
                break;
            }
        }
        editor.putString(keys[index], newValue);
        editor.apply();
    }

    private void updateSampleCards() {
        for (int i = 0; i < 3; i++) {
            // Sample cards:
            // Card 1: color 0, shape 0, pattern 0 (Empty)
            // Card 2: color 1, shape 1, pattern 1 (Shaded)
            // Card 3: color 2, shape 2, pattern 2 (Solid)
            mSampleCards[i].setCard(new Card(1, i, i, i));
        }
    }

    private static class ColorAdapter extends ArrayAdapter<String> {
        ColorAdapter(Context context, String[] values) {
            super(context, 0, values);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return createView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return createView(position, convertView, parent);
        }

        private View createView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new ImageView(getContext());
                int size = (int) (48 * getContext().getResources().getDisplayMetrics().density);
                convertView.setLayoutParams(new ViewGroup.LayoutParams(size, size));
                int padding = (int) (8 * getContext().getResources().getDisplayMetrics().density);
                convertView.setPadding(padding, padding, padding, padding);
            }
            ShapeDrawable drawable = new ShapeDrawable(new RectShape());
            drawable.getPaint().setColor(Color.parseColor(getItem(position)));
            ((ImageView) convertView).setImageDrawable(drawable);
            return convertView;
        }
    }

    private static class ShapeAdapter extends ArrayAdapter<String> {
        ShapeAdapter(Context context, String[] values) {
            super(context, 0, values);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return createView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return createView(position, convertView, parent);
        }

        private View createView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new ImageView(getContext());
                int size = (int) (48 * getContext().getResources().getDisplayMetrics().density);
                convertView.setLayoutParams(new ViewGroup.LayoutParams(size, size));
                int padding = (int) (8 * getContext().getResources().getDisplayMetrics().density);
                convertView.setPadding(padding, padding, padding, padding);
            }
            android.graphics.drawable.shapes.Shape shape;
            switch (getItem(position)) {
                case "square": shape = new RectShape(); break;
                case "circle": shape = new OvalShape(); break;
                case "triangle": shape = new TriangleShape(); break;
                case "diamond": shape = new DiamondShape(); break;
                case "hexagon": shape = new HexagonShape(); break;
                default: shape = new RectShape(); break;
            }
            ShapeDrawable drawable = new ShapeDrawable(shape);
            drawable.getPaint().setColor(Color.GRAY);
            ((ImageView) convertView).setImageDrawable(drawable);
            return convertView;
        }
    }

    private static class PatternAdapter extends ArrayAdapter<String> {
        PatternAdapter(Context context, String[] values) {
            super(context, 0, values);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return createView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return createView(position, convertView, parent);
        }

        private View createView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new ImageView(getContext());
                int size = (int) (48 * getContext().getResources().getDisplayMetrics().density);
                convertView.setLayoutParams(new ViewGroup.LayoutParams(size, size));
                int padding = (int) (8 * getContext().getResources().getDisplayMetrics().density);
                convertView.setPadding(padding, padding, padding, padding);
            }

            Card sampleCard = new Card(0, 0, 1, 0); // number 0, shape 0, pattern 1 (shaded), color 0
            SymbolDrawable drawable = new SymbolDrawable(getContext(), sampleCard, getItem(position));
            // We need a bitmap because SymbolDrawable might use Shaders that need bounds
            int size = (int) (48 * getContext().getResources().getDisplayMetrics().density);
            android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(new Rect(0, 0, size, size));
            drawable.draw(canvas);

            ((ImageView) convertView).setImageBitmap(bitmap);

            return convertView;
        }
    }
}
