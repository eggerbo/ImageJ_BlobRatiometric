Blob ratiometrics is a [ImageJ][ij-web] plugin to perform ratiometric analysis between two channels. It uses a mask to define regions of interest for analysis. The ratiometric quantification will be computed for each roi on a 2D-image or on an image stack.  

# Installation
Details on how to install a plugin in ImageJ can be found on the official [wiki][ij-plugin]. To install the plugin with ImageJ, you'll need the jar-file containing the compiled and packaged code of this plugin. The following two sub-section explain the two ways to get them.
 
## Release binaries
The major versions published here on github [Releases][bin]. From there you can directly download the jar-files.

## Build from source
Smaller modifications might not be released each time. But you can build the plugin from source in order to have the latest (not yet released) version. To this end firstly clone this repository and secondly compile it with [Maven][mvn]. With a command line the following lines should do the trick:

```
git clone https://github.com/eggerbo/ImageJ_BlobRatiometric.git
cd ImageJ_BlobRatiometric
mvn package

```

# Usage
Once the plugin is installed and ImageJ has been restarted, the plugin is available through the ImageJ menu: Plugins > Analyze > Blob Ratiometrics (2D). The plugin needs three open images, two fluorescent images and the image mask. All the images have to have the exact same dimension (2D or 3D).


[ij-web]: http://imagej.net/
[ij-plugin]: http://imagej.net/Installing_3rd_party_plugins
[mvn]: https://maven.apache.org/
[bin]: https://github.com/eggerbo/ImageJ_BlobRatiometric/releases