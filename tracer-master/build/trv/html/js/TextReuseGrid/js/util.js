		var JenksCaspall = function(classes,data,entry,dest){
			var dataClasses = [];
			var classMedians = [];
			for( var i=0; i<classes; i++ ){
				dataClasses.push([]);
				classMedians.push(0);
			}
			var sortByEntry = function(doc1,doc2){
				if( doc1[entry] < doc2[entry] ){
					return -1;
				}
				return 1;
			}
			data.sort(sortByEntry);
			if( data.length < classes ){
				for( var i=0; i<data.length; i++ ){
					dataClasses[i].push(data[i]);
					dataClasses[i][0][dest] = i;
				}
				return dataClasses.slice(0,data.length);
			}
			var calcCountMedians = function(){
				for( var i=0; i<dataClasses.length; i++ ){
					classMedians[i] = 0;
					for( var j=0; j<dataClasses[i].length; j++ ){
						classMedians[i] += dataClasses[i][j][entry];
					}
					classMedians[i] /= dataClasses[i].length;
				}
			}
			var j = 0;
			var fsi = data.length/classes;
			var fs = Math.round(fsi);
			for( var i=0; i<data.length; i++ ){
				if( i==fs ){
					j++;
					fs = Math.round((j+1)*fsi);
				}
				dataClasses[j].push(data[i]);
			}
			var checkSplit = function(){
				do {
					calcCountMedians();
					var separated = true;
					for( var i=0; i<classMedians.length-1; i++ ){
						if( classMedians[i] == classMedians[i+1] ){
							separated = false;
							dataClasses[i] = dataClasses[i].concat(dataClasses[i+1]);
							dataClasses = dataClasses.slice(0,i+1).concat(dataClasses.slice(i+2));
							break;
						}
					}
					if( !separated ){
						var diff = 0, cand = -1; 
						for( var i=0; i<dataClasses.length; i++ ){
							var diff_i = Math.abs(dataClasses[i][0][entry] - dataClasses[i][dataClasses[i].length-1][entry]);
							if( diff_i > diff ){
								diff = diff_i;
								cand = i;
							}
						}
						if( cand > -1 ){
							var dataClassesNew = dataClasses.slice(0,cand);
							var candidate = dataClasses[cand];
							var newClass1 = [], newClass2 = [];
							for( var i=0; i<candidate.length; i++ ){
								if( i<candidate.length/2 ){
									newClass1.push(candidate[i]);
								}
								else {
									newClass2.push(candidate[i]);
								}
							}
							dataClassesNew.push(newClass1);
							dataClassesNew.push(newClass2);
							dataClassesNew = dataClassesNew.concat(dataClasses.slice(cand+1));
							dataClasses = dataClassesNew;
						}
						else {
							classes--;
							classMedians.pop();
						}
					}
				}
				while(!separated);
			}
			do {
				checkSplit();
				calcCountMedians();
				var improvement = false;
				var from = 0, to = 0, shift = 0;
				for( var i=0; i<dataClasses.length; i++ ){
					if( i > 0 ){
						var innerDiff = classMedians[i]-dataClasses[i][0][entry];
						var outerDiff = dataClasses[i][0][entry]-classMedians[i-1];
						if( innerDiff > outerDiff && innerDiff-outerDiff > shift ){
							shift = innerDiff-outerDiff;
							from = i;
							to = i-1;
						}
					}
					if( i < dataClasses.length-1 ){
						var innerDiff = dataClasses[i][dataClasses[i].length-1][entry]-classMedians[i];
						var outerDiff = classMedians[i+1]-dataClasses[i][dataClasses[i].length-1][entry];
						if( innerDiff > outerDiff && innerDiff-outerDiff > shift ){
							shift = innerDiff-outerDiff;
							from = i;
							to = i+1;
						}
					}
				}
				if( shift > 0 ){
					improvement = true;
					if( from < to ){
						dataClasses[to] = [dataClasses[from][dataClasses[from].length-1]].concat(dataClasses[to]);
						dataClasses[from].pop();
					}
					else {
						dataClasses[to] = dataClasses[to].concat([dataClasses[from][0]]);
						dataClasses[from] = dataClasses[from].slice(1);
					}
				}
			}
			while(improvement);
			for( var i=0; i<dataClasses.length; i++ ){
				for( var j=0; j<dataClasses[i].length; j++ ){
					dataClasses[i][j][dest] = i;
				}
			}
			return dataClasses;
		}

		var hsv2rgb = function(h,s,v) {
			var r, g, b;
			var RGB = new Array();
			if(s==0){
			  RGB['red']=RGB['green']=RGB['blue']=Math.round(v*255);
			}else{
			  // h must be < 1
			  var var_h = h * 6;
			  if (var_h==6) var_h = 0;
			  //Or ... var_i = floor( var_h )
			  var var_i = Math.floor( var_h );
			  var var_1 = v*(1-s);
			  var var_2 = v*(1-s*(var_h-var_i));
			  var var_3 = v*(1-s*(1-(var_h-var_i)));
			  if(var_i==0){ 
			    var_r = v; 
			    var_g = var_3; 
			    var_b = var_1;
			  }else if(var_i==1){ 
			    var_r = var_2;
			    var_g = v;
			    var_b = var_1;
			  }else if(var_i==2){
			    var_r = var_1;
			    var_g = v;
			    var_b = var_3
			  }else if(var_i==3){
			    var_r = var_1;
			    var_g = var_2;
			    var_b = v;
			  }else if (var_i==4){
			    var_r = var_3;
			    var_g = var_1;
			    var_b = v;
			  }else{ 
			    var_r = v;
			    var_g = var_1;
			    var_b = var_2
			  }
			  //rgb results = 0 ÷ 255  
			  RGB['red']=Math.round(var_r * 255);
			  RGB['green']=Math.round(var_g * 255);
			  RGB['blue']=Math.round(var_b * 255);
			  }
			return "rgb("+Math.round(var_r*255)+","+Math.round(var_g*255)+","+Math.round(var_b*255)+")";
		};

