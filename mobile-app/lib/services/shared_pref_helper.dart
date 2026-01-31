import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';

class SharedPrefHelper {
  SharedPrefHelper._();

  /// remove a value from sharedPreferences with given [key].
  static removeData(String key) async {
    debugPrint('SharedPrefHelper : data with key : $key has been removed ');
    SharedPreferences sharedPreferences = await SharedPreferences.getInstance();
    await sharedPreferences.remove(key);
  }

  /// remove all key and values from sharedPreferences.
  static clearAlldata() async {
    debugPrint('SharedPrefHelper : all data had been removed');
    try {
      SharedPreferences sharedPreferences =
          await SharedPreferences.getInstance();
      await sharedPreferences.clear();
    } on PlatformException catch (e) {
      if (e.code == 'channel-error') {
        debugPrint('Ignored SharedPreferences channel-error (safe to ignore).');
      } else {
        rethrow;
      }
    } catch (e) {
      debugPrint('Other error: $e');
    }
  }

  /// save a [value] with a [key] in sharedPreferences.
  static setData(String key, value) async {
    debugPrint('SharedPrefHelper : setData with key : $key and value: $value ');
    SharedPreferences sharedPreferences = await SharedPreferences.getInstance();

    switch (value.runtimeType) {
      case int:
        await sharedPreferences.setInt(key, value);
        break;
      case String:
        await sharedPreferences.setString(key, value);
        break;
      case double:
        await sharedPreferences.setDouble(key, value);
        break;
      case bool:
        await sharedPreferences.setBool(key, value);
        break;
      default:
        return null;
    }
  }

  /// save an object [value] with a [key] in sharePrefrences.
  static setObject(String key, Map<String, dynamic> value) async {
    debugPrint(
      'SharedPrefHelper : setObject with key : $key and value: $value ',
    );
    SharedPreferences sharedPreferences = await SharedPreferences.getInstance();
    final String userJson = jsonEncode(value); // Serialize to JSON string
    await sharedPreferences.setString(key, userJson);
  }

  /// Gets an object value from sharedPrefrences with a given [key].
  static getObject(String key) async {
    debugPrint('SharedPrefHelper : get object with key : $key');
    SharedPreferences sharedPreferences = await SharedPreferences.getInstance();
    String? jsonString = sharedPreferences.getString(key);
    if (jsonString != null) {
      return jsonDecode(jsonString);
    }
  }

  /// Gets a bool value from sharedPreferences with given [key].
  static getBool(String key) async {
    debugPrint('SharedPrefHelper : get bool with key : $key');
    SharedPreferences sharedPreferences = await SharedPreferences.getInstance();
    return sharedPreferences.getBool(key) ?? false;
  }

  /// Gets a doble value from sharedPreferences with given [key].
  static getDouble(String key) async {
    debugPrint('SharedPrefHelper : get double with key : $key');
    SharedPreferences sharedPreferences = await SharedPreferences.getInstance();
    return sharedPreferences.getDouble(key) ?? 0.0;
  }

  /// Gets a int value from sharedPreferences with given [key].
  static getInt(String key) async {
    debugPrint('SharedPrefHelper : get int with key : $key');
    SharedPreferences sharedPreferences = await SharedPreferences.getInstance();
    return sharedPreferences.getInt(key) ?? 0;
  }

  /// Gets a string value from sharedPreferences with given [key].
  static getString(String key) async {
    debugPrint('SharedPrefHelper : get bool with key : $key');
    SharedPreferences sharedPreferences = await SharedPreferences.getInstance();
    return sharedPreferences.getString(key) ?? '';
  }

  /// saves a [value] with a [key] secured.

  static setSecureString(String key, value) async {
    const flutterSecureStorage = FlutterSecureStorage();
    debugPrint(
      'FlutterSecureStorage : set secured string with a value : $value and key: $key ',
    );
    await flutterSecureStorage.write(key: key, value: value);
  }

  /// Get Secured String with [key].
  static getSecuredString(String key) async {
    const flutterSecureStorage = FlutterSecureStorage();
    debugPrint('FlutterSecureStorage : get secured string with a key: $key ');
    return await flutterSecureStorage.read(key: key) ?? '';
  }

  //   Future<bool> isLoggedIn() async {
  //   try {
  // const flutterSecureStorage = FlutterSecureStorage();

  //     final token = await flutterSecureStorage.read(key: _accessTokenKey);
  //     return token != null;
  //   } catch (e) {
  //     return false;
  //   }
  // }
}
