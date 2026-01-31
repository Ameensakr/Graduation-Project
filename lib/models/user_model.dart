class UserModel {
  final String username;
  final String userId;
  UserModel({required this.userId, required this.username});
  factory UserModel.fromjson(data) {
    return UserModel(userId: data['email'], username: data['username']);
  }
  Map<String, dynamic> toJson() => {'username': username, 'email': userId};
}
