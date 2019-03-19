# ImageParser
A tool to mess with YOLO neural network datasets

Relies on .txt files describing in which regions objects are present.  
Preserves your original dataset. Output dir is img/processed/.  
Cropping converts values in your .txt files to match new image sizes.  

# Command line arguments
*-c* or *-crop* - crops images. Default size is 320x320  
*-b* or *-balance* - balances dataset. Default ratio is 1:1  
