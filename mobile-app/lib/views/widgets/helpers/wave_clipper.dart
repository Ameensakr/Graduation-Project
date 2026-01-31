import 'package:flutter/material.dart';

class WaveClipper extends CustomClipper<Path> {
  @override
  getClip(Size size) {
    var path = Path();
        path.moveTo(0, 190);
    
    // TOP WAVE - gentle curve upward
    path.quadraticBezierTo(
      size.width / 3.5,  // Control X: middle
      110,             // Control Y: gentle curve
      size.width,      // End X: right side
      100,             // End Y: same as start
    );

    path.lineTo(size.width, size.height - 140);  
    
    path.quadraticBezierTo(
      size.width * 0.75,       
      size.height - 120,       
      size.width / 2,          
      size.height - 90,        
    );
    
    
    path.quadraticBezierTo(
      size.width *.20,           
      size.height - 50,         
      0,                        
      size.height - 100,        
    );

path.close();


  

    return path;
  }

  @override
  bool shouldReclip(covariant CustomClipper oldClipper) {
    return false;
  }
}
