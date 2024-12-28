## How to run the app

1   -   Create a new project on Firebase

2   -   Add an Android app to the project

3   -   Download the `google-services.json` file and add it to the app directory

4   -   create a file named 'app/src/main/res/values/strings.xml' and add your client id and gemini api key as shown below



```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">convoai</string>
    <string name="default_web_client_id">YOUR-CLIENT-ID-HERE</string>
    <string name="gemini_api_key">YOUR_GEMINI_API_KEY</string>
</resources>
```