import 'package:explore_egypt/utils/constants.dart';
import 'package:explore_egypt/views/chat/widgets/message_text_field.dart';
import 'package:flutter/material.dart';

class CustomRow extends StatelessWidget {
  const CustomRow({
    super.key,
    required this.controller,
    required this.onPressed,
  });
  final TextEditingController controller;
  final VoidCallback onPressed;
  @override
  Widget build(BuildContext context) {
    return Row(
      spacing: 8,
      children: [
        MessageTextField(controller: controller),
        ElevatedButton(
          onPressed: onPressed,
          style: ElevatedButton.styleFrom(
            backgroundColor: AppConstants.kPrimaryColor,
            shape: CircleBorder(eccentricity: 0),
            fixedSize: Size(54, 54),
            padding: EdgeInsets.all(0),
          ),
          child: Image.asset(
            AppConstants.sendIcon2,
            width: 24,
            height: 24,
            color: Colors.white,
          ),
        ),
      ],
    );
  }
}