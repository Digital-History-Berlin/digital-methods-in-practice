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

		var markDuplicates = function(docs){
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
				if( ids1[docs[i].i] < 2 && ids2[docs[i].j] < 2 ){
					docs[i].duplicate = false;
				}
				else {
					docs[i].duplicate = true;
				}
			}
		}

var ReuseViz = new function(){

	var unselectCss = {
		"-webkit-touch-callout": "none",
		"-webkit-user-select": "none",
		"-khtml-user-select": "none",
		"-moz-user-select": "none",
		"-ms-user-select": "none",
		"user-select": "none",
	};

	var iid = 0;
	this.getIndependentIndex = function(){
		return ++iid;
	}

	var vid = 0;
	this.getVertexIndex = function(){
		return ++vid;
	}

	var history = [];

	this.drawOverlay = function(){
		this.overlay = $("<div class='overlay'/>").appendTo('body');
		$(this.overlay).css('width',($(document).width()-1)+'px');
		$(this.overlay).css('height',($(document).height()-1)+'px');
	}

	this.initialize = function(){

		this.container = $('<div class="container"/>').appendTo($('#container')[0]);
		var editionSelect = $("<table class='biblePos'></table>").appendTo(this.container);

		this.options = $('<div class="options"/>').appendTo(this.container);
		this.reuse = $("<div class='reuseDiv'></div>").appendTo(this.container);
		this.reuseViz = $("<div id='reuse' style='display:block;'><img src='images/preview.png'/></div>").appendTo(this.reuse);

		this.parallelView = $('<div class="parallelView"/>').appendTo(this.container);

		this.goBack = $("<div class='goBack'></div>").appendTo(this.reuse);
		$(this.goBack).click(function(){
			history.pop();
			if( history.length > 0 ){
				var h = history[history.length-1];
				ReuseViz.visualizeReusesHub(h.min1,h.max1,h.min2,h.max2);
			}
			else {
				$(ReuseViz.goBack).css('display','none');
			}
		});

		var titles = $("<tr></tr>").appendTo(editionSelect);
		$("<td>Text 1</td>").appendTo(titles);
		$("<td>Text 2</td>").appendTo(titles);
		$("<td></td>").appendTo(titles);

		var dropdowns = $("<tr></tr>").appendTo(editionSelect);
		var cell1 = $('<td/>').appendTo(dropdowns);
		var cell2 = $('<td/>').appendTo(dropdowns);
		var cell3 = $('<td/>').appendTo(dropdowns);

		this.selectEdition1 = $('<select></select>').appendTo(cell1);
		for( var i=0; i<texts.length; i++ ){
			$("<option>"+texts[i].title+"</option>").appendTo(this.selectEdition1);
		}
		this.selectEdition2 = $(this.selectEdition1).clone().appendTo(cell2);
		this.setTexts = $('<input type="button" value="Set Texts!">').appendTo(cell3);

		var showReuses = function(){
			$('option:selected',ReuseViz.selectEdition1).each(function(reuse,index){
				ReuseViz.edition1 = $(this).val();
			});
			$('option:selected',ReuseViz.selectEdition2).each(function(reuse,ind){
				ReuseViz.edition2 = $(this).val();
			});
			for( var i=0; i<texts.length; i++ ){
				if( ReuseViz.edition1 == texts[i].title ){
					ReuseViz.edition1 = texts[i];
				}
				if( ReuseViz.edition2 == texts[i].title ){
					ReuseViz.edition2 = texts[i];
				}
			}
			if( ReuseViz.edition1.min > ReuseViz.edition2.min ){
				var tmp = ReuseViz.edition2;
				ReuseViz.edition2 = ReuseViz.edition1;
				ReuseViz.edition1 = tmp;
			}
			history = [{
				min1: ReuseViz.edition1.min,
				max1: ReuseViz.edition1.max,
				min2: ReuseViz.edition2.min,
				max2: ReuseViz.edition2.max
			}];
			ReuseViz.visualizeReusesHub(ReuseViz.edition1.min,ReuseViz.edition1.max,ReuseViz.edition2.min,ReuseViz.edition2.max);
		}

		$(this.setTexts).click(function(){
			showReuses();
		});

		if( window.location.href.indexOf('?params=') > -1 ){
			var params = decodeURI(window.location.href.substring(window.location.href.indexOf('?params=') + 8 )).split('_');
			for( var i=0; i<texts.length; i++ ){
				if( params[0] == texts[i].title ){
					ReuseViz.edition1 = texts[i];
				}
				if( params[1] == texts[i].title ){
					ReuseViz.edition2 = texts[i];
				}
			}
			history = [{
				min1: ReuseViz.edition1.min,
				max1: ReuseViz.edition1.max,
				min2: ReuseViz.edition2.min,
				max2: ReuseViz.edition2.max
			}];
			$(ReuseViz.selectEdition1).find("option").filter(function(index) {
			    return params[0] === $(this).text();
			}).attr("selected", "selected");
			$(ReuseViz.selectEdition2).find("option").filter(function(index) {
			    return params[1] === $(this).text();
			}).attr("selected", "selected");
			ReuseViz.visualizeReusesHub(ReuseViz.edition1.min,ReuseViz.edition1.max,ReuseViz.edition2.min,ReuseViz.edition2.max);
		}

	}

	this.drawSpace = function(width,height,min1,max1,min2,max2,label1,label2){
		this.paper = Raphael("reuse",width+200,height);
		this.paper.rect(0, 0, width+200, height, 5).attr({fill: "#EEE", stroke: "none"});
		var space = this.paper.rect(50, 50, width-100, height-100, 0).attr({fill: "#FFF", stroke: "none"});
		var label = this.paper.text(25, height/2, label1).attr({font: "16px Droid Sans"}).attr({color:"#3E576F",fill:"#3E576F",textAnchor:"middle"});
		$(label.node).bind("selectstart", function(){
			return false;
		});
		label.rotate(270);
		$(label.node).css(unselectCss);

		var label2 = this.paper.text(width/2, height-25, label2).attr({font: "16px Droid Sans"}).attr({color:"#3E576F",fill:"#3E576F",textAnchor:"middle"});
		$(label2.node).css(unselectCss);
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
		$(l1.node).css(unselectCss);
		$(l2.node).css(unselectCss);
		var l3 = this.paper.text(53, height-40, min2).attr({font: "10px Droid Sans"}).attr({color:"#3E576F",fill:"#3E576F",'text-anchor':"start"});
		var l4 = this.paper.text(width-53, height-40, max2).attr({font: "10px Droid Sans"}).attr({color:"#3E576F",fill:"#3E576F",'text-anchor':"end"});
		$(l3.node).css(unselectCss);
		$(l4.node).css(unselectCss);
		return space;
	}

	this.visualizeReusesHub = function(min1,max1,min2,max2){
		ReuseViz.drawOverlay();
		setTimeout(function(){
			ReuseViz.visualizeReuses(min1,max1,min2,max2);
			$(ReuseViz.overlay).remove();
		},10);		
	}

	this.visualizeReuses = function(min1,max1,min2,max2){
		if( history.length > 1 ){
			$(ReuseViz.goBack).css('display','block');
		}
		else {
			$(ReuseViz.goBack).css('display','none');
		}
		var qtips = $('.qtip');
		for( var i=0; i<qtips.length; i++ ){
			$(qtips[i]).remove();
		}

		var w, h;
		if( (max2-min2) > (max1-min1) ){
			w = 700;
			h = 700 * (max1-min1) / (max2-min2);
			if( h < 200 ){
				w *= 200/h;
				h = 200;
			}
		}
		else {
			w = 700 * (max2-min2) / (max1-min1);
			h = 700;
			if( w < 200 ){
				h *= 200/w;
				w = 200;
			}
		}

		var radius = 4;
		var setTooltip = function(node,unit1,unit2,x){
			var iid = ReuseViz.getIndependentIndex();
			var anchor = "topMiddle";
			var olspace = $(ReuseViz.reuseViz).width()/2 - w/2;
			if( olspace + x < 350 ){
				anchor = "topLeft";
			}
			if( w - x + olspace < 350 ){
				anchor = "topRight";
			}
			$(node).qtip({
				content: {	
					text: "<span style='font-size: 12px;color:red'>"+ReuseViz.edition1.title+"<br><span>"+segments[unit1]+"</span></span><br><span style='font-size: 12px;color:blue'>"+ReuseViz.edition2.title+"<br><span>"+segments[unit2]+"</span></span><br><div style='width:100%;overflow:auto;'><div id='alignment"+iid+"'></div></div>",
					title: {
						text: "<div>Text Re-use Alignment Visualization</div>",
						button: 'X'
					}
				},
				style: {
					tip: true,
					border: { width: 0, radius: 4 },
					width: 600
				},
				position: {
					corner: {
						tooltip: anchor,
						target: "bottomMiddle"
					},
					adjust: {
						x: radius,
						y: radius
					}
				},
				show: { 
					when: 'click', 
					solo: true
				},
				hide: {
					when: { event: 'click' }
				},
				api: {
					onShow: function(){
						var traviz = new TRAViz('alignment'+iid,{
							lineBreaks: false
						});
						var data = [{
								edition: ReuseViz.edition1.title,
								text: segments[unit1]
							},{
								edition: ReuseViz.edition2.title,
								text: segments[unit2]
							}];
						traviz.align(data);
						traviz.visualize();
					}
				}
			});
		}

		$(this.reuseViz).empty();
		$(this.parallelView).empty();
		$(this.options).empty();

		var minLabel1 = min1;
		var minLabel2 = min2;
		var maxLabel1 = max1;
		var maxLabel2 = max2;
console.info(w,h,minLabel1,maxLabel1,minLabel2,maxLabel2,ReuseViz.edition1.title,ReuseViz.edition2.title);
		var space = this.drawSpace(w,h,minLabel1,maxLabel1,minLabel2,maxLabel2,ReuseViz.edition1.title,ReuseViz.edition2.title);

		var getMousePosition = function(event) {
			if (!event) {
				event = window.event;
			}
			var body = (window.document.compatMode && window.document.compatMode == "CSS1Compat") ? window.document.documentElement : window.document.body;
			return {
				top : event.pageY ? event.pageY : event.clientY,
				left : event.pageX ? event.pageX : event.clientX
			};
		};

		$(space.node).mousedown(function(evt){
			var startPos = getMousePosition(evt);
			var x1 = evt.offsetX-50;
			var y1 = evt.offsetY-50;
			var bbox;
			document.onmouseup = function(evt){
				document.onmouseup = null;
				document.onmousemove = null;				
				var endPos = getMousePosition(evt);
				var x2 = x1 + (endPos.left-startPos.left);
				var y2 = y1 + (endPos.top-startPos.top);
				x2 > w-100 ? x2 = w-100 : '';
				x2 < 0 ? x2 = 0 : '';
				y2 > h-100 ? y2 = h-100 : '';
				y2 < 0 ? y2 = 0 : '';
				var x_min, x_max, y_min, y_max;
				if( x1 < x2 ){
					x_min = Math.floor(x1 / (w-100) * (max2-min2) + min2);
					x_max = Math.floor(x2 / (w-100) * (max2-min2) + min2);
				}
				else {
					x_min = Math.floor(x2 / (w-100) * (max2-min2) + min2);
					x_max = Math.floor(x1 / (w-100) * (max2-min2) + min2);
				}
				if( y1 > y2 ){
					y_min = Math.floor((h-100-y1) / (h-100) * (max1-min1) + min1);
					y_max = Math.floor((h-100-y2) / (h-100) * (max1-min1) + min1);
				}
				else {
					y_min = Math.floor((h-100-y2) / (h-100) * (max1-min1) + min1);
					y_max = Math.floor((h-100-y1) / (h-100) * (max1-min1) + min1);
				}
				if( x_min < x_max && y_min < y_max ){
					history.push({
						min1: y_min,
						max1: y_max,
						min2: x_min,
						max2: x_max
					});
					ReuseViz.visualizeReusesHub(y_min,y_max,x_min,x_max);
				}
			}
			document.onmousemove = function(evt){
				evt.preventDefault();
				evt.stopPropagation();
				var curPos = getMousePosition(evt);
				var x2 = x1 + (curPos.left-startPos.left);
				var y2 = y1 + (curPos.top-startPos.top);
				x2 > w-100 ? x2 = w-100 : '';
				x2 < 0 ? x2 = 0 : '';
				y2 > h-100 ? y2 = h-100 : '';
				y2 < 0 ? y2 = 0 : '';
				var x_min, x_max, y_min, y_max;
				if( x1 < x2 ){
					x_min = Math.floor(x1+50);
					x_max = Math.floor(x2+50);
				}
				else {
					x_min = Math.floor(x2+50);
					x_max = Math.floor(x1+50);
				}
				if( y1 < y2 ){
					y_min = Math.floor(y1+50);
					y_max = Math.floor(y2+50);
				}
				else {
					y_min = Math.floor(y2+50);
					y_max = Math.floor(y1+50);
				}
				if( typeof bbox == 'undefined' ){
					bbox = ReuseViz.paper.rect(x_min, y_min, x_max-x_min, y_max-y_min, 0).attr({stroke: "#AAA"});
				}
				else {
					bbox.attr({
						'x': x_min,
						'y': y_min,
						'height': y_max-y_min,
						'width': x_max-x_min
					});
				}
			}
		});

		var dots = [];
		var strokes = [];

		var getEdition = function(id){
			for( var i=0; i<texts.length; i++ ){
				if( texts[i].min <= id && id <= texts[i].max ){
					return texts[i].title;
				}
			}		
		}

		var reuses = [];
		for( var i=0; i<reusesData.length; i++ ){
			var e1 = getEdition(reusesData[i].i);
			var e2 = getEdition(reusesData[i].j);
			if( e1 == ReuseViz.edition1.title && e2 == ReuseViz.edition2.title && min1 <= reusesData[i].i && reusesData[i].i <= max1 && min2 <= reusesData[i].j && reusesData[i].j <= max2 || e2 == ReuseViz.edition1.title && e1 == ReuseViz.edition2.title && min1 <= reusesData[i].j && reusesData[i].j <= max1 && min2 <= reusesData[i].i && reusesData[i].i <= max2 ){
				reuses.push(reusesData[i]);
			}
		}
		markDuplicates(reuses);

		var nor = reuses.length;
		if( nor == 0 ){
			alert("No Text Re-use passages found!");
			return;
		}

		if( typeof filter != 'undefined' ){
			reuses = filterDocs(reuses);
		}

		var reusesCopy = [];
		for( var i=0; i<reuses.length; i++ ){
			reusesCopy.push(reuses[i]);
		}

		var scoreClasses = JenksCaspall(4,reusesCopy,"s","scoreClass");

		var hue = [60];
		for( var i=1; i<scoreClasses.length; i++ ){
			hue.push(60+Math.round(i*60/(scoreClasses.length-1)));
		}
		var saturation = 100;
		var value = [100];
		for( var i=1; i<scoreClasses.length; i++ ){
			value.push(100-Math.round(i*50/(scoreClasses.length-1)));
		}

		var centerX = w+80, centerY = h/2;
		var x = centerX - hue.length/2 * 30;
		for( var i=0; i<hue.length; i++ ){
			var y = centerY - 15;
			var color = hsv2rgb(hue[i]/360,1,value[i]/100);
			this.paper.rect(x, y, 30, 30, 0).attr({fill: color, stroke: "none"});
			var min = Math.round(scoreClasses[i][0].s*100)/100;
			var max = Math.round(scoreClasses[i][scoreClasses[i].length-1].s*100)/100;
			var txt = min+"-\n"+max;
			if( max == min ){
				txt = max;
			}
			var labelI = this.paper.text(x+15, centerY - 1/2 * 30 - 15, txt).attr({font: "10px Droid Sans"}).attr({color:"#3E576F",fill:"#3E576F",textAnchor:"middle"});
			$(labelI.node).css(unselectCss);
			x += 30;
		}
		var label2 = this.paper.text(centerX, centerY + 1/2 * 30 + 20, "Score").attr({font: "12px Droid Sans"}).attr({color:"#3E576F",fill:"#3E576F",textAnchor:"middle"});
		$(label2.node).css(unselectCss);
		this.paper.path("M "+(centerX - hue.length/2 * 30)+" "+(centerY + 1/2 * 30 + 10)+" L "+(centerX + hue.length/2 * 30)+" "+(centerY + 1/2 * 30 + 10)).attr({stroke: "#3E576F", "stroke-width": 1});
		this.paper.path("M "+(centerX + hue.length/2 * 30)+" "+(centerY + 1/2 * 30 + 10)+" L  "+(centerX + hue.length/2 * 30 - 10)+" "+(centerY + 1/2 * 30 + 14)+" L "+(centerX + hue.length/2 * 30 - 10)+" "+(centerY + 1/2 * 30 + 6)+" L "+(centerX + hue.length/2 * 30)+" "+(centerY + 1/2 * 30 + 10)).attr({stroke: "#3E576F", "stroke-width": 0, fill: "#3E576F"});
		$(this.goBack).css('top',($(this.reuseViz).position().top+10)+'px');
		$(this.goBack).css('right',(($(this.reuseViz).width()-w)/2-90)+'px');

		var reuseMapping1 = [];
		var reuseMapping2 = [];
		for( var i=0; i<reuses.length; i++ ){
			reuses[i].runningId = i;
			reuses[i].distances = [];
			for( var j=0; j<9; j++ ){
				reuses[i].distances.push(false);
			}
			reuses[i].distances.push(true);
			if( typeof reuseMapping1[''+reuses[i].i] == 'undefined' ){
				reuseMapping1[''+reuses[i].i] = [];
			}
			if( typeof reuseMapping2[''+reuses[i].j] == 'undefined' ){
				reuseMapping2[''+reuses[i].j] = [];
			}
			reuseMapping1[''+reuses[i].i].push(reuses[i]);
			reuseMapping2[''+reuses[i].j].push(reuses[i]);
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

		for( var i=0; i<reuses.length; i++ ){
			var x, y;
			if( getEdition(reuses[i].i) == ReuseViz.edition1.title && reuses[i].j >= min2 && reuses[i].j <= max2 ){
				x = ( reuses[i].j - min2) / (max2-min2) * (w-100) + 50;
				y = (h-100) - ( reuses[i].i - min1) / (max1-min1) * (h-100) + 50;
			}
			else {
				x = ( reuses[i].i - min2) / (max2-min2) * (w-100) + 50;
				y = (h-100) - ( reuses[i].j - min1) / (max1-min1) * (h-100) + 50;
			}
			var color = hsv2rgb(hue[reuses[i].scoreClass]/360,1,value[reuses[i].scoreClass]/100);
			var dot = this.paper.circle(x, y, radius).attr({fill: color, stroke: "#3E576F","cursor":"pointer"});
			dots.push(dot);
			setTooltip(dot.node,reuses[i].i,reuses[i].j,x);
		}

		var height = 400, width = 40;
		var h1, h2;
		if( (max2-min2) > (max1-min1) ){
			h1 = height*(max1-min1)/(max2-min2);
			h2 = height;
		}
		else {
			h1 = height;
			h2 = height*(max2-min2)/(max1-min1);
		}

		var header1 = $("<div class='parallelHeader'>"+ReuseViz.edition1.title+"</div>").appendTo(this.parallelView);
		$(header1).css('padding-right','50px');
		$(header1).css('padding-left','200px');
		var header2 = $("<div class='parallelHeader'>"+ReuseViz.edition2.title+"</div>").appendTo(this.parallelView);
		$(header2).css('padding-left','50px');
		$("<br/>").appendTo(this.parallelView);

		var overview = $("<div id='overviewDiv' class='overviewDiv'/>").appendTo(this.parallelView);
		var first = $("<div class='parallelDiv'/>").appendTo(this.parallelView);
		var between = $("<div id='betweenDiv' class='betweenDiv'/>").appendTo(this.parallelView);
		var second = $("<div class='parallelDiv'/>").appendTo(this.parallelView);

		var overviewPaper = Raphael("overviewDiv",200,600);
		var overviewBg = overviewPaper.rect(0, 0, 200, 600, 0).attr({fill: "#FFF", stroke: "none"});
		overviewPaper.rect(40, 100 + Math.abs((Math.max(h1,h2)-h1)/2), width, h1, 0).attr({fill: "#AABCCF", stroke: "none"});
		overviewPaper.rect(120, 100 + Math.abs((Math.max(h1,h2)-h2)/2), width, h2, 0).attr({fill: "#AABCCF", stroke: "none"});		

		var leftViewport = overviewPaper.rect(35, 100 + Math.abs((Math.max(h1,h2)-h1)/2), width+10, 0, 0).attr({fill: "#DBE3EB", stroke: "none"});
		var rightViewport = overviewPaper.rect(115, 100 + Math.abs((Math.max(h1,h2)-h2)/2), width+10, 0, 0).attr({fill: "#DBE3EB", stroke: "none"});		

		for( var i=0; i<reuses.length; i++ ){
			var x11 = 40, x12 = 80;
			var x21 = 120, x22 = 160;
			var y1 = ( reuses[i].i - min1) / (max1-min1) * h1 + 100 + Math.abs((Math.max(h1,h2)-h1)/2);
			var y2 = ( reuses[i].j - min2) / (max2-min2) * h2 + 100 + Math.abs((Math.max(h1,h2)-h2)/2);
			var color = hsv2rgb(hue[reuses[i].scoreClass]/360,1,value[reuses[i].scoreClass]/100);
			var path = overviewPaper.path("M "+x11+" "+y1+" L  "+x12+" "+y1+" L "+x21+" "+y2+" L "+x22+" "+y2).attr({stroke: color, "stroke-width": 1, "cursor": "pointer"});
			strokes.push(path);
		}

		var ids1 = [];
		var ids2 = [];
		var idMapping1 = [];
		var idMapping2 = [];		

		var appendIdClick = function(id,unit){
			$(id).click(function(){
				checkVisibility(unit);
			});
		}

		var firstTable = $("<table/>").appendTo(first);
		for( var i=min1; i<max1+1; i++ ){
			var tr = $("<tr/>").appendTo(firstTable);
			var line = $("<td class='parallelVerse'>"+segments[i+""]+"</div>").appendTo(tr);
			var id = $("<td class='parallelId'>"+i+"</td>").appendTo(tr);
			appendIdClick(id,i);
			if( typeof reuseMapping1[''+i] != "undefined" ){
				var idel = {
					line: line,
					element: id,
					reuses: reuseMapping1[''+i]
				};
				ids1.push(idel);
				idMapping1[''+i] = idel;
			}
		}
		var secondTable = $("<table/>").appendTo(second);
		for( var i=min2; i<max2+1; i++ ){
			var tr = $("<tr/>").appendTo(secondTable);
			var id = $("<td class='parallelId'>"+i+"</td>").appendTo(tr);
			var line = $("<td class='parallelVerse'>"+segments[i+""]+"</div>").appendTo(tr);
			appendIdClick(id,i);
			if( typeof reuseMapping2[''+i] != "undefined" ){
				var idel = {
					line: line,
					element: id,
					reuses: reuseMapping2[''+i]
				};
				ids2.push(idel);
				idMapping2[''+i] = idel;
			}
		}

		$(first).jScrollPane({
			animateScroll: true
		});
		$(second).jScrollPane({
			animateScroll: true
		});

		var api1 = first.data('jsp');
		var api2 = second.data('jsp');

		var lastHighlight = null;
		appendClick = function(connection,reuse,dot){
			var clicked = false;
			$(connection).bind('click', function(){
				api1.scrollToY(Math.round(idMapping1[''+reuse.i].top));
				api2.scrollToY(Math.round(idMapping2[''+reuse.j].top));
				if( !clicked ){
					clicked = true;
					$(dot).trigger('click');
				}
				else {
					clicked = false;
				}
			});
			$(dot).bind('click', function(){
				if( !clicked ){
					clicked = true;
					$(connection).trigger('click');
				}
				else {
					clicked = false;
				}
			});
			$(connection).bind('mouseover', function(){
				if( lastHighlight != null ){
					$(lastHighlight.line1).html(lastHighlight.verse1);
					$(lastHighlight.line2).html(lastHighlight.verse2);
				}
				$(idMapping1[''+reuse.i].line).empty();
				$(idMapping2[''+reuse.j].line).empty();
				var traviz = new TRAViz('alignment'+iid,{
					lineBreaks: false
				});
				var data = [{
						edition: ReuseViz.edition1.title,
						text: segments[reuse.i]
					},{
						edition: ReuseViz.edition2.title,
						text: segments[reuse.j]
				}];
				traviz.align(data);
				var verse1 = segments[reuse.i].replace(/  /g, " ");
				var verse2 = segments[reuse.j].replace(/  /g, " ");
				var tokens1 = verse1.split(" ");
				var tokens2 = verse2.split(" ");
				var spans1 = [];
				for( var i=0; i<tokens1.length; i++ ){
					var span = $("<span>"+tokens1[i]+"</span>").appendTo(idMapping1[''+reuse.i].line);
					$(idMapping1[''+reuse.i].line).append(" ");
					spans1.push(span);
					if( traviz.sentencePaths[0][i].count == 2 ){
						$(span).css('background-color','#99E6FF');
					}
				}
				var spans2 = [];
				for( var i=0; i<tokens2.length; i++ ){
					var span = $("<span>"+tokens2[i]+"</span>").appendTo(idMapping2[''+reuse.j].line);
					$(idMapping2[''+reuse.j].line).append(" ");
					spans2.push(span);
					if( traviz.sentencePaths[1][i].count == 2 ){
						$(span).css('background-color','#99E6FF');
					}
				}
				lastHighlight = {
					line1: idMapping1[''+reuse.i].line,
					line2: idMapping2[''+reuse.j].line,
					verse1: segments[reuse.i],
					verse2: segments[reuse.j]
				};
			});
			$(connection).bind('mouseout', function(){
				$(idMapping1[''+reuse.i].line).html(segments[reuse.i]);
				$(idMapping2[''+reuse.j].line).html(segments[reuse.j]);
				lastHighlight = null;
			});
		}

		var selectedUnit = undefined;

		$($('.jspPane',first)[0]).css('left','16px');
		$($('.jspVerticalBar',first)[0]).css('left','0px');

		for( var i=0; i<ids1.length; i++ ){
			ids1[i].level = $(ids1[i].element).position().top + $(ids1[i].element).height()/2;
			ids1[i].top = $(ids1[i].element).position().top;
		}
		for( var i=0; i<ids2.length; i++ ){
			ids2[i].level = $(ids2[i].element).position().top + $(ids2[i].element).height()/2;
			ids2[i].top = $(ids2[i].element).position().top;
		}
		
		for( var i=0; i<strokes.length; i++ ){
			appendClick(strokes[i].node,reuses[i],dots[i].node);
		}

		var betweenPaper = Raphael("betweenDiv",100,600);
		var parallelBg = betweenPaper.rect(0, 0, 100, 600, 0).attr({fill: "#FFF", stroke: "none"});

		var appendScroll = function(element){
			$(element).bind('mousewheel', function(event, delta){
				var top1 = $($('.jspPane',first)[0]).position().top+10*delta;
				var top2 = $($('.jspPane',second)[0]).position().top+10*delta;
				var h1 = -1 * $($('.jspPane',first)[0]).height() + $(first).height();
				var h2 = -1 * $($('.jspPane',second)[0]).height() + $(second).height();
				if( h1 <= top1 && top1 <= 0 && h2 <= top2 && top2 <= 0 ){
					$($('.jspPane',first)[0]).css('top',($($('.jspPane',first)[0]).position().top+10*delta)+'px');
					$($('.jspPane',second)[0]).css('top',($($('.jspPane',second)[0]).position().top+10*delta)+'px');
				}
				drawConnections();
				event.preventDefault();
			});		
		}
		appendScroll(parallelBg.node);

		var showDuplicates = true;
		var distance = 9;
		var connections = [];
		var drawConnections = function(verse){
			var mult1 = h1/$($('.jspPane',first)[0]).height();
			leftViewport.attr({
				'y': 100 + Math.abs((Math.max(h1,h2)-h1)/2) - mult1*$($('.jspPane',first)[0]).position().top,
				'height': mult1*$(first).height(),
			}); 
			var mult2 = h2/$($('.jspPane',second)[0]).height();
			rightViewport.attr({
				'y': 100 + Math.abs((Math.max(h2,h1)-h2)/2) - mult2*$($('.jspPane',second)[0]).position().top,
				'height': mult2*$(second).height(),
			}); 
			for( var i=0; i<connections.length; i++ ){
				$(connections[i].node).remove();
			}
			connections = [];
			var firstTop = Math.abs($($('.jspPane',first)[0]).position().top);
			var secondTop = Math.abs($($('.jspPane',second)[0]).position().top);
			var plotted = [];
			for( var i=0; i<ids1.length; i++ ){
				if( ids1[i].level > firstTop && ids1[i].level < firstTop + $(first).height() ){
					for( var j=0; j<ids1[i].reuses.length; j++ ){
						if( !showDuplicates && ids1[i].reuses[j].duplicate ){
							continue;
						} 
						if( !ids1[i].reuses[j].distances[distance] ){
							continue;
						} 
						if( typeof verse != "undefined" && ids1[i].reuses[j].i != verse && ids1[i].reuses[j].j != verse ){
							continue;
						} 
						var x1 = 5;
						var y1 = ids1[i].level - firstTop;
						var x2 = 95;
						var y2 = idMapping2[''+ids1[i].reuses[j].j].level - secondTop;
						var color = hsv2rgb(hue[ids1[i].reuses[j].scoreClass]/360,1,value[ids1[i].reuses[j].scoreClass]/100);
						var connection = betweenPaper.path("M "+x1+" "+y1+" L  "+x2+" "+y2).attr({stroke: color, "stroke-width": 2, "stroke-linecap": "round","cursor":"pointer"});
						appendScroll(connection.node);
						connections.push(connection);
						appendClick(connection.node,ids1[i].reuses[j],dots[ids1[i].reuses[j].runningId].node);
						plotted[ids1[i].reuses[j].i+''+ids1[i].reuses[j].i] = true;
					}
				}
			}
			for( var i=0; i<ids2.length; i++ ){
				if( ids2[i].level > secondTop && ids2[i].level < secondTop + $(second).height() ){
					for( var j=0; j<ids2[i].reuses.length; j++ ){
						if( !showDuplicates && ids2[i].reuses[j].duplicate ){
							continue;
						} 
						if( !ids2[i].reuses[j].distances[distance] ){
							continue;
						} 
						if( typeof verse != "undefined" && ids2[i].reuses[j].i != verse && ids2[i].reuses[j].j != verse ){
							continue;
						} 
						if( typeof plotted[ids2[i].reuses[j].i+''+ids2[i].reuses[j].i] != 'undefined' ){
							continue;
						}
						var x1 = 95;
						var y1 = ids2[i].level - secondTop;
						var x2 = 5;
						var y2 = idMapping1[''+ids2[i].reuses[j].i].level - firstTop;
						var color = hsv2rgb(hue[ids2[i].reuses[j].scoreClass]/360,1,value[ids2[i].reuses[j].scoreClass]/100);
						var connection = betweenPaper.path("M "+x1+" "+y1+" L  "+x2+" "+y2).attr({stroke: color, "stroke-width": 2, "stroke-linecap": "round","cursor":"pointer"});
						appendScroll(connection.node);
						connections.push(connection);
						appendClick(connection.node,ids2[i].reuses[j],dots[ids2[i].reuses[j].runningId].node);
					}
				}
			}
		}

		drawConnections();

		var colorLines = function(){
			for( var i=0; i<ids1.length; i++ ){
				var color = "#3e576f";
				var score = 0;
				for( var j=0; j<ids1[i].reuses.length; j++ ){
					if( !showDuplicates && ids1[i].reuses[j].duplicate ){
						continue;
					} 
					if( !ids1[i].reuses[j].distances[distance] ){
						continue;
					}
					if( ids1[i].reuses[j].s > score ){
						score = ids1[i].reuses[j].s;
						color = hsv2rgb(hue[ids1[i].reuses[j].scoreClass]/360,1,value[ids1[i].reuses[j].scoreClass]/100);
					}
				}
			}
			for( var i=0; i<ids2.length; i++ ){
				var color = "#3e576f";
				var score = 0;
				for( var j=0; j<ids2[i].reuses.length; j++ ){
					if( !showDuplicates && ids2[i].reuses[j].duplicate ){
						continue;
					} 
					if( !ids2[i].reuses[j].distances[distance] ){
						continue;
					}
					if( ids2[i].reuses[j].s > score ){
						score = ids2[i].reuses[j].s;
						color = hsv2rgb(hue[ids2[i].reuses[j].scoreClass]/360,1,value[ids2[i].reuses[j].scoreClass]/100);
					}
				}
			}
		}

		colorLines();

		$(first).scroll(function(){
			drawConnections();
		});
		$(second).scroll(function(){
			drawConnections();
		});
		
		var nor_new = nor;

		var numberOfReuses = overviewPaper.text(100,50, nor_new+" Text Re-uses").attr({font: "16px Droid Sans"}).attr({color:"#3E576F",fill:"#3E576F",textAnchor:"middle"});

		var checkVisibility = function(verse){
			nor_new = nor;
			for( var i=0; i<reuses.length; i++ ){
				var show = true;
				if( typeof verse != "undefined" && reuses[i].i != verse && reuses[i].j != verse ){
					show = false;
				}
				if( reuses[i].duplicate && !showDuplicates ){
					show = false;
				}
				if( !reuses[i].distances[distance] ){
					show = false;
				}
				if( show ){
					dots[i].show();
					strokes[i].show();
				}
				else {
					nor_new--;
					dots[i].hide();
					strokes[i].hide();
				}
			}
			drawConnections(verse);
			colorLines();
			numberOfReuses.attr({
				'text': nor_new+" Text Re-uses"
			});
		}

		$("<label for='duplicatesCheckbox' class='duplicatesLabel'>duplicates</label>").appendTo(this.options);
		var duplicates = $("<input type='checkbox' id='duplicatesCheckbox' checked='checked' class='duplicates'/>").appendTo(this.options);
		$(duplicates).click(function(){
			showDuplicates = !showDuplicates;
			checkVisibility();
		});

		$("<label class='thinningLabel'>thinning</label>").appendTo(this.options);
		var filterSlider = $("<div class='filterSlider'></div>").appendTo(this.options);
		$(filterSlider).slider({

			min: 1,
			max: 10,
			value: 10,
			step: 1,
			slide: function( event, ui ){
				distance = ui.value - 1;
				checkVisibility();
			}
		});

		$("<label class='thinningLabel'>circle sizes</label>").appendTo(this.options);
		var circleSlider = $("<div class='filterSlider'></div>").appendTo(this.options);
		$(circleSlider).slider({
			min: 1,
			max: 5,
			value: 4,
			step: 1,
			slide: function( event, ui ){
				for( var i=0; i<dots.length; i++ ){
					dots[i].attr({'r': ui.value });
				}
			}
		});

	}

};
