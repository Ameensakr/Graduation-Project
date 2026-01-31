import 'package:dio/dio.dart';
import 'package:explore_egypt/services/services_constatnts.dart';
import 'package:explore_egypt/services/shared_pref_helper.dart';
import 'package:explore_egypt/utils/api_constants.dart';
import 'package:flutter/foundation.dart';
import 'package:pretty_dio_logger/pretty_dio_logger.dart';

class ApiService {
  
  late Dio dio;
  bool _isRefreshing = false;
  ApiService() {
    dio = Dio(
      BaseOptions(
        baseUrl: ApiConstants.baseUrl,
        connectTimeout: Duration(seconds: 30),
        receiveTimeout: Duration(seconds: 30),
        headers: {'Content-Type': 'application/json'},
      ),
    );

    dio.interceptors.add(
      PrettyDioLogger(
        requestHeader: true,
        requestBody: true,
        responseBody: true,
      ),
    );

    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) async {
          final token = await SharedPrefHelper.getSecuredString(
            ServicesConstants.userToken,
          );

          if (token != null) {
            options.headers['Authorization'] = 'Bearer $token';
          }
          debugPrint("Logout headers: ${options.headers}");

          handler.next(options);
        },
        onError: (error, handler) async {
          if (error.response?.statusCode == 401) {
            if (error.requestOptions.path == ApiConstants.refreshToken) {
              await SharedPrefHelper.clearAlldata();
              return handler.next(error);
            }

            // Only one thread should refresh at a time
            if (!_isRefreshing) {
              _isRefreshing = true;
              try {
                // Call refresh token endpoint
                final refreshResponse = await dio.post(
                  ApiConstants.refreshToken,
                );

                if (refreshResponse.statusCode == 200) {
                  // 👇 Get new token from response
                  final newToken =
                      refreshResponse.data[ServicesConstants.userToken];

                  if (newToken != null) {
                    // Save new token
                    await SharedPrefHelper.setSecureString(ServicesConstants.userToken,newToken);

                    // Update header for retry
                    error.requestOptions.headers['Authorization'] =
                        'Bearer $newToken';

                    // Retry original request with new token
                    _isRefreshing = false;
                    return handler.resolve(
                      await dio.request(
                        error.requestOptions.path,
                        options: Options(
                          method: error.requestOptions.method,
                          headers: error.requestOptions.headers,
                        ),
                        data: error.requestOptions.data,
                        queryParameters: error.requestOptions.queryParameters,
                      ),
                    );
                  }
                }

                // Refresh failed
                await SharedPrefHelper.clearAlldata();
                _isRefreshing = false;
                return handler.next(error);
              } catch (e) {
                debugPrint('Token refresh error: $e');
                await SharedPrefHelper.clearAlldata();
                _isRefreshing = false;
                return handler.next(error);
              }
            }
          }
          handler.next(error);
        },
      ),
    );
  }
}
