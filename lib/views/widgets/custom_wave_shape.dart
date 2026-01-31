import 'package:explore_egypt/views/widgets/helpers/wave_clipper.dart';
import 'package:explore_egypt/views/widgets/helpers/custom_wave_border.dart';
import 'package:flutter/material.dart';

class CustomWaveShape extends StatelessWidget {
  const CustomWaveShape({
    super.key,
  });
  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: ShapeDecoration(
        shape: const CustomWaveBorder(),
        shadows: [
          BoxShadow(
            color: Colors.black.withValues(alpha: .5),
            blurRadius: 25,
            spreadRadius: 5,
          ),
        ],
      ),
      child: ClipPath(
        clipper: WaveClipper(),
        child: Container(
          height: double.infinity,
          color: Colors.white,
        ),
      ),
    );
  }
}
