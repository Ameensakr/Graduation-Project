part of 'auth_cubit.dart';

@immutable
sealed class AuthState {}

final class AuthInitial extends AuthState {}

final class AuthLoading extends AuthState {}

final class AuthSuccess extends AuthState {
  final Map<String, dynamic> data;

  AuthSuccess(this.data);
}

final class AuthRegisterSuccess extends AuthState {
  final Map<String, dynamic> data;
  AuthRegisterSuccess(this.data);
}

final class AuthError extends AuthState {
  final String message;
  AuthError(this.message);
}

final class AuthLoggedOut extends AuthState {
  final String message;
  AuthLoggedOut(this.message);
}
