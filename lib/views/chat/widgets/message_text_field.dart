import 'package:explore_egypt/utils/constants.dart';
import 'package:flutter/material.dart';

class MessageTextField extends StatelessWidget {
  const MessageTextField({super.key, required this.controller});

  final TextEditingController controller;

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: Container(
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(12),
          boxShadow: [
            BoxShadow(
              blurRadius: 64,
              spreadRadius: -4,
              offset: Offset(0, 14),
              color: AppConstants.kShadowColor.withAlpha(31),
            ),
            BoxShadow(
              blurRadius: 22,
              spreadRadius: -6,
              offset: Offset(0, 8),
              color: AppConstants.kShadowColor.withAlpha(31),
            ),
          ],
        ),
        child: TextField(
          controller: controller,
          textInputAction: TextInputAction.newline,
          keyboardType: TextInputType.multiline,
          maxLines: 5,
          minLines: 1,
          cursorColor: AppConstants.kPrimaryColor,
          decoration: InputDecoration(
            hint: Text(
              'Generate a name of ...',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w500,
                color: AppConstants.kGrey,
              ),
            ),

            contentPadding: EdgeInsets.all(10),

            focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: BorderSide(color: Colors.white),
            ),
            enabledBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: BorderSide(color: Colors.white),
            ),
          ),
        ),
      ),
    );
  }
}
