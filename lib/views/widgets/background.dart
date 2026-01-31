import 'package:explore_egypt/utils/constants.dart';
import 'package:flutter/material.dart';

class BackGround extends StatelessWidget {
  const BackGround({super.key});

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            AppConstants.kPrimaryColor,
            AppConstants.kPrimaryVariant,
            AppConstants.kSecondary,
          ],
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
        ),
      ),
      child: Opacity(
        opacity: 0.2,
        child: Image.asset(
          AppConstants.bgImage,
          fit: BoxFit.fill,
          colorBlendMode: BlendMode.multiply,
          height: double.infinity,
          width: double.infinity,
        ),
      ),
    );
  }
}
