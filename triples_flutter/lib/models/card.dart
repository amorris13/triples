enum PropertyValue {
  v0,
  v1,
  v2,
}

enum PropertyType {
  number,
  shape,
  pattern,
  color,
}

class Card implements Comparable<Card> {
  static const int maxVariables = 3;

  final int number;
  final int shape;
  final int pattern;
  final int color;

  Card({
    required this.number,
    required this.shape,
    required this.pattern,
    required this.color,
  })  : assert(number >= 0 && number < maxVariables),
        assert(shape >= 0 && shape < maxVariables),
        assert(pattern >= 0 && pattern < maxVariables),
        assert(color >= 0 && color < maxVariables);

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is Card &&
          runtimeType == other.runtimeType &&
          number == other.number &&
          shape == other.shape &&
          pattern == other.pattern &&
          color == other.color;

  @override
  int get hashCode =>
      number.hashCode ^ shape.hashCode ^ pattern.hashCode ^ color.hashCode;

  @override
  String toString() {
    return 'Card{number: $number, shape: $shape, pattern: $pattern, color: $color}';
  }

  int getValue(PropertyType type) {
    switch (type) {
      case PropertyType.number:
        return number;
      case PropertyType.shape:
        return shape;
      case PropertyType.pattern:
        return pattern;
      case PropertyType.color:
        return color;
    }
  }

  @override
  int compareTo(Card other) {
    int result = number.compareTo(other.number);
    if (result == 0) {
      result = shape.compareTo(other.shape);
      if (result == 0) {
        result = pattern.compareTo(other.pattern);
        if (result == 0) {
          result = color.compareTo(other.color);
        }
      }
    }
    return result;
  }
}
