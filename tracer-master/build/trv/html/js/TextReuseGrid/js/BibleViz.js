		var filterDocs = function(docs,distances){
			var filteredDocs = [];
			var ids1 = [];
			var ids2 = [];
			for( var i=0; i<docs.length; i++ ){
				if( typeof ids1[docs[i].i] == 'undefined' ){
					ids1[docs[i].i] = 0;
				}
				ids1[docs[i].i]++;
				if( typeof ids2[docs[i].j] == 'undefined' ){
					ids2[docs[i].j] = 0;
				}
				ids2[docs[i].j]++;
			}
			for( var i=0; i<docs.length; i++ ){
				var push = true;
				if( !(ids1[docs[i].i] < 2 && ids2[docs[i].j] < 2) ){
					push = false;
				}
				if( distances && !docs[i].distances[5] ){
					push = false;
				}
				if( push ){
					filteredDocs.push(docs[i]);
				}
			}
			return filteredDocs;
		}

		var getDuplicateProportion = function(docs){
			var filteredDocs = [];
			var ids1 = [];
			var ids2 = [];
			for( var i=0; i<docs.length; i++ ){
				if( typeof ids1[docs[i].i] == 'undefined' ){
					ids1[docs[i].i] = 0;
				}
				ids1[docs[i].i]++;
				if( typeof ids2[docs[i].j] == 'undefined' ){
					ids2[docs[i].j] = 0;
				}
				ids2[docs[i].j]++;
			}
			for( var i=0; i<docs.length; i++ ){
				var push = true;
				if( ids1[docs[i].i] < 2 && ids2[docs[i].j] < 2 ){
					push = false;
				}
				if( push ){
					filteredDocs.push(docs[i]);
				}
			}
			return filteredDocs.length/docs.length;
		}

		var setDistances = function(reuses){
			for( var i=0; i<reuses.length; i++ ){
				reuses[i].distances = [];
				for( var j=0; j<9; j++ ){
					reuses[i].distances.push(false);
				}
				reuses[i].distances.push(true);
			}
			for( var i=0; i<reuses.length-1; i++ ){
				for( var j=i+1; j<reuses.length; j++ ){
					var dist1 = Math.abs(reuses[i].i-reuses[j].i);
					var dist2 = Math.abs(reuses[i].j-reuses[j].j);
					var dist = Math.round((dist1+dist2)/2);
					if( dist < 9 ){
						for( var k=dist-1; k<9; k++ ){
							reuses[i].distances[k] = true;
							reuses[j].distances[k] = true;
						}
					}
				}
			}
		}

		var getGroups = function(reuses){
			var groups = [];
			for( var i=0; i<reuses.length-1; i++ ){
				if( typeof reuses[i].group == 'undefined' ){
					var group = [];
					group.push(reuses[i]);
					reuses[i].group = group;
					groups.push(group);
				}
				for( var j=i+1; j<reuses.length; j++ ){
					var dist1 = Math.abs(reuses[i].i-reuses[j].i);
					var dist2 = Math.abs(reuses[i].j-reuses[j].j);
					var dist = Math.round((dist1+dist2)/2);
					if( typeof reuses[j].group == 'undefined' && dist <= 5 ){
						reuses[i].group.push(reuses[j]);
						reuses[j].group = reuses[i].group;
					}
				}
			}
			return groups;
		}

var ls = 0;

var ReuseViz = new function(){

	var iid = 0;
	this.getIndependentIndex = function(){
		return ++iid;
	}

	var vid = 0;
	this.getVertexIndex = function(){
		return ++vid;
	}

	this.drawOverlay = function(){
		this.overlay = $("<div class='overlay'/>").appendTo('body');
		$(this.overlay).css('width',($(document).width()-1)+'px');
		$(this.overlay).css('height',($(document).height()-1)+'px');
	}

	this.initialize = function(){
		this.container = $('<div class="container"/>').appendTo($('#container')[0]);
		this.options = $('<div class="options"/>').appendTo(this.container);
		this.reuse = $("<div class='reuseDiv'></div>").appendTo(this.container);
		this.reuseViz = $("<div id='reuse' style='display:block;'><img src='images/preview.png'/></div>").appendTo(this.reuse);
		ReuseViz.visualizeBooksHub();
	}

	this.visualizeBooksHub = function(){
		ReuseViz.drawOverlay();
		setTimeout(function(){
			ReuseViz.visualizeBooks();
			$(ReuseViz.overlay).remove();
		},10);		
	}

	var getEdition = function(id){
		for( var i=0; i<texts.length; i++ ){
			if( parseInt(texts[i].min) <= parseInt(id) && parseInt(id) <= parseInt(texts[i].max) ){
				return texts[i].title;
			}
		}		
	}

	this.visualizeBooks = function(){

		ReuseViz.chapterReuses = undefined;

		var typeRepetetive = false;

		var setTooltip = function(node,edition1,edition2,reuses,rcc,di){
			$(node).qtip({
				content: {	
					text: "<span><strong>"+edition1.title+" / "+edition2.title+"</strong></span>"+
						"<br><span>"+reuses+" Text Reuses</span><br><span class='stri'>Systematic Text Reuse Index: "+parseInt(rcc*100)+"%</span><span class='rtri'>Repetetive Text Reuse Index: "+parseInt(di*100)+"%</span>"
				},
				style: {
					background: '#fff',
					color: '#000',
					border: { width: 2, radius: 2, color: '#777' }
				},
				position: {
					corner: {
						tooltip: "bottomLeft"
					}
				},
				show: {
					delay: 0
				},
				api: {
					onShow: function(){
						if( typeRepetetive ){
							$(".stri").css('display','none');
							$(".rtri").css('display','inline-block');
						}
						else {
							$(".rtri").css('display','none');
							$(".stri").css('display','inline-block');
						}
					}
				}
			});
			$(node).click(function(){
				ReuseViz.edition1 = edition1;
				ReuseViz.edition2 = edition2;
				window.location = '../../js/TextReuseBrowser/index.html?params='+edition1.title+'_'+edition2.title;
			});
		}

		var sal = this;
		$(this.reuseViz).empty();
		$(this.parallelView).empty();
		$(this.options).empty();

		this.drawSpace(700,700,"","","","","","");

		var correlationCoefficient = function(docs){
			if( docs.length == 0 ){
				return 0;
			}
			var x_m = 0, y_m = 0;
			for( var i=0; i<docs.length; i++ ){
				if( docs[i].source1 == ReuseViz.edition1 ){
					x_m += docs[i].unit1;
					y_m += docs[i].unit2;
				}
				else {
					y_m += docs[i].unit1;
					x_m += docs[i].unit2;
				}
			}
			x_m /= docs.length;
			y_m /= docs.length;
			var Xs = 0, Ys = 0, XY = 0;
			for( var i=0; i<docs.length; i++ ){
				if( docs[i].source1 == ReuseViz.edition1 ){
					Xs += ( docs[i].unit1 - x_m )*( docs[i].unit1 - x_m );
					Ys += ( docs[i].unit2 - y_m )*( docs[i].unit2 - y_m );
					XY += ( docs[i].unit1 - x_m )*( docs[i].unit2 - y_m );
				}
				else {
					Xs += ( docs[i].unit2 - x_m )*( docs[i].unit2 - x_m );
					Ys += ( docs[i].unit1 - y_m )*( docs[i].unit1 - y_m );
					XY += ( docs[i].unit2 - x_m )*( docs[i].unit1 - y_m );
				}
			}
			return Math.abs( XY / Math.sqrt( Xs * Ys ) );
		}
		var rankCorrelationCoefficient = function(docs){
			if( docs.length < 5 ){
				return 0;
			}
			var sortX = function(doc1,doc2){
				if( doc1.i < doc2.i ){
					return -1;
				}
				return 1;
			}
			var sortY = function(doc1,doc2){
				if( doc1.j < doc2.j ){
					return -1;
				}
				return 1;
			}
			var xSorted = docs.sort(sortX);
			var lastXUnit = -1, xStart = -1, xRank = 0;
			for( var i=0; i<xSorted.length; i++ ){
				if( lastXUnit != xSorted[i].i ){
					if( lastXUnit != -1 ){
						var r = rank / (i-xStart);
						for( var j=xStart; j<i; j++ ){
							xSorted[j].xRank = r;
						}
					}
					lastXUnit = xSorted[i].i;
					xStart = i;
					rank = i+1;
				}
				else {
					rank += i+1;
				}
				if( i+1 == xSorted.length ){
					var r = rank / ((i+1)-xStart);
					for( var j=xStart; j<i+1; j++ ){
						xSorted[j].xRank = r;
					}
				}
			}
			var ySorted = docs.sort(sortY);
			var lastYUnit = -1, yStart = -1, yRank = 0;
			for( var i=0; i<ySorted.length; i++ ){
				if( lastYUnit != ySorted[i].j ){
					if( lastYUnit != -1 ){
						var r = rank / (i-yStart);
						for( var j=yStart; j<i; j++ ){
							ySorted[j].yRank = r;
						}
					}
					lastYUnit = ySorted[i].j;
					yStart = i;
					rank = i+1;
				}
				else {
					rank += i+1;
				}
				if( i+1 == ySorted.length ){
					var r = rank / ((i+1)-yStart);
					for( var j=yStart; j<i+1; j++ ){
						ySorted[j].yRank = r;
					}
				}
			}
			var diff = 0;
			for( var i=0; i<docs.length; i++ ){
				diff += (docs[i].xRank-docs[i].yRank)*(docs[i].xRank-docs[i].yRank);
			}
			return Math.abs( 1 - 6*diff/(docs.length*(docs.length*docs.length-1)) );
		}

		var x = 50, y = 650;
		var reuseMapping = [];
		for( var i=0; i<texts.length; i++ ){
			reuseMapping[texts[i].title] = [];
			for( var j=0; j<texts.length; j++ ){		
				reuseMapping[texts[i].title][texts[j].title] = [];
			}
		}
		for( var i=0; i<reusesData.length; i++ ){
			reuseMapping[getEdition(reusesData[i].i)][getEdition(reusesData[i].j)].push(reusesData[i]);
		}
		this.textReuses = [];
		this.allTR = [];
		for( var i=0; i<texts.length; i++ ){
			this.textReuses.push([]);
			for( var j=0; j<texts.length; j++ ){
				this.textReuses[i].push([]);
				this.textReuses[i][j] = false;
			}
		}
		for( var i=0; i<texts.length; i++ ){
			var text1 = texts[i].title;
			for( var j=0; j<texts.length; j++ ){
				var text2 = texts[j].title;
				if( reuseMapping[text1][text2].length > 0 ){
					this.textReuses[i][j] = { docs: reuseMapping[text1][text2], numFound: reuseMapping[text1][text2].length };
					setDistances(this.textReuses[i][j].docs);						
					var filter = filterDocs(this.textReuses[i][j].docs,true);
					var groups = getGroups(filter);
					var rcc = 0;
					for( var k=0; k<groups.length; k++ ){
						rcc += rankCorrelationCoefficient(groups[k])*groups[k].length/filter.length;
					}
					this.textReuses[i][j].duplicateIndex = getDuplicateProportion(this.textReuses[i][j].docs);
					this.textReuses[i][j].rcc = rcc;
					this.allTR.push(this.textReuses[i][j]);
				}
			}
		}

		var rccClasses = JenksCaspall(4,this.allTR,"rcc","rccClass");
		var reuseClasses = JenksCaspall(4,this.allTR,"numFound","reuseClass");
		var repetitionClasses = JenksCaspall(4,this.allTR,"duplicateIndex","repetitionClass");

		var hue = [];
		for( var i=0; i<rccClasses.length; i++ ){
			hue.push(240+Math.round(i*120/(rccClasses.length-1)));
		}
		var hue2 = [];
		for( var i=0; i<repetitionClasses.length; i++ ){
			hue2.push(240+Math.round(i*120/(repetitionClasses.length-1)));
		}
		var saturation = [];
		for( var i=0; i<reuseClasses.length; i++ ){
			saturation.push(Math.round((i+1)/(reuseClasses.length)*100));
		}

		var cells = [];

		var value = 100;
		for( var i=0; i<texts.length; i++ ){
			var height = ( parseInt(texts[i].max) - parseInt(texts[i].min) ) / parseInt(texts[texts.length-1].max) * 600;
			for( var j=0; j<texts.length; j++ ){
				var width = ( parseInt(texts[j].max) - parseInt(texts[j].min) ) / parseInt(texts[texts.length-1].max) * 600;
				if( !this.textReuses[i][j] ){
					x += width;
					continue;
				}				
				var h = hue[this.textReuses[i][j].rccClass];
				var h2 = hue2[this.textReuses[i][j].repetitionClass];
				var s = saturation[this.textReuses[i][j].reuseClass];
				var v = value;
				if( s > 0 ){
					var color = hsv2rgb(h/360,s/100,v/100);
					var color2 = hsv2rgb(h2/360,s/100,v/100);
					var unit = this.paper.rect(x, y-height, width, height, 0).attr({fill: color, stroke: "none","cursor":"pointer"});
					setTooltip(unit.node,texts[i],texts[j],this.textReuses[i][j].numFound,this.textReuses[i][j].rcc,this.textReuses[i][j].duplicateIndex);
					cells.push({
						unit: unit,
						rccColor: color,
						repetitionColor: color2,
						squareWidth: 600/texts.length,
						squareHeight: 600/texts.length,
						height: height,
						width: width,
						x1: x,
						y1: y-height,
						x2: j*600/texts.length+50,
						y2: 650-(i+1)*600/texts.length
					});
					texts[i].x = (x+j*600/texts.length+50)/2;
					texts[i].y = (x+j*600/texts.length+50)/2;
				}
				x += width;
			}
			y -= height;
			x = 50;
		}

	var legendItems = [];
	var drawLegend = function(classes,hue,value,type){
	
		for( var i=0; i<legendItems.length; i++ ){
			$(legendItems[i].node).remove();
		}
		legendItems = [];

		var centerX = 780 + ls, centerY = 350;
		var x = centerX - hue.length/2 * 30;

		for( var i=0; i<hue.length; i++ ){
			var y = centerY - saturation.length/2 * 30;;
			for( var j=saturation.length; j>0; j-- ){
				var color = hsv2rgb(hue[i]/360,saturation[j-1]/100,100/100);
				legendItems.push(ReuseViz.paper.rect(x, y, 30, 30, 0).attr({fill: color, stroke: "none"}));
				if( i==0 ){
					var min = reuseClasses[j-1][0].numFound;
					var max = reuseClasses[j-1][reuseClasses[j-1].length-1].numFound;
					var txt = max+"-\n"+min;
					if( max == min ){
						txt = max;
					}
					legendItems.push(ReuseViz.paper.text(centerX + hue.length/2 * 30 + 15, y + 15, txt).attr({font: "10px Droid Sans"}).attr({color:"#3E576F",fill:"#3E576F",textAnchor:"middle"}));
				}
				y += 30;
			}
			var min = Math.round(classes[i][0][value]*100);
			var max = Math.round(classes[i][classes[i].length-1][value]*100);
			var txt = min+"-\n"+max;
			if( max == min ){
				txt = max;
			}
			legendItems.push(ReuseViz.paper.text(x+15, centerY - saturation.length/2 * 30 - 15, txt).attr({font: "10px Droid Sans"}).attr({color:"#3E576F",fill:"#3E576F",textAnchor:"middle"}));
			x += 30;
		}
		var label1 = ReuseViz.paper.text(centerX - hue.length/2 * 30 - 20, centerY, "#Text Re-uses").attr({font: "12px Droid Sans"}).attr({color:"#3E576F",fill:"#3E576F",textAnchor:"middle"});
		label1.rotate(270);
		var label2 = ReuseViz.paper.text(centerX, centerY + saturation.length/2 * 30 + 20, type).attr({font: "12px Droid Sans"}).attr({color:"#3E576F",fill:"#3E576F",textAnchor:"middle"});
		legendItems.push(label1);
		legendItems.push(label2);

		legendItems.push(ReuseViz.paper.path("M "+(centerX - hue.length/2 * 30 - 10)+" "+(centerY - saturation.length/2 * 30)+" L "+(centerX - hue.length/2 * 30 - 10)+" "+(centerY + saturation.length/2 * 30)).attr({stroke: "#3E576F", "stroke-width": 1}));
		legendItems.push(ReuseViz.paper.path("M "+(centerX - hue.length/2 * 30)+" "+(centerY + saturation.length/2 * 30 + 10)+" L "+(centerX + hue.length/2 * 30)+" "+(centerY + saturation.length/2 * 30 + 10)).attr({stroke: "#3E576F", "stroke-width": 1}));

		legendItems.push(ReuseViz.paper.path("M "+(centerX + hue.length/2 * 30)+" "+(centerY + saturation.length/2 * 30 + 10)+" L  "+(centerX + hue.length/2 * 30 - 10)+" "+(centerY + saturation.length/2 * 30 + 14)+" L "+(centerX + hue.length/2 * 30 - 10)+" "+(centerY + saturation.length/2 * 30 + 6)+" L "+(centerX + hue.length/2 * 30)+" "+(centerY + saturation.length/2 * 30 + 10)).attr({stroke: "#3E576F", "stroke-width": 0, fill: "#3E576F"}));

		legendItems.push(ReuseViz.paper.path("M "+(centerX - hue.length/2 * 30 - 10)+" "+(centerY - saturation.length/2 * 30)+" L "+(centerX - hue.length/2 * 30 - 14)+" "+(centerY - saturation.length/2 * 30 + 10)+" L "+(centerX - hue.length/2 * 30 - 6)+" "+(centerY - saturation.length/2 * 30 + 10)+" L "+(centerX - hue.length/2 * 30 - 10)+" "+(centerY - saturation.length/2 * 30)).attr({stroke: "#3E576F", "stroke-width": 0, fill: "#3E576F"}));

}
drawLegend(rccClasses,hue,"rcc","Systematic Text Re-use (in %)");

		var showSquares = false;

		var switchPerspective = function(){
			for( var i=0; i<cells.length; i++ ){
				if( showSquares ){
					var anim = Raphael.animation({x: cells[i].x2, y: cells[i].y2, width: cells[i].squareWidth, height: cells[i].squareHeight },1000);
					cells[i].unit.animate(anim);
				}
				else {
					var anim = Raphael.animation({x: cells[i].x1, y: cells[i].y1, width: cells[i].width, height: cells[i].height },1000);
					cells[i].unit.animate(anim);
				}
			}
		}

		var opts1 = $("<div/>").appendTo(this.options);
		$(opts1).css("display","inline-block");
		$(opts1).css("text-align","left");

/*
		var gridLabel = $('<span style="vertical-align: middle;margin-right:10px;font-size:14px;">Grid Type</span>').appendTo(opts1);
		var irreg = $("<img style='cursor:pointer;vertical-align: middle;' title='Cells by Text Lengths' src='images/irreg-grid.png'/>").appendTo(opts1);
		var reg = $("<img style='cursor:pointer;vertical-align: middle;' title='Cells as Squares' src='images/reg-grid2.png'/>").appendTo(opts1);
		$(reg).click(function(){
			if( !showSquares ){
				$(irreg).attr({ 'src': 'images/irreg-grid2.png' });
				$(reg).attr({ 'src': 'images/reg-grid.png' });
				showSquares = !showSquares;
				switchPerspective();
			}
		});
		$(irreg).click(function(){
			if( showSquares ){
				$(irreg).attr({ 'src': 'images/irreg-grid.png' });
				$(reg).attr({ 'src': 'images/reg-grid2.png' });
				showSquares = !showSquares;
				switchPerspective();
			}
		});
*/
/*
		var sizes = $("<input type='radio' name='display' id='sizeRadio' class='squares' checked=checked/>").appendTo(opts1);
		$("<label for='sizeRadio' class='duplicatesLabel'>cells by text lengths</label>").appendTo(opts1);
		$("<br>").appendTo(opts1);
		var squares = $("<input type='radio' name='display' id='squaresRadio' class='squares'/>").appendTo(opts1);
		$("<label for='squaresRadio' class='duplicatesLabel'>cells as squares</label>").appendTo(opts1);
		$(squares).click(function(){
			if( !showSquares ){
				showSquares = !showSquares;
				switchPerspective();
			}
		});
		$(sizes).click(function(){
			if( showSquares ){
				showSquares = !showSquares;
				switchPerspective();
			}
		});
*/

		var switchType = function(){
			for( var i=0; i<cells.length; i++ ){
				if( typeRepetetive ){
					var anim = Raphael.animation({fill: cells[i].repetitionColor},1000);
					cells[i].unit.animate(anim);
				}
				else {
					var anim = Raphael.animation({fill: cells[i].rccColor},1000);
					cells[i].unit.animate(anim);
				}
			}
			if( typeRepetetive ){
				drawLegend(repetitionClasses,hue2,"duplicateIndex","Repetetive Text Re-use (in %)");
			}
			else {
				drawLegend(rccClasses,hue,"rcc","Systematic Text Re-use (in %)");
			}
		}

		var opts2 = $("<div/>").appendTo(this.options);
		$(opts2).css("display","inline-block");
		$(opts2).css("margin-left","30px");
		$(opts2).css("text-align","left");

		var systematicButton = $("<img style='cursor:pointer;vertical-align: middle;' title='Highlight Systematic Text Reuse' src='images/systematic.png'/>").appendTo(opts2);
		var repetetiveButton = $("<img style='cursor:pointer;vertical-align: middle;' title='Highlight Repetitive Text Reuse' src='images/repetetive2.png'/>").appendTo(opts2);
		var typeLabel = $('<span style="vertical-align: middle;margin-left:10px;font-size:14px;">Reuse Highlight</span>').appendTo(opts2);
		$(repetetiveButton).click(function(){
			if( !typeRepetetive ){
				$(systematicButton).attr({ 'src': 'images/systematic2.png' });
				$(repetetiveButton).attr({ 'src': 'images/repetetive.png' });
				typeRepetetive = !typeRepetetive;
				switchType();
			}
		});
		$(systematicButton).click(function(){
			if( typeRepetetive ){
				$(systematicButton).attr({ 'src': 'images/systematic.png' });
				$(repetetiveButton).attr({ 'src': 'images/repetetive2.png' });
				typeRepetetive = !typeRepetetive;
				switchType();
			}
		});
	}

	this.drawSpace = function(width,height,min1,max1,min2,max2,label1,label2){
		this.paper = Raphael("reuse",width+200,height);
		var layer = this.paper.rect(0, 0, width+200, height, 5).attr({fill: "#EEE", stroke: "none"});
		var space = this.paper.rect(50, 50, width-100, height-100, 0).attr({fill: "#FFF", stroke: "none"});
		var label = this.paper.text(25, height/2, label1).attr({font: "16px Droid Sans"}).attr({color:"#3E576F",fill:"#3E576F",textAnchor:"middle"});
		$(label.node).bind("selectstart", function(){
			return false;
		});
		label.rotate(270);
		this.paper.text(width/2, height-25, label2).attr({font: "16px Droid Sans"}).attr({color:"#3E576F",fill:"#3E576F",textAnchor:"middle"});
		this.paper.path("M 39 49 L "+(width-49)+" 49").attr({stroke: "#3E576F", "stroke-width": 1});
		this.paper.path("M 39 "+(height-49)+" L "+(width-49)+" "+(height-49)).attr({stroke: "#3E576F", "stroke-width": 1, "stroke-linecap": "round"});
		this.paper.path("M 49 "+(height-39)+" L 49 49").attr({stroke: "#3E576F", "stroke-width": 1, "stroke-linecap": "round"});
		this.paper.path("M "+(width-49)+" "+(height-39)+" L "+(width-49)+" 49").attr({stroke: "#3E576F", "stroke-width": 1, "stroke-linecap": "round"});
		var l1 = this.paper.text(42, height-53, min1).attr({font: "10px Droid Sans"}).attr({color:"#3E576F",fill:"#3E576F",'text-anchor':"middle"});
		var l2 = this.paper.text(42, 53, max1).attr({font: "10px Droid Sans"}).attr({color:"#3E576F",fill:"#3E576F",'text-anchor':"middle"});
		l1.rotate(270);
		l2.rotate(270);
		l1.attr({
			'text-anchor':"start"
		});
		l2.attr({
			'text-anchor':"end"
		});
		this.paper.text(53, height-40, min2).attr({font: "10px Droid Sans"}).attr({color:"#3E576F",fill:"#3E576F",'text-anchor':"start"});
		this.paper.text(width-53, height-40, max2).attr({font: "10px Droid Sans"}).attr({color:"#3E576F",fill:"#3E576F",'text-anchor':"end"});
		
		var helperDiv = $("<span>").appendTo("body");
		$(helperDiv).css("font","10px Droid Sans");
		var num = texts[texts.length-1].max;
		for( var i=0; i<texts.length; i++ ){
			var x = ( texts[i].min + ( texts[i].max - texts[i].min ) / 2 ) / num * ( width - 100 ) + 50;
			var y = height - 50 - ( texts[i].min + ( texts[i].max - texts[i].min ) / 2 ) / num * ( height - 100 );
			this.paper.text( width-40, y, texts[i].title).attr({font: "10px Droid Sans"}).attr({color:"#3E576F",fill:"#3E576F",'text-anchor':"start"});
			var lx = this.paper.text(x, height-40, texts[i].title).attr({font: "10px Droid Sans"}).attr({color:"#3E576F",fill:"#3E576F",'text-anchor':"middle"});
			lx.rotate(270);
			lx.attr({'text-anchor':"end"});
			$(helperDiv).html(texts[i].title);
			if( ls < $(helperDiv).width() ){
				ls = $(helperDiv).width();
			}
		}
		$(helperDiv).remove();

		this.paper.setSize((width+200+ls)+"px",(height+ls)+"px");
		layer.attr({ "width": width+200+ls });
		layer.attr({ "height": height+ls });

		return space;
	}

};
