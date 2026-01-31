import 'package:explore_egypt/utils/constants.dart';
import 'package:explore_egypt/utils/validators.dart';
import 'package:explore_egypt/views/auth/cubit/auth_cubit/auth_cubit.dart';
import 'package:explore_egypt/views/widgets/background.dart';
import 'package:explore_egypt/views/widgets/custom_button.dart';
import 'package:explore_egypt/views/widgets/custom_password_text_field.dart';
import 'package:explore_egypt/views/widgets/custom_text_field.dart';
import 'package:explore_egypt/views/widgets/custom_wave_shape.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class RegisterPage extends StatefulWidget {
  const RegisterPage({super.key});

  @override
  State<RegisterPage> createState() => _RegisterPageState();
}

class _RegisterPageState extends State<RegisterPage> {
  TextEditingController emailController = TextEditingController();

  TextEditingController passController = TextEditingController();
  TextEditingController confirmPassController = TextEditingController();
  TextEditingController nameController = TextEditingController();
  GlobalKey<FormState> formKey = GlobalKey();
  @override
  void dispose() {
    super.dispose();
    emailController.dispose();
    passController.dispose();
    nameController.dispose();
    confirmPassController.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      resizeToAvoidBottomInset: false,
      backgroundColor: Colors.transparent,
      body: BlocConsumer<AuthCubit, AuthState>(
        listener: (context, state) {
          if (state is AuthRegisterSuccess) {
            ScaffoldMessenger.of(
              context,
            ).showSnackBar(SnackBar(content: Text(state.data['message'])));

            Navigator.pop(context);
          } else if (state is AuthError) {
            ScaffoldMessenger.of(
              context,
            ).showSnackBar(SnackBar(content: Text(state.message)));
          }
        },
        builder: (context, state) {
          return Stack(
            children: [
              BackGround(),
              CustomWaveShape(),
              SingleChildScrollView(
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  child: Form(
                    key: formKey,
                    child: SizedBox(
                      height: MediaQuery.sizeOf(context).height,
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        spacing: 20,
                        children: [
                          Text(
                            'Sign Up',
                            style: TextStyle(
                              fontSize: 30,
                              color: AppConstants.kPrimaryColor,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          CustomTextField(
                            controller: nameController,
                            validator: Validators.validateName,
                            label: 'User name',
                          ),
                          CustomTextField(
                            controller: emailController,
                            validator: Validators.emailValidator,
                            label: 'Email',
                          ),

                          CustomPasswordTextField(controller: passController),
                          CustomPasswordTextField(
                            controller: confirmPassController,
                            validator: (value) =>
                                Validators.validatePassConfirm(
                                  value,
                                  passController.text,
                                ),
                            label: 'Confirm Password',
                          ),

                          CustomButton(
                            onPressed: () async {
                              if (formKey.currentState!.validate()) {
                                await context.read<AuthCubit>().register(
                                  email: emailController.text,
                                  username: nameController.text,
                                  password: passController.text,
                                );
                              }
                            },
                            label: 'Sign Up',
                          ),

                          Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Text('I already have an account?'),
                              TextButton(
                                onPressed: () {
                                  Navigator.pop(context);
                                },
                                child: Text(
                                  'Login',
                                  style: TextStyle(
                                    color: AppConstants.kPrimaryColor,
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}
