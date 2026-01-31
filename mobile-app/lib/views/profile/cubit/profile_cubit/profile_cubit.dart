import 'package:explore_egypt/models/user_model.dart';
import 'package:explore_egypt/services/profile_service.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:meta/meta.dart';

part 'profile_state.dart';

class ProfileCubit extends Cubit<ProfileState> {
  final profileService = ProfileService();

  ProfileCubit() : super(ProfileInitial());

  Future<dynamic> getUserData() async {
    emit(ProfileLoading());
    try {
      final  userData = await profileService.getUserProfile();
      
      emit(ProfileLoaded(user: userData));
    } catch (e) {
      String errorMessage = e.toString().replaceFirst('Exception: ', '');

      emit(ProfileLoadedFaild(errorMessage));
    }
  }
}
