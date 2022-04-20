# VocabularyTrainer

Android app written in Kotlin designed for small children to practice their vocabulary in various languages. The UI is designed for children who can not read and is mostly free of text. The UI for adding categories and words is in English.

All languages supported by Google's Text-to-Speech engine are supported.

Categories are created manually and words can be added with images from the phone or by searching for images on Pixabay.com within the app thanks to the free Pixabay API.

The [Android-Image-Cropper](https://github.com/ArthurHub/Android-Image-Cropper) library is used to allow cropping and resizing of chosen images. 
Glide is used to handle image loading.
Coroutines are used for handling threading for IO.
