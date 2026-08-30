// Extracts a 6-digit OTP from maestro.copiedText and sets output.otp.

var text = maestro.copiedText || "";
output.copiedText = text;

function isSixDigits(value) {
  if (!value || value.length !== 6) {
    return false;
  }

  for (var i = 0; i < value.length; i++) {
    var c = value.charAt(i);
    if (c < "0" || c > "9") {
      return false;
    }
  }

  return true;
}

function firstSixDigitRun(value) {
  var run = "";
  var inRun = false;
  var tooLong = false;

  for (var i = 0; i <= value.length; i++) {
    var c = i < value.length ? value.charAt(i) : "";
    var isDigit = c >= "0" && c <= "9";

    if (isDigit) {
      if (!inRun) {
        run = "";
        inRun = true;
        tooLong = false;
      }

      run = run + c;

      if (run.length > 6) {
        tooLong = true;
      }
    } else {
      if (inRun && !tooLong && run.length === 6) {
        return run;
      }

      inRun = false;
      run = "";
      tooLong = false;
    }
  }

  return "";
}

var otp = "";
var trimmed = text.trim();

// Case 1: copyTextFrom matched only the OTP TextView.
if (isSixDigits(trimmed)) {
  otp = trimmed;
}

// Case 2: known SMS/notification template, e.g. "Your verification code is 932180".
if (!otp) {
  var marker = "is ";
  var idx = text.indexOf(marker);

  if (idx >= 0) {
    var candidate = text.substring(idx + marker.length, idx + marker.length + 6);

    if (isSixDigits(candidate)) {
      otp = candidate;
    }
  }
}

// Case 3: fallback to any standalone 6-digit run.
if (!otp) {
  otp = firstSixDigitRun(text);
}

output.otp = otp || "";