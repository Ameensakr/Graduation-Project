import 'package:explore_egypt/utils/validators.dart';
import 'package:explore_egypt/views/widgets/custom_text_field.dart';
import 'package:flutter/material.dart';

class CustomPasswordTextField extends StatefulWidget {
  final TextEditingController controller;
  final String? Function(String?)? validator;
  final String label;
  const CustomPasswordTextField({
    super.key,
    required this.controller,
    this.validator = Validators.passwordValidatore,
    this.label = 'Password'});

  @override
  State<CustomPasswordTextField> createState() =>
      _CustomPasswordTextFieldState();
}

class _CustomPasswordTextFieldState extends State<CustomPasswordTextField> {
  bool _isObscure = true;
  @override
  Widget build(BuildContext context) {
    return CustomTextField(
      controller: widget.controller,
      validator: widget.validator,
      label: widget.label,
      isObscure: _isObscure,
      sufixIcon: IconButton(
        onPressed: () {
          setState(() {
            _isObscure = !_isObscure;
          });
        },
        icon: Icon(
          _isObscure
              ? Icons.visibility_off_outlined
              : Icons.visibility_outlined,
        ),
      ),
    );
  }
}
