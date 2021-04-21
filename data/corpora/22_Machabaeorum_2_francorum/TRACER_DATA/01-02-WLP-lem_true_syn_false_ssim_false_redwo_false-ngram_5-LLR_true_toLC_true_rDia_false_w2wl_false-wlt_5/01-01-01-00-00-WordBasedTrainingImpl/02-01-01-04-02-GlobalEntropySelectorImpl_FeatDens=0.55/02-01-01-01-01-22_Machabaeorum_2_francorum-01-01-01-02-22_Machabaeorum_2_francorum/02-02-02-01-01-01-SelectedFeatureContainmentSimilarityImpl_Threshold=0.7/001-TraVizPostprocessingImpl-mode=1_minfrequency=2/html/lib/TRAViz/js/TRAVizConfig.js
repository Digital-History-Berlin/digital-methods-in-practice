/*

Copyright (C) 2014, Stefan Jänicke.

Permission is hereby granted, free of charge, to any person obtaining a
copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following conditions:

1. The above copyright notice and this permission notice shall be included
in all copies or substantial portions of the Software.

2. It is prohibited to remove, hide or modify any visual copyright notice
generated by this Software.

3. When using screenshots of any part of the visual output generated by
this Software for presentations or on websites the following link MUST
be clearly visible for the beholder: http://traviz.vizcovery.org

4. When using screenshots of any part of the visual output generated by
this Software in publications, the following reference MUST be inserted: 

S. Jänicke, A. Geßner, M. Büchler and G. Scheuermann (2014). Visualizations
for Text Re-use. In Proceedings of the 5th International Conference on
Information Visualization Theory and Applications, IVAPP 2014, pages 59–70.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

(Fair Academic License (FAL), http://vizcovery.org/fal.html)

*/

/**
 * -------------------------------------------------------
 * CLASS TRAVizConfig 
 * -------------------------------------------------------
 * configuration of the visualization
 * the preconfigured options <this.options> can be overwritten by a json object <options> with individual configurations
 */
function TRAVizConfig(options) {

	this.options = {

		/* General */
		colors: [ "red", "blue", "green", "rgb(230,230,0)", "orange",
                      "#996600", "purple", "#FF00FF", "#66FFFF", "#339999" ], // colors used to identify the various edition flows		
		normalize: true, // if the sentences shall be normalized or not (remove special characters)
		lineBreaks: true, // if line breaks are allowed or not (if true, only the width of the given div is used)
		lineNumbering: true, // if line numbers are shown when lineBreaks are used
		lineNumberingText: "Line ", // text of line numbers
		rtl: false, // if labels should be drawn from right to left (for arabic, hebrew)
		popupLabel: "occurrences", // header label to be shown in the popup window
		optimizedAlignment: true, // computes a better alignment at the expense of runtime
		editionLabels: true, // if edition labels are shown in a popup when hovering edges

		/* Text Vertices */		
		baseColor: '#3E576F', // color used for text and joined connections		
		vertexBackground: 'rgba(242,242,242,0.75)', // false or a CSS color for the text backgrounds		
		font: 'Georgia', // text font		
		startAndEnd: true, // if start and end vertex are shown and linked to all paths
		collapseLabels: 0, // text labels are only shown for vertices with more than the given value
		interpolateFontSize: false, // if true, the font size of the vertices is interpolated between 'fontSizeMin' and 'fontSizeMax'
		fontSizeMin: 10, // minimum font size
		fontSizeMax: 50, // maximum font size
		fontSizeIncrease: 4, // the number of pixels the labels grow by edition if interpolateFontSize = false
		
		/* Connections */
		edgeGap: 5, // minimum gap between two connections; required when adjusting the connections horizontally and vertically		
		curveRadius: 10, // radius of the curves
		connectionType: 'all', // how the connections shall be displayed: 
						// 'all' for displaying each individual stream, 
						// 'joined' to merge all parallel connections, or 
						// 'majority' to merge only if more than half of the edges are routed between the same vertices
		majorityPercentage: 0.5, // an edge becomes a majority edge when the given percentage of editions passes it

		editDistance: false, // false (or 0) if only exact matches between two words shall be merged or edit distance dependent on the word lengths computed with the formula 2*editDistance/(|word1|+|word2|)

		splitAndMerge: true, // if the user is allowed to interactively split vertices or merge via drag&drop

		transpositions: true, // if transpositions shall be determined and visualized on mouseover

	};

	if ( typeof options != 'undefined') {
		$.extend(this.options, options);
	};

};

/**
 * Getter for a unique vertex index (required for vertex hash in the graph)
 */
TRAVizConfig.prototype.getVertexIndex = function(){
	if( typeof this.vid == 'undefined' ){	
		this.vid = 0;
	}
	return ++this.vid;
};

/**
 * converts HSV to RGB
 * adapted from http://jsres.blogspot.de/2008/01/convert-hsv-to-rgb-equivalent.html
 */
TRAVizConfig.prototype.Hsv2rgb = function(h,s,v){
	var r, g, b;
	var var_h = h * 6;
	if(var_h==6){
		var_h = 0;
	}
	var var_i = Math.floor( var_h );
	var var_1 = v*(1-s);
	var var_2 = v*(1-s*(var_h-var_i));
	var var_3 = v*(1-s*(1-(var_h-var_i)));
	if(var_i==0) { 
		var_r = v; 
		var_g = var_3; 
		var_b = var_1;
	}
	else if(var_i==1) {
		var_r = var_2;
		var_g = v;
		var_b = var_1;
	}
	else if(var_i==2) {
		var_r = var_1;
		var_g = v;
		var_b = var_3
	}
	else if(var_i==3){
		var_r = var_1;
		var_g = var_2;
		var_b = v;
	}
	else if(var_i==4){
		var_r = var_3;
		var_g = var_1;
		var_b = v;
	}
	else{
		var_r = v;
		var_g = var_1;
		var_b = var_2
	}
	return "rgb("+Math.round(var_r*255)+","+Math.round(var_g*255)+","+Math.round(var_b*255)+")";
};

/**
 * Getter for an arbitrary number of colors
 * if <num> is larger than the configuration color array, randomly generated saturated colors are added
 */
TRAVizConfig.prototype.getColors = function(num){
	var colors = [];
	for( var i=0; i<num; i++ ){
		if( i >= this.options.colors.length ){
			colors.push(this.Hsv2rgb(((Math.random()*360)+1)/360,1,(25 + (Math.random()*50)+1)/100));
		}
		else {
			colors.push(this.options.colors[i]);
		}
	}
	return colors;
};
