set terminal png nocrop enhanced font arial 16 size 2450,500 
set output '<image_name>'

set pm3d map
set iso 4
set samples 4
set xrange [1:*]
set yrange [*:*]
set cbrange [0:2.75]
f(x,y)=2*x+y
set key off
unset xtics
unset ytics
set tics scale 2
set colorbox horizontal user origin 0.2,0.25 size .6,0.08


# Needed by terminals limited on colors:
#set palette maxcolors 26

# Note: no more space in the following multiplot to show demo for
#set pm3d corners2color median
#set pm3d corners2color geomean

set multiplot
# Let us do multiplot grid 3x3:
dx = 1.0/1; dy = 1.0/2;
set size dx,dy

set origin 0*dx,1*dy
set title "<author_work>"
#set pm3d corners2color c1
#set pm3d corners2color c3
set pm3d corners2color c2

splot'<plot.file>' using 2:5:4

unset multiplot

