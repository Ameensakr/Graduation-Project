import 'package:explore_egypt/utils/constants.dart';
import 'package:flutter/material.dart';
import 'package:flutter_svg/svg.dart';

class CustomNavigationDestination extends StatelessWidget {
  const CustomNavigationDestination({
    super.key, required this.label, required this.icon,
  });
final String label ;
final String icon ;
  @override
  Widget build(BuildContext context) {
    return NavigationDestination(
      icon: SvgPicture.asset(icon,height: 24,),
      label: label,
      selectedIcon: SvgPicture.asset(
        icon,
        colorFilter: ColorFilter.mode(
          AppConstants.kPrimaryColor,
          BlendMode.srcIn,
        ),
        height:24,
      ),
    );
  }
}