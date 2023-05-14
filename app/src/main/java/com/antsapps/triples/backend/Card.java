package com.antsapps.triples.backend;

import com.google.common.base.Objects;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class Card {
  public static final int MAX_VARIABLES = 3;

  public final int mNumber;
  public final int mShape;
  public final int mPattern;
  public final int mColor;

  public Card(int number, int shape, int pattern, int color) {
    Preconditions.checkArgument(number >= 0 && number < MAX_VARIABLES, "number = %d", number);
    Preconditions.checkArgument(shape >= 0 && shape < MAX_VARIABLES, "shape = %d", shape);
    Preconditions.checkArgument(pattern >= 0 && pattern < MAX_VARIABLES, "pattern = %d", pattern);
    Preconditions.checkArgument(color >= 0 && color < MAX_VARIABLES, "color = %d", color);

    mNumber = number;
    mShape = shape;
    mPattern = pattern;
    mColor = color;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(mNumber, mShape, mPattern, mColor);
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof Card) {
      Card that = (Card) object;
      return Objects.equal(this.mNumber, that.mNumber)
          && Objects.equal(this.mShape, that.mShape)
          && Objects.equal(this.mPattern, that.mPattern)
          && Objects.equal(this.mColor, that.mColor);
    }
    return false;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("mNumber", mNumber)
        .add("mShape", mShape)
        .add("mPattern", mPattern)
        .add("mColor", mColor)
        .toString();
  }
}
