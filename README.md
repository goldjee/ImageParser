# ImageParser
A tool to mess with YOLO neural network datasets.

Relies on .txt files describing in which regions markedObjects are present.

# Command line arguments
Supply path to your config.json which you want to use.

# Config.json
The app uses JSON config files with instructions what and how it is supposed to do.
They are called "operations". Anyway, it the app will execute them in pre-defined order
and you are not able to change it.

**NB.** Duplicate operations with different parameters are not allowed.

There is reference config-example.json in project root. It is pretty self-explanatory.
