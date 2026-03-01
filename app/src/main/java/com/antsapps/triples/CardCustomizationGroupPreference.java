package com.antsapps.triples;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Shader;
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

import com.antsapps.triples.cardsview.DiamondShape;
import com.antsapps.triples.cardsview.HexagonShape;
import com.antsapps.triples.cardsview.SampleCardView;
import com.antsapps.triples.cardsview.SymbolDrawable;
import com.antsapps.triples.cardsview.TriangleShape;

import java.util.Arrays;

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

        setupPropertySpinners(
            new String[] {
                getContext().getString(R.string.pref_color_0),
                getContext().getString(R.string.pref_color_1),
                getContext().getString(R.string.pref_color_2)
            },
            mColorSpinners,
            mColorValues,
            new ColorAdapter(getContext(), mColorValues),
            sharedPrefs);

        setupPropertySpinners(
            new String[] {
                getContext().getString(R.string.pref_shape_0),
                getContext().getString(R.string.pref_shape_1),
                getContext().getString(R.string.pref_shape_2)
            },
            mShapeSpinners,
            mShapeValues,
            new ShapeAdapter(getContext(), mShapeValues),
            sharedPrefs);

        setupPatternSpinner(sharedPrefs);
    }

    private void setupPropertySpinners(
            final String[] keys,
            final Spinner[] spinners,
            final String[] allValues,
            ArrayAdapter<String> adapter,
            SharedPreferences sharedPrefs) {
        for (int i = 0; i < 3; i++) {
            final int index = i;
            spinners[i].setAdapter(adapter);
            String defaultValue = allValues[i]; // Use index-based defaults as fallback
            String value = sharedPrefs.getString(keys[i], defaultValue);
            spinners[i].setSelection(Math.max(0, Arrays.asList(allValues).indexOf(value)));

            spinners[i].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (mBinding) return;
                    String newValue = allValues[position];
                    handleUniqueness(keys, spinners, index, newValue, allValues);
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
        String defaultValue = allValues[index];
        String oldValue = prefs.getString(keys[index], defaultValue);
        if (newValue.equals(oldValue)) return;

        SharedPreferences.Editor editor = prefs.edit();
        for (int i = 0; i < 3; i++) {
            if (i == index) continue;
            String otherDefaultValue = allValues[i];
            String otherValue = prefs.getString(keys[i], otherDefaultValue);
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
            // Card 1: 2 symbols (number 1), color 0, shape 0, pattern 0 (empty)
            // Card 2: 2 symbols (number 1), color 1, shape 1, pattern 1 (shaded)
            // Card 3: 2 symbols (number 1), color 2, shape 2, pattern 2 (solid)
            mSampleCards[i].setProperties(getContext(), 1, i, i, i);
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
                ((ImageView) convertView).setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
            int size = (int) (48 * getContext().getResources().getDisplayMetrics().density);
            ShapeDrawable drawable = new ShapeDrawable(new RectShape());
            drawable.setBounds(0, 0, size, size);
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
                ((ImageView) convertView).setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
            int size = (int) (48 * getContext().getResources().getDisplayMetrics().density);
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
            drawable.setBounds(0, 0, size, size);
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

            android.graphics.drawable.shapes.Shape shape = new RectShape();
            int color = Color.BLACK;
            Shader shader = SymbolDrawable.getShaderForPatternId(getContext(), 1, color, getItem(position));
            SymbolDrawable drawable = new SymbolDrawable(shape, color, shader);

            int size = (int) (48 * getContext().getResources().getDisplayMetrics().density);
            // We draw at a larger size and scale down to match how CardDrawable works
            // and ensure correct pattern thickness.
            Bitmap bitmap = Bitmap.createBitmap(size * 2, size * 2, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(new Rect(0, 0, size * 2, size * 2));
            drawable.draw(canvas);

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, size, size, true);
            ((ImageView) convertView).setImageBitmap(scaledBitmap);

            return convertView;
        }
    }
}
