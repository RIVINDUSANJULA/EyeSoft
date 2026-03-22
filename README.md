# EyeSoft 👁️💻

A lightweight, cross-platform desktop application built in Java to help developers and heavy computer users combat digital eye strain. EyeSoft enforces the 20-20-20 rule by running silently in the background and automatically prompting you to take screen breaks.

## ✨ Features

* **Background Execution:** Runs unobtrusively in your System Tray (Windows) or Menu Bar (macOS).
* **Automated Screen Blocking:** Forces a full-screen, borderless overlay when it is time to rest your eyes, preventing you from ignoring the break.
* **Cross-Platform Compatibility:** Built with Java Swing, ensuring it works seamlessly across both Windows and macOS environments.
* **Minimal Resource Usage:** Designed to be lightweight so it doesn't interfere with your heavy IDEs or development tools.

## 🛠️ Tech Stack

* **Language:** Java
* **UI Framework:** Java Swing (`javax.swing`, `java.awt`)

## 🚀 Getting Started

Since there is no packaged executable just yet, you can run EyeSoft directly from the source code.

### Prerequisites
* **Java Development Kit (JDK)** installed on your machine (Java 8 or higher recommended).

### Installation & Execution

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/RIVINDUSANJULA/EyeSoft.git](https://github.com/RIVINDUSANJULA/EyeSoft.git)
   cd EyeSoft/src/main/java/com/eyesoft
   ````

*(Note: Adjust the path if your `EyeRest.java` file is located in the root directory rather than `src/main/java...`)*

2.  **Compile the Java file:**

    ```bash
    javac EyeRest.java
    ```

3.  **Run the application:**

    ```bash
    java EyeRest
    ```

## 💻 Usage

1.  Once started, the application will not appear in your taskbar or dock.
2.  Look for the small green circle icon in your **System Tray** (bottom right on Windows) or **Menu Bar** (top right on macOS).
3.  The app will run a 20-minute work timer, followed by a 20-second mandatory full-screen rest period.
4.  To exit the application completely, right-click the System Tray/Menu Bar icon and select **"Exit Eye Rest"**.


## 🛣️ Roadmap / Future Enhancements

  - [ ] Add Machine Learing Support to Identify Users Usage and Tiredness.

## 👨‍💻 Collaborators

  * **Rivindu Sanjula** - [@RIVINDUSANJULA](https://www.google.com/search?q=https://github.com/RIVINDUSANJULA)
  * **Gagana Perera** - [@Gagana-Perera](https://github.com/Gagana-Perera)
  * **Nimsara** - [@Nimsara-boop](https://github.com/Nimsara-boop)
