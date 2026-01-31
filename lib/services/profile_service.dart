import 'package:dio/dio.dart';
import 'package:explore_egypt/models/user_model.dart';
import 'package:explore_egypt/services/api_service.dart';
import 'package:explore_egypt/services/shared_pref_helper.dart';

class ProfileService {
   final ApiService _apiService = ApiService();
   ProfileService();
  Future<dynamic> getUserProfile() async {
    try {
      final response = await _apiService.dio.get('profile');

      final userModel = UserModel.fromjson(response.data);
      await SharedPrefHelper.setObject('user', userModel.toJson());
      return userModel;
    } on DioException {
      throw Exception('Failed to fetch profile');
    } catch (e) {
      throw Exception(e.toString());
    }
  }
}
