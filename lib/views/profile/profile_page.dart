import 'package:explore_egypt/views/profile/cubit/profile_cubit/profile_cubit.dart';
import 'package:explore_egypt/views/widgets/welcome.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class ProfilePage extends StatefulWidget {
  const ProfilePage({super.key, });
  @override
  State<ProfilePage> createState() => _ProfilePageState();
}

class _ProfilePageState extends State<ProfilePage> {
  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (context) => ProfileCubit()..getUserData(),
      child: Scaffold(
        body: BlocBuilder<ProfileCubit, ProfileState>(
          builder: (context, state) {
            if (state is ProfileLoading) {
              return CircularProgressIndicator();
            } else if (state is ProfileLoaded) {
              return Welcome(user: state.user,);
            } else if (state is ProfileLoadedFaild) {
              return Center(child: Text(state.errorMessage));
            }
              return Center(child: Text('welcome'));
            
          },
        ),
      ),
    );
  }
}
