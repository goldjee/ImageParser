# ImageParser
A tool to mess with YOLO neural network datasets

Relies on .txt files describing in which regions markedObjects are present.
Preserves your original dataset. Output dir is img/processed/.  
Cropping converts values in your .txt files to match new image sizes.  

# Command line arguments
*-c* or *-crop* - crops images. Default size is 320x320  
*-cl* or *-clean* - cleans output directories before any operations  
*-b* or *-balance* - balances dataset. Default ratio is 1:1  
*-re* or *-removeEmpty* - overrides *-b* anl leaves only images with objects present  
