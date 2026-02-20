// goes through the style sheet   
for (var j = 0; j < document.styleSheets.length; j++) {   
  var styleRules = document.styleSheets[j].rules;   
  for (var i=0; i< styleRules.length; i++) {   
    // find all background images that uses png   
    var imageFile = styleRules[i].style.backgroundImage;   
    if (imageFile.indexOf(".png") > -1) {   
      // rewrites the tag with IE6 PNG transparency   
      styleRules[i].style.backgroundImage = "none";   
      styleRules[i].style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(enabled='true', sizingMethod='scale', src='" + unescape(imageFile.substring(5,imageFile.length  - 1)) + "')";   
    }   
  }   
}  