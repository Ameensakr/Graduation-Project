import 'package:explore_egypt/utils/constants.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class ChatBubble extends StatelessWidget {
  const ChatBubble({super.key, required this.isBot, required this.message});
  final bool isBot;
  final String message;
  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: isBot
          ? MainAxisAlignment.start
          : MainAxisAlignment.end,
      children: [
        Container(
          width: MediaQuery.of(context).size.width * 0.8,
          padding: const EdgeInsets.symmetric(vertical: 15, horizontal: 16),
          margin: const EdgeInsets.all(10),
          decoration: BoxDecoration(
            boxShadow: [
              BoxShadow(
                blurRadius: 8,
                spreadRadius: 2,
                offset: Offset(0, 4),
                color: Colors.black.withAlpha(38),
              ),
              BoxShadow(
                blurRadius: 6.1,
                spreadRadius: 0,
                offset: Offset(0, 4),
                color: Colors.black.withAlpha(26),
              ),
            ],
            borderRadius: BorderRadius.only(
              topLeft: Radius.circular(19),
              topRight: Radius.circular(19),
              bottomLeft: isBot ? Radius.zero : Radius.circular(19),
              bottomRight: isBot ? Radius.circular(19) : Radius.zero,
            ),
            color: isBot ? AppConstants.kWhite : AppConstants.kPrimaryColor,
          ),
          child: Text(
            message,
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w400,
              color: isBot ? Colors.black : Colors.white,
              letterSpacing: 0,
            ),
            softWrap: true,
          ),
        ),
        if (isBot)
          IconButton(
            icon: Icon(Icons.copy_rounded, size: 24, color: AppConstants.kGrey),
            onPressed: () {
              Clipboard.setData(ClipboardData(text: message));
            },
          ),
      ],
    );
  }
}
