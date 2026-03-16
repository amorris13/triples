package com.antsapps.triples.backend;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public final class Card implements Comparable<Card> {
  public static final int MAX_VARIABLES = 3;

  public enum PropertyType {
    NUMBER,
    SHAPE,
    PATTERN,
    COLOR
  }

  public final int mNumber;
  public final int mShape;
  public final int mPattern;
  public final int mColor;

  public Card(int number, int shape, int pattern, int color) {
    Preconditions.checkArgument(number >= 0 && number < MAX_VARIABLES, "number = %s", number);
    Preconditions.checkArgument(shape >= 0 && shape < MAX_VARIABLES, "shape = %s", shape);
    Preconditions.checkArgument(pattern >= 0 && pattern < MAX_VARIABLES, "pattern = %s", pattern);
    Preconditions.checkArgument(color >= 0 && color < MAX_VARIABLES, "color = %s", color);

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

  public int getValue(PropertyType type) {
    switch (type) {
      case NUMBER:
        return mNumber;
      case SHAPE:
        return mShape;
      case PATTERN:
        return mPattern;
      case COLOR:
        return mColor;
      default:
        throw new IllegalArgumentException();
    }
  }

  @Override
  public int compareTo(Card o) {
    int result = Integer.compare(mNumber, o.mNumber);
    if (result == 0) {
      result = Integer.compare(mShape, o.mShape);
      if (result == 0) {
        result = Integer.compare(mPattern, o.mPattern);
        if (result == 0) {
          result = Integer.compare(mColor, o.mColor);
        }
      }
    }
    return result;
  }
}
