import 'package:flutter/rendering.dart';

class CustomWaveBorder extends ShapeBorder {
  const CustomWaveBorder();
  @override
  EdgeInsetsGeometry get dimensions => EdgeInsets.zero;

  @override
  Path getInnerPath(Rect rect, {TextDirection? textDirection}) =>
      _createWavePath(rect);

  @override
  Path getOuterPath(Rect rect, {TextDirection? textDirection}) =>
      _createWavePath(rect);
  Path _createWavePath(Rect rect) {
    final path = Path();
    path.moveTo(0, 190);

    // TOP WAVE - gentle curve upward
    path.quadraticBezierTo(
      rect.width / 3.5, // Control X: middle
      110, // Control Y: gentle curve
      rect.width, // End X: right side
      100, // End Y: same as start
    );

    path.lineTo(rect.width, rect.height - 140);

    path.quadraticBezierTo(
      rect.width * 0.75,
      rect.height - 120,
      rect.width / 2,
      rect.height - 90,
    );

    path.quadraticBezierTo(
      rect.width * .20,
      rect.height - 50,
      0,
      rect.height - 100,
    );

    path.close();

    return path;
  }

  @override
  void paint(Canvas canvas, Rect rect, {TextDirection? textDirection}) {}

  @override
  ShapeBorder scale(double t) => this;
}
