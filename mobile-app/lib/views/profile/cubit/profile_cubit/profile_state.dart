part of 'profile_cubit.dart';

@immutable
sealed class ProfileState {}

final class ProfileInitial extends ProfileState {}

final class ProfileLoading extends ProfileState {}

final class ProfileLoaded extends ProfileState {
  UserModel user;
  ProfileLoaded({required this.user});
}

final class ProfileLoadedFaild extends ProfileState {
  String errorMessage;
  ProfileLoadedFaild(this.errorMessage);
}
