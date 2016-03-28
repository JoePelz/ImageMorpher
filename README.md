#Image Warping Tool

##Working:
* open/capture/display two images
* draw/edit/delete lines, with corresponding lines on the other image.
* save/open project
* render settings: number of frames, values of a, b, and P in the weight equation
* render images: multithreaded, progress view, sparse sampling technique to speed up rendering
* playback images: finger-slide to show animation, frame-forward/frame-backward buttons.


##User Guide:

###Screen 1: (ShowImages.java)
============================
####Layout: 
* Two Top Images:  These are the source and destination images. Touch to change pictures.
* Center Image:  This is your canvas for drawing control lines.
* Bottom buttons: 
 * Save (save your images and lines)
 * Open (Change project)
 * Build (Advance to the rendering settings view)

####Usage:
* Touch the circles at the top of the screen to choose images to use from the camera or the gallery.  
 * If you request from the camera, you will be prompted for camera permission and then need to try again to use the camera.
* Touch and drag to draw a line on the center image. Drag the endpoints to adjust the line.
* Click the top image to switch between src and destination versions of the lines.
* Drag a line off-image to delete it.

* Save will save your images and lines in the current project.
* Open allows you to switch projects.
* Build will advance to the "Render Settings" view, in anticipation of warping.

###Screen 2: (RenderSettings.java)
===============================
####Layout:
* Editable values and sliders, with descriptions above: These describe your number of frames and weight equation.
* Render button: Begin the rendering process
* Progress bar with caption: Display update on rendering progress.
* Playback button: Advance to the playback view to see your results.

####Usage:
* Type values in the edit boxes, or pull the sliders to adjust settings.
* Touch "Render" to process the frames for output
 *See the progress bar update after render is clicked
* Touch "Playback" to view the frames rendered so far.

###Screen 3: (Playback.java)
=========================
####Layout:
* Black background
* Large central image: this is your warped result.
* Left and right buttons: frame forward and backward.

####Usage:
* Slide your finger across the image left and right to see the warp happening
* Use the framing buttons "|<|" and "|>|" to step backwards and forwards respectively 1 frame.





