Blob ratiometrics is a [ImageJ][ij-web] plugin to perform ratiometric analysis between two channels. It uses a mask to define regions of interest for analysis. The ratiometric quantification will be computed for each roi on a 2D-image or on an image stack.  

# Installation
The plugin needs to be build from source. Therefore this repository needs to be cloned and [Maven][mvn] can be used to build the plugin. In the command line use the following lines:

```
git clone https://github.com/eggerbo/ImageJ_BlobRatiometric.git

mvn package

```

Finally the plugin (jar-file) can be dragged and dropped from the target folder on the imageJ main UI. After restarting, the plugin is available through the menu: Plugins > Analyze > Blob Ratiometrics (2D).

# Usage
The plugin needs 3 open images, two florescent images and the image mask. All the images have to have the exact same dimension (2D or 3D).


[ij-web]: http://imagej.net/
[mvn]: https://maven.apache.org/
