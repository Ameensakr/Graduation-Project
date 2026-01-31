import 'package:explore_egypt/models/user_model.dart';
import 'package:flutter/widgets.dart';

class Welcome extends StatelessWidget {
  final UserModel user;
  const Welcome( {super.key,required this.user});

  @override
  Widget build(BuildContext context) {
    return  Center(child: Text('Welcome Back, ${user.username}',style: TextStyle(fontSize: 30),));
  }
}
