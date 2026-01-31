import 'package:explore_egypt/services/profile_service.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:explore_egypt/services/auth_services.dart';
part 'auth_state.dart';

class AuthCubit extends Cubit<AuthState> {
  final AuthServices _authServices;
  final profileService =ProfileService();
  AuthCubit(this._authServices) : super(AuthInitial());

  Future<dynamic> login({
    required String email,
    required String password,
  }) async {
    emit(AuthLoading());
    try {
      final data = await _authServices.login(email: email, password: password);
      emit(AuthSuccess(data));
    } on Exception catch (e) {
      String errorMessage = e.toString().replaceFirst('Exception: ', '');
      emit(AuthError(errorMessage));
    } catch (e) {
      emit(AuthError('An unexpected error occurred:$e'));
    }
  }

  Future<dynamic> register({
    required String email,
    required String username,
    required String password,
  }) async {
    emit(AuthLoading());
    try {
      final data = await _authServices.createUser(
        email: email,
        password: password,
        username: username,
      );

      emit(AuthRegisterSuccess(data));
    } on Exception catch (e) {
      String errorMessage = e.toString().replaceFirst('Exception: ', '');
      emit(AuthError(errorMessage));
    } catch (e) {
      emit(AuthError('An un expected error occured'));
    }
  }

  Future<dynamic> logout() async {
    try {
      final data = await _authServices.logout();
      debugPrint(data);
      emit(AuthLoggedOut(data));
    } on Exception catch (e) {
      String errorMessage = e.toString().replaceFirst('Exception: ', '');
      emit(AuthError(errorMessage));
    } catch (e) {
      emit(AuthError('An un expected error occured'));
    }
  }
}
