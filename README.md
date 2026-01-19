# FixMyArea 🏘️

A community-driven Android application for reporting and tracking local area issues. Empower your community to identify and resolve problems collaboratively.

![Platform](https://img.shields.io/badge/Platform-Android-green)
![Language](https://img.shields.io/badge/Language-Java-orange)
![License](https://img.shields.io/badge/License-MIT-blue)

## Features ✨

- 🔐 **Secure Authentication** - User registration and login with Firebase Auth
- 👤 **User Profiles** - Personalized profiles with profile pictures
- 📸 **Image Upload** - Upload and store images via Cloudinary
- 🏗️ **Clean Architecture** - Well-organized package structure
- 🔥 **Firebase Integration** - Authentication and Firestore database
- 🎨 **Modern UI** - Clean and intuitive user interface

## Screenshots

*Coming soon...*

## Tech Stack 🛠️

### Frontend
- **Language**: Java
- **Platform**: Android (API 24+)
- **UI**: Material Design Components
- **Image Loading**: Glide

### Backend & Services
- **Authentication**: Firebase Authentication
- **Database**: Firebase Firestore
- **Image Storage**: Cloudinary
- **HTTP Client**: OkHttp

### Build & Tools
- **Build System**: Gradle (Kotlin DSL)
- **IDE**: Android Studio
- **Version Control**: Git

## Getting Started 🚀

### Prerequisites

- Android Studio Arctic Fox or newer
- JDK 11 or higher
- Android SDK (API 24-36)
- Firebase account (free tier)
- Cloudinary account (free tier)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/FixMyArea.git
   cd FixMyArea
   ```

2. **Set up Firebase**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Download `google-services.json` and place in `app/` folder
   - Enable Email/Password authentication
   - Create a Firestore database

3. **Set up Cloudinary**
   - Create account at [Cloudinary](https://cloudinary.com/)
   - Get your Cloud Name from dashboard
   - Create an unsigned upload preset

4. **Configure credentials**
   ```bash
   cp local.properties.example local.properties
   # Edit local.properties and add your credentials
   ```

5. **Build and run**
   - Open project in Android Studio
   - Sync project with Gradle files
   - Run on emulator or device

📖 For detailed setup instructions, see [SETUP.md](SETUP.md)

## Project Structure 📁

```
com.example.fixmyarea/
├── auth/           # Authentication (Login, Register)
├── ui/             # Main application screens
├── firebase/       # Firebase utilities & constants
└── utils/          # Helper classes (Cloudinary, etc.)
```

## Security 🔒

This project follows security best practices:

- ✅ API keys stored in `local.properties` (gitignored)
- ✅ Firebase config in `google-services.json` (gitignored)
- ✅ No hardcoded credentials in source code
- ✅ BuildConfig for compile-time configuration
- ✅ Unsigned uploads for Cloudinary (no sensitive API secrets)

### Files You Should Never Commit
- `google-services.json` - Firebase credentials
- `local.properties` - API keys and secrets
- `*.keystore`, `*.jks` - Signing keys

All sensitive files are protected in `.gitignore`.

## Configuration 📝

The app uses environment-based configuration via `local.properties`:

```properties
# Cloudinary
cloudinary.cloud.name=your_cloud_name
cloudinary.upload.preset=your_preset_name
```

These values are automatically loaded into `BuildConfig` at build time.

## Building for Production 🏭

1. Create a release keystore
2. Configure signing in `app/build.gradle.kts`
3. Build release APK/AAB:
   ```bash
   ./gradlew assembleRelease
   # or
   ./gradlew bundleRelease
   ```

## Contributing 🤝

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Roadmap 🗺️

- [ ] Issue reporting functionality
- [ ] Map integration for location tracking
- [ ] Push notifications for updates
- [ ] Admin dashboard
- [ ] Issue categorization and filtering
- [ ] User voting/upvoting system
- [ ] Dark mode support

## License 📄

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments 🙏

- Firebase for backend services
- Cloudinary for image management
- Material Design for UI guidelines
- Android community for inspiration

## Contact 📧

**Your Name**
- GitHub: [@yourusername](https://github.com/robiul-islam-ridoy)
- Email: robiulislamhr.bd@gmail.com

## Project Status 🚧

This project is currently in active development.

---

Made with ❤️ for the community
# FIxMyArea
