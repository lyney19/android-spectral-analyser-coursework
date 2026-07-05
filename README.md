# NoteScope

## 📌 Description

**NoteScope** is an Android application written in **Java** for real-time audio spectrum visualization and spectrogram analysis.

The app captures audio from the device microphone, performs FFT-based spectral analysis, and renders both the frequency spectrum and spectrogram in real time. It was originally developed as a university coursework project and later refactored into a standalone pet project focused on digital signal processing.

## 🚀 Features

* 🎤 Real-time microphone recording
* 📊 Live FFT spectrum visualization
* 🌈 Real-time spectrogram
* ⚙️ Configurable DSP parameters (Frame Size, Overlap, Hop Size)
* 💾 Persistent DSP settings using `SharedPreferences`
* 🎨 Material 3 inspired custom views
* 🔄 Smooth rendering with immutable spectrum snapshots
* 🧵 Multi-threaded audio processing using the Producer–Consumer pattern

## ⚙️ How It Works

The application processes audio through a real-time DSP pipeline:

```text
Microphone
    ↓
AudioRecorder
    ↓
BlockingQueue
    ↓
SpectrumAnalyser (FFT)
    ↓
SpectrumProcessor
    ↓
Circular Buffer
    ↓
ViewModel
    ↓
Spectrum View / Spectrogram View
```

To achieve smooth real-time rendering:

* audio recording and FFT processing run on separate threads;
* `BlockingQueue` is used to synchronize producers and consumers;
* processed spectra are stored in a thread-safe circular buffer;
* the UI receives immutable snapshots approximately 30 times per second, preventing race conditions.

## 🛠 Tech Stack

* Java
* Android SDK
* AudioRecord API
* FFT (Fast Fourier Transform)
* MVVM
* ViewModel
* Data Binding
* Custom Views
* SharedPreferences
* Material 3

## 📷 Screenshots

| Spectrum                                          | Spectrogram                                          | Settings                                          |
|---------------------------------------------------|------------------------------------------------------|---------------------------------------------------|
| <img src="screenshots/spectrum.png" width="250"/> | <img src="screenshots/spectrogram.png" width="250"/> | <img src="screenshots/settings.png" width="250"/> |

## 📥 Installation & Run

1. Clone the repository.
2. Open the project in Android Studio.
3. Build and run the application on an Android device (microphone permission required).

If you want to install the app without building the project, you can download the APK directly from the repository:

* [Download ](app/release/app-release.apk)[`app-release.apk`](app/release/app-release.apk)

## 📁 Project Structure

```text
audio/          # Audio recording and DSP
binding/        # Data Binding adapters
storage/        # Settings persistence
ui/             # Activities, Fragments and custom Views
viewmodel/      # UI state and DSP coordination
network/        # Legacy coursework networking implementation
```

## 🧠 What I Learned

Through this project I gained practical experience with:

* digital signal processing fundamentals;
* FFT-based spectrum analysis;
* concurrent programming in Java;
* Producer–Consumer architecture;
* thread synchronization and race condition prevention;
* custom View development for Android;
* real-time rendering optimization;
* MVVM architecture.

## 🔮 Future Improvements

### DSP

* [ ] Track the detected note as a curve over time
* [ ] Add volume tracking
* [ ] Add additional spectrum processing algorithms
* [ ] Improve note detection accuracy

### UI/UX

* [ ] Add a light theme
* [ ] Add a color scheme for the spectrogram
* [ ] Improve the zoom mechanism in the spectrogram
* [ ] Add screenshot export

### Performance

* [ ] Rendering optimizations
* [ ] Performance benchmarking
* [ ] Memory usage improvements