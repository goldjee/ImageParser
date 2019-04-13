# ImageParser
A tool to mess with YOLO neural network datasets

Relies on .txt files describing in which regions markedObjects are present.
Preserves your original dataset. Output dir is img/processed/.  
Cropping converts values in your .txt files to match new image sizes.  

# Command line arguments
|Argument|Synonym|Description|
|--------|-------|-----------|
|*-clean*|*-cl*|Cleans output directories before any operations|
|*-crop*|*-c*|Crops images. Default size is 320x320.  Anyway, you can specify output size like this: *-c 416*|
|*-augmentRotate*|*-ar*|Augments dataset with rotated images. Angle varie from -2.0 to 2.0 degrees with 20 steps.   You can override it like this: *-ar 25.1 100*|
|*-balance*|*-b*|Balances dataset. Default ratio is 1:1|
|*-removeEmpty*|*-re*|Overrides *-b* anl leaves only images with objects present|
