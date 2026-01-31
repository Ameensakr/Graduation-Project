import 'package:explore_egypt/services/api_service.dart';

class ChatServices {
  final ApiService _apiService;

  ChatServices(this._apiService);

  Future<dynamic> getConversation(int conversationID, String userId) async {
    try {
      final response = await _apiService.dio.get(
        'chat/conversation',
        queryParameters: {'conversationId': conversationID, 'userId': userId},
      );
      return response.data;
    } catch (e) {
      throw Exception(e.toString());
    }
  }

  Future<dynamic> getAllConversations(String userId) async {
    try {
      final response = await _apiService.dio.get(
        'chat/all',
        queryParameters: {'userId': userId},
      );
      return response.data;
    } catch (e) {
      throw Exception(e.toString());
    }
  }

  Future<dynamic> sendMessage(
    String conversationId, {
    required String userId,
    required String sender,
    required String message,
  }) async {
    try {
      final response = await _apiService.dio.post(
        'chat/send',
        data: {
          'userId': userId,
          'sender': sender,
          'message': message,
          'ConversationId': conversationId,
        },
      );
      return response.data;
    } catch (e) {
      throw Exception(e.toString());
    }
  }
}
