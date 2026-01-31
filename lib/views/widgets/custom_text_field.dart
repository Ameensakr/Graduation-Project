import 'package:explore_egypt/utils/constants.dart';
import 'package:flutter/material.dart';

class CustomTextField extends StatelessWidget {
  const CustomTextField({
    super.key,
    required this.controller,
    this.isObscure = false,
    required this.validator,
    required this.label,
    this.sufixIcon,
  });
  final TextEditingController controller;
  final bool isObscure;
  final String? Function(String?)? validator;
  final String label;
  final Widget? sufixIcon;
  @override
  Widget build(BuildContext context) {
    return TextFormField(
      controller: controller,
      obscureText: isObscure,
      decoration: InputDecoration(
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: BorderSide(color: AppConstants.kPrimaryVariant, width: 0),
          
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: BorderSide(color: AppConstants.kErrorColor, width: 2),
        ),
        hint: Text(label, style: TextStyle(fontSize: 16)),
        suffixIcon: sufixIcon,
        fillColor: AppConstants.kWhite,
        filled: true,
      ),
      validator: validator,
    );
  }
}
