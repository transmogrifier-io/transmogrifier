# transmogrifier
## What's working
- Reading the urls and returning a code response
- Extracting the function from the url

## What's not working (yet)
- Writing to a file
- Writing to a URL
- Validator function
- Reading sinks from URL
- Reading sources from URL

## Setup
You would need to create a new project to run the new transmongrifier.dart file.

Link for Flutter setup (VsCode): https://docs.flutter.dev/get-started/install

Keep in mind you need an emulator that supports android SDK of 29 and over (not sure for IOS). 

Once you've created the project in android/app/build.gradle in the defaultConfig change the following `minSdkVersion flutter.minSdkVersion` to `minSdkVersion 29'

For now, copy over the transmongrifier.dart in the same directory as the main.dart file.

### Required dependencies in pubspec.yaml file
```
dependencies:
  flutter:
    sdk: flutter

  http: ^0.13.6
  flutter_js: ^0.6.0
```
Also, add the following for the section that requires flutter packages (also pubspec.yaml)
```
# The following section is specific to Flutter packages.
flutter:

  # The following line ensures that the Material Icons font is
  # included with your application, so that you can use the icons in
  # the material Icons class.
  uses-material-design: true

  # To add assets to your application, add an assets section, like this:
  assets:
    - assets/
```

### Create assets folder in the main directory
Copy and paste the van-texas-manifest.json into the folder so that it can be referenced.

### Code to put in lib/main.dart
Replace the main function with the following:
```
import 'package:flutter/material.dart';
import './transmogrifier.dart' as transmongrifier;

Future<void> main() async {

  WidgetsFlutterBinding.ensureInitialized();

  transmongrifier.main();

  // runApp(const MyApp());
}
```
Leave the rest of the auto generated code in the file alone.


## Final comments
These instructions are very brief and somewhat vague so please shoot me a message on discord if any of this doesn't make sense or your stuck. If needed we can possibly hop on a call Saturday night to discuss about any issues.
