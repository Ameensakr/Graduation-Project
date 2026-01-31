# explore_egypt

Cross-platform Flutter Graduation-Project app focused on basic authentication, profile and chat screens.

## Quick start

1. Install Flutter: https://flutter.dev
2. Get dependencies:
    ```sh
    flutter pub get
    ```
3. Run the app:
    - Mobile emulator / device:
      ```sh
      flutter run
      ```


## Project entry
- Main entry: lib/main.dart
- Pubspec: pubspec.yaml

## Project structure (important parts)

- lib/
  - main.dart
  - models/
    - user_model.dart
  - services/
    - api_service.dart
    - auth_services.dart
    - chat_services.dart
    - profile_service.dart
    - services_constatnts.dart
    - shared_pref_helper.dart
  - utils/
    - api_constants.dart
    - constants.dart
    - route_names.dart
    - validators.dart
  - view_models/
    - cubits/
      - auth_cubit/
        - auth_cubit.dart
        - auth_state.dart


  - views/
    - auth/
      - cubit/
        - auth_cubit/
          - auth_cubit.dart
          - auth_state.dart
      - login_page.dart
      - register_page.dart
    - chat/
      - cubit/
        - chat_cubit/
          - chat_cubit.dart
          - chat_state.dart
      - chat_page.dart
    - profile/
      - cubit/
        - profile_cubit/
          - profile_cubit.dart
          - profile_state.dart
      - profile_page.dart
    - widgets/
      - background.dart
      - custom_button.dart
      - custom_password_text_field.dart
      - custom_text_field.dart
      - custom_wave_shape.dart
      - message_text_field.dart
      - welcome.dart
      - helpers/
        - custom_wave_border.dart
        - wave_clipper.dart

## Notable implementation details

- State management: Cubits under lib/view_models/cubits for auth, chat and profile.
- Networking: ApiService handles HTTP calls and configuration.
- Auth: AuthServices + AuthCubit implement authentication flow.
- Persistence: SharedPrefHelper for local storage of tokens/preferences.
- UI: Reusable widgets in lib/views/widgets.

## How to extend

- Add screens under lib/views.
- Add new services to lib/services and expose them via cubits in lib/view_models.
- Keep models in lib/models and constants/validators in lib/utils.

## Tests

- Add unit/widget tests under the test/ folder and run:
  ```sh
  flutter test
  ```

## Contributing

- Follow existing folder organization and naming conventions.
- Open issues or PRs with concise descriptions and reproducible steps.

## License

No license file included. Add a LICENSE if you intend to publish or open-source the project.
