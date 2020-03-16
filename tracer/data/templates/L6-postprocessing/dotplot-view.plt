set terminal png
set title "Dotplot view for <author.x> and <author.y>"
set xlabel "<author.work.x>"
set ylabel "<author.work.y>"
#set logscale x
#set logscale y
set output "<output.file>"
plot [1:*] [1:*] '<data.file>' using 2:4 with points pointtype 0 pointsize 3 lt -1 notitle  
