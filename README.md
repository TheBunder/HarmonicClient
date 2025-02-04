# Harmonic - Android Client

This repository contains the Android client-side code for Harmonic, an application designed to automate the counting of short sound segments within longer recordings. Harmonic simplifies the process of repetitive sound counting, freeing users from manual effort and allowing them to focus on more important tasks. A complete description of the project architecture and overall functionality, including the server-side components, can be found in the [server-side](https://github.com/TheBunder/HarmonicServer) repository.

Harmonic addresses the common need to count recurring sounds within a longer audio clip. For example, a user can record the sound of a single keyboard key press and then record a longer session of typing. Harmonic will then automatically count how many times that key press sound occurred in the recording. This automation has broad applications, from enhancing sports performance (e.g., counting punches or jump rope repetitions) to research and data analysis.

## System Description

Harmonic consists of the following key components:

* **User Authentication:** The app includes a secure login system, enabling users to access their personal account.  This ensures that user data and recordings are protected.
* **Sound Recording:** Users can record short sound segments, which will be used in later analysis. Additionally, users have the option to save these sound segments within the app for future reference and analysis.  This allows users to build a library of sounds for different counting tasks.
* **Sound Comparison and Counting:** Recorded sound segments are sent to the server for similarity comparison and occurrence counting. The server uses FFT for this analysis.
* **Data Encryption:** Users' personal information is encrypted before transmission to the server to ensure secure data handling using AES and Diffie–Hellman encryptions.
* **Server Interaction:** The application communicates with a server, transmitting sound data for analysis and receiving occurrence counts.  This client-server architecture allows for efficient processing of audio data.

## Features

* **Sound Segment Recording:** Users can easily record short sound segments (e.g., a single key press, a specific sound event) using the Android device's microphone.
* **Long Recording Analysis:** Users can record longer audio clips (e.g., a typing session, a workout) using the Android device's microphone to be analyzed for the frequency of the short sound segment.
* **Automated Counting:** Harmonic automatically counts the occurrences of the short sound within the long recording, eliminating manual counting.
* **Real-time Results:** The app displays the counting results in real-time during the recording process.

## Installation

1. **Prerequisites:**
    * Android Studio installed.
    * Android SDK installed and configured in Android Studio.
    * A physical Android device or emulator for testing.
    * **Important:** The client application and the server *must* be on the same private network (e.g., behind a NAT router).  This is essential for communication between the client and server.

2. **Steps:**
    1. Clone the repository: `https://github.com/TheBunder/HarmonicClient.git`
    2. Open Android Studio and select "Import project".
    3. Navigate to the cloned repository directory and select the `build.gradle` file.
    4. Android Studio will import the project and download the necessary dependencies.

## Technologies Used

* Java
* Android SDK
* Retrofit 2.9.0 (for networking)
* AudioTools 1.2.3 (for audio processing)
* Bouncy Castle 1.70 (for AES and Diffie-Hellman encryption)

## Usage

1.  Open the Harmonic app on your Android device.
2.  Record a short sound segment by tapping the big record button.
3.  Record a longer audio clip by tapping the big record button.
4.  The results will be displayed in real-time during the recording.

## Relationship to Server

The Android client-side application interacts with the server-side API by sending the raw audio data to the server for analysis. The server returns the count of the short sound segments, which the client then displays to the user. The server-side code is responsible for processing audio data using the FFT algorithm, providing user authentication using username and password.

## App Screens
![App_screens](https://github.com/user-attachments/assets/23748e32-692f-4fde-94e1-130eab3e2f2a)
