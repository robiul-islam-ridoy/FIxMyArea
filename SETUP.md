# FixMyArea - Setup Guide

This guide will help you set up the FixMyArea project for local development.

## Prerequisites

- **Android Studio**: Arctic Fox (2020.3.1) or newer
- **JDK**: 11 or higher
- **Android SDK**: API 24 (Android 7.0) minimum, API 36 target
- **Gradle**: 8.0+ (included with project)

## Setup Steps

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/FixMyArea.git
cd FixMyArea
```

### 2. Configure Firebase

Firebase is used for authentication and database.

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project (or use an existing one)
   - Project name: *FixMyArea* (or your choice)
   - Enable Google Analytics (optional)
3. Add an Android app to your Firebase project:
   - **Package name**: `com.example.fixmyarea`
   - **App nickname**: FixMyArea (optional)
   - **SHA-1**: Leave blank for now (needed later for Google Sign-In)
4. Download the `google-services.json` file
5. Place it in: `app/google-services.json`
   ```bash
   # The file should be at this location:
   FixMyArea/app/google-services.json
   ```

6. In Firebase Console, enable authentication:
   - Go to **Authentication** → **Sign-in method**
   - Enable **Email/Password** authentication

7. Create Firestore database:
   - Go to **Firestore Database** → **Create database**
   - Start in **Test mode** (for development)
   - Choose a location closest to you

### 3. Configure Cloudinary

Cloudinary is used for image upload and storage.

1. Create a free account at [Cloudinary](https://cloudinary.com/)
2. Go to your [Dashboard](https://cloudinary.com/console)
3. Note your **Cloud Name** (displayed prominently on dashboard)
4. Create an **unsigned upload preset**:
   - Go to **Settings** → **Upload**
   - Scroll down to "Upload presets"
   - Click **Add upload preset**
   - Set **Signing Mode**: *Unsigned*
   - Set **Preset name**: Choose a name (e.g., `fixmyarea_preset`)
   - (Optional) Set **Folder**: `profile_images`
   - Click **Save**
   - Note the preset name

### 4. Configure local.properties

This file stores your sensitive credentials locally (it's gitignored for security).

1. Copy the example file:
   ```bash
   cp local.properties.example local.properties
   ```

2. Edit `local.properties` and fill in your credentials:
   ```properties
   # Android SDK path (usually auto-configured by Android Studio)
   sdk.dir=/path/to/Android/Sdk
   
   # Cloudinary credentials from step 3
   cloudinary.cloud.name=your_actual_cloud_name
   cloudinary.upload.preset=your_actual_preset_name
   ```

3. Save the file

> ⚠️ **Important**: Never commit `local.properties` to Git! It's already in `.gitignore`.

### 5. Sync and Build

1. Open the project in Android Studio:
   - File → Open → Select the FixMyArea folder
2. Wait for Gradle sync to complete
3. If prompted, accept any SDK licenses
4. Sync project with Gradle files:
   - File → Sync Project with Gradle Files
5. Build the project:
   - Build → Make Project
6. Wait for build to complete

### 6. Run the Application

1. **Using an Emulator**:
   - Tools → Device Manager
   - Create a new virtual device (or use existing)
   - Recommended: Pixel 5 with API 34 (Android 14)
   - Run → Run 'app'

2. **Using a Physical Device**:
   - Enable Developer Options on your Android device
   - Enable USB Debugging
   - Connect via USB
   - Run → Run 'app'

## Project Structure

```
FixMyArea/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/fixmyarea/
│   │       │   ├── auth/          # Login & Registration
│   │       │   ├── ui/             # Main UI screens
│   │       │   ├── firebase/       # Firebase utilities
│   │       │   └── utils/          # Cloudinary & helpers
│   │       ├── res/                # Resources (layouts, etc.)
│   │       └── AndroidManifest.xml
│   ├── google-services.json        # Firebase config (YOU ADD THIS)
│   └── build.gradle.kts
├── local.properties                 # Local config (YOU CREATE THIS)
├── local.properties.example         # Template
└── README.md
```

## Troubleshooting

### Build Errors

**Error**: `google-services.json not found`
- Solution: Make sure you've added `google-services.json` to the `app/` folder

**Error**: `BuildConfig.CLOUDINARY_CLOUD_NAME cannot be resolved`
- Solution: Make sure `local.properties` has your Cloudinary credentials and sync Gradle

**Error**: SDK not found
- Solution: Update `sdk.dir` in `local.properties` to point to your Android SDK

### Runtime Errors

**Firebase Authentication fails**
- Check that Email/Password auth is enabled in Firebase Console
- Verify `google-services.json` is from the correct Firebase project

**Image upload fails**
- Verify Cloudinary credentials in `local.properties`
- Check that upload preset is set to "Unsigned" in Cloudinary dashboard

## Security Notes

⚠️ **Never commit these files to Git:**
- `google-services.json` - Contains Firebase API keys
- `local.properties` - Contains Cloudinary credentials
- Any `*.keystore` or `*.jks` files - App signing keys

These files are already protected in `.gitignore`.

## Need Help?

- Check existing issues on GitHub
- Review Firebase documentation: https://firebase.google.com/docs
- Review Cloudinary documentation: https://cloudinary.com/documentation

## Next Steps

Once the app is running:
1. Try registering a new account
2. Upload a profile image to test Cloudinary integration
3. Explore the codebase
4. Start building new features!

Happy coding! 🚀
