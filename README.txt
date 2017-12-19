### USAGE - environment ###
to run the program you must have OpenCV and Ant installed.

Please Configure the following paths in the file HoleFilling\build.xml file:

Set the path to your OpenCv .jar file at line 5:
<property name="lib.dir" value="C:\opencv\build\java"/>

Set the path to your OpenCv native library at line 84:
<sysproperty key="java.library.path" path="C:\opencv\build\java\x64"/>
(E.g the path to the folder which contains the relevant .dll file in windows)

The values which are in the examples are the values taken from my OS

### USAGE - run the program ###
To build and run the program, just hit:

ant <optional args>

from the same location as the build.xml file

To just run the program, without building it, add the word run, e.g:

ant <optional args> run

Optional command line args:

-Dimgpath='absolute\path\to\input\img'
If you the argument -Dimgpath will not be given, the example_input_img.jpg under the projects base dir will be used

-Dalg=0 will run the algorithm from section 2, -Dalg=1 will run the approximating one from section 5

-Dz=<any int> will set Z from the formula in the task with the given integer

-Deps=<any double, possibly in scientific representation, e.g simply: 1e-8>
default value for z is 4 for algorithm 0 and 1 for algorithm 1

-Dtest=true will log more calculations to help monitor the process
e.g the average value of the the missing pixels after they where filled,
and the average of the pixels in the boundaries. We expect these average to be close.
the default value for Dtest is false, meaning no logs will be printed

-Dmockhole will, in case that -Dmock=true , configure the position of the mock hole;
-Dmockhole=<'topmostRow height leftmostCol width'>
there are default values for the mockhole in case it's not given in command line

The program's output will be created under HoleFilling\outputImages

### Design and more ###
I used java and OpenCV for this task, using OpenCV only to load and output images, and in the Mat, MatOfDouble object
instead of 2-dims arrays. Of course, I haven't used any "smart" methods these object offer, to manipulate the image.

I chose to implement a class which holds the input image, and helps us manipulate it easily - HoleFiller

The methods which are required in the task itself, and more helper methods I used, are in the class Utils:

For implementation of section 1 see:
public static Hole findHole(Mat m)

For implementation of the algorithm from section 2 see:
Utils.public static void fillHole(MatOfDouble m, Hole hole, int z, double eps)
Utils.public static void fillHole(MatOfDouble m, Hole hole, BiFunction<Index, Index[], MatOfDouble> weightFunc)

For implementation of approximating algorithm from section 5 see:
Utils.public static void fillHoleCircular(MatOfDouble m, Hole hole)

I also chose to use a "runner program" - HoleFillingRunner, which also has helper methods to parse command line
arguments, run different program routines, log data, ext.

Finally, there are MockUtils class which uses for helper methods to et mock data, e.g placing a mock hole
in matrix, and more classes to represent objects, such as Index, Neighborhood, ext.

### Theoretical questions ###

## Section 4 ##

The basic algorithm goes through all pixels in the hole (n) and for each one, calculates the weights function based on
all pixels in the boundaries (m), therefore it will run in O(n*m).

If we assume that the hole is a square, we get that it's a [sqrt(n) x sqrt(n)] square, which has 4*sqrt(n) pixels in
it's boundaries. So we get runtime of O(n^1.5).

If we assume that the hole is a circle, then it's perimeter (boundary) will contain 2*pi*sqrt(pi*n) = 2*pi^1.5*sqrt(n)
pixels, and so it will also run at O(n^1.5).

## Section 5 ##

for this section I implemented an approximating algorithm which fills the outmost perimeter of missing pixels first,
and then goes to the inner perimeter, and so on, until all missing pixels are filled.
To fill each pixel, the algorithm considers the average of the pixels which aren't missing,
only from the neighborhood of this pixel. This way, we obtain O(n) runtime.
