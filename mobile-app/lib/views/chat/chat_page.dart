import 'package:explore_egypt/utils/constants.dart';
import 'package:explore_egypt/views/chat/widgets/chat_bubble.dart';
import 'package:explore_egypt/views/chat/widgets/custom_row.dart';
import 'package:flutter/material.dart';

class ChatPage extends StatelessWidget {
  const ChatPage({super.key});

  final List messages = const [
    {
      'isBot': false,
      'message': "I would like to know more about Egypt's history.",
    },
    {'isBot': true, 'message': "ok i will tell you about it"},
    {
      'isBot': true,
      'message':
          'Egypt has a rich and ancient history that dates back to around 3100 BC when Upper and Lower Egypt were unified under the first pharaoh, Narmer. The civilization of ancient Egypt is renowned for its monumental architecture, including the iconic pyramids, temples, and tombs. The Egyptians made significant advancements in various fields such as writing (hieroglyphics), mathematics, medicine, and engineering.Egypt has a rich and ancient history that dates back to around 3100 BC when Upper and Lower Egypt were unified under the first pharaoh, Narmer. The civilization of ancient Egypt is renowned for its monumental architecture, including the iconic pyramids, temples, and tombs. The Egyptians made significant advancements in various fields such as writing (hieroglyphics), mathematics, medicine, and engineering.',
    },
  ];
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(
          'ChatBot AI',
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        centerTitle: true,
        scrolledUnderElevation: 0,
        surfaceTintColor: Colors.transparent,
        backgroundColor: Colors.transparent,
      ),
      body: Column(
        children: [
          Divider(color: AppConstants.kGrey, thickness: 1),
          Expanded(
            child: ListView.builder(
              itemCount: messages.length,
              itemBuilder: (context, index) {
                return ChatBubble(
                  isBot: messages[index]['isBot']!,
                  message: messages[index]['message']!,
                );
              },
            ),
          ),
          Padding(
            padding: EdgeInsets.all(8.0),
            child: CustomRow(
              controller: TextEditingController(),
              onPressed: () {
                // Handle send button press
              },
            ),
          ),
        ],
      ),
    );
  }
}
