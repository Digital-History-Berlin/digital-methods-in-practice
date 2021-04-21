function Graph(){

	this.vertices = [];
	this.vertexMap = [];

	this.getVertex = function(index){
		return this.vertexMap[index];
	}

	this.removeVertex = function(index){
		var v = this.vertexMap[index];
		for( var i=0; i<v.successors.length; i++ ){
			this.vertexMap[v.successors[i]].removePredecessor(index);
		}
		for( var i=0; i<v.predecessors.length; i++ ){
			this.vertexMap[v.predecessors[i]].removeSuccessor(index);
		}
		for( var i=0; i<this.vertices.length; i++ ){
			if( this.vertices[i] == v ){
				this.vertices.splice(i,1);
				break;
			}
		}
		delete this.vertexMap[index];
	}

	this.addVertex = function(v){
		this.vertices.push(v);
		this.vertexMap[v.index] = v;
	}

	this.mergeVertices = function(v1,v2,adjacent){
		if( adjacent ){
			if( v1.successors.length == 1 && v1.successors[0] == v2.index && v2.predecessors.length == 1 || v2.successors.length == 1 && v2.successors[0] == v1.index && v1.predecessors.length == 1 ){
				adjacent = true;
			}
			else {
				adjacent = false;
			}
		}
		var v = new Vertex(this,SentenceAlignerProperties.getVertexIndex(),v1.token);
		this.addVertex(v);
		v.count = v1.count + v2.count;
		for( var i=0; i<v1.sources.length; i++ ){
			v.sources.push(v1.sources[i]);
		}
		for( var i=0; i<v2.sources.length; i++ ){
			v.sources.push(v2.sources[i]);
		}
		for( var i=0; i<v1.predecessors.length; i++ ){
			var id = v1.predecessors[i];
			if( id == v1.index || id == v2.index ){
				id = v.index;
			}
			v.addPredecessor(id);
			this.vertexMap[id].addSuccessor(v.index);
		}
		for( var i=0; i<v2.successors.length; i++ ){
			var id = v2.successors[i];
			if( id == v1.index || id == v2.index ){
				id = v.index;
			}
			v.addSuccessor(id);
			this.vertexMap[id].addPredecessor(v.index);
		}
		for( var i=0; i<v1.successors.length; i++ ){
			var id = v1.successors[i];
			if( id == v1.index || id == v2.index ){
				id = v.index;
			}			
			v.addSuccessor(id);
			this.vertexMap[id].addPredecessor(v.index);
		}
		for( var i=0; i<v2.predecessors.length; i++ ){
			var id = v2.predecessors[i];
			if( id == v1.index || id == v2.index ){
				id = v.index;
			}			
			v.addPredecessor(id);
			this.vertexMap[id].addSuccessor(v.index);
		}
		this.removeVertex(v1.index);
		this.removeVertex(v2.index);
		if( adjacent ){
			v.removeSuccessor(v.index);
			v.removePredecessor(v.index);
		}
		return v;
	}

	this.clone = function(){
		var cg = new Graph();
		for( var i=0; i<this.vertices.length; i++ ){
			cg.addVertex(new Vertex(cg,this.vertices[i].index,this.vertices[i].token));
		}
		for( var i=0; i<this.vertices.length; i++ ){
			var v = this.vertices[i];
			var vc = cg.vertices[i];
			vc.count = v.count;
			for( var j=0; j<v.sources.length; j++ ){
				vc.sources.push(v.sources[j]);
			}
			for( var j=0; j<v.successors.length; j++ ){
				vc.addSuccessor(v.successors[j]);
				cg.vertexMap[v.successors[j]].addPredecessor(vc.index);
			}
			for( var j=0; j<v.predecessors.length; j++ ){
				vc.addPredecessor(v.predecessors[j]);
				cg.vertexMap[v.predecessors[j]].addSuccessor(vc.index);
			}
		}
		return cg;
	}

	this.isAcyclicFromVertex = function(v1,v2){
		var v = new Vertex(this,SentenceAlignerProperties.getVertexIndex(),v1.token);
		this.addVertex(v);
		v.count = v1.count + v2.count;
		for( var i=0; i<v1.sources.length; i++ ){
			v.sources.push(v1.sources[i]);
		}
		for( var i=0; i<v2.sources.length; i++ ){
			v.sources.push(v2.sources[i]);
		}
		for( var i=0; i<v1.predecessors.length; i++ ){
			var id = v1.predecessors[i];
			if( id == v1.index || id == v2.index ){
				id = v.index;
			}
			v.addPredecessor(id);
			this.vertexMap[id].addSuccessor(v.index);
		}
		for( var i=0; i<v2.successors.length; i++ ){
			var id = v2.successors[i];
			if( id == v1.index || id == v2.index ){
				id = v.index;
			}
			v.addSuccessor(id);
			this.vertexMap[id].addPredecessor(v.index);
		}
		for( var i=0; i<v1.successors.length; i++ ){
			var id = v1.successors[i];
			if( id == v1.index || id == v2.index ){
				id = v.index;
			}			
			v.addSuccessor(id);
			this.vertexMap[id].addPredecessor(v.index);
		}
		for( var i=0; i<v2.predecessors.length; i++ ){
			var id = v2.predecessors[i];
			if( id == v1.index || id == v2.index ){
				id = v.index;
			}
			v.addPredecessor(id);
			this.vertexMap[id].addSuccessor(v.index);
		}
		for( var i=0; i<this.vertices.length; i++ ){
			this.vertices[i].visited = 0;
			this.vertices[i].limit = this.vertices[i].predecessors.length;
			for( var j=0; j<this.vertices[i].predecessors.length; j++ ){
				if( this.vertices[i].predecessors[j] == v1.index ){
					this.vertices[i].limit--;
				}
				if( this.vertices[i].predecessors[j] == v2.index ){
					this.vertices[i].limit--;
				}
			}
		}
		v.visited = v.limit;
		var edges = [];
		for( var i=0; i<v.successors.length; i++ ){
			if( v.successors[i] != v1.index && v.successors[i] != v2.index ){
				edges.push({
					head: v,
					tail: this.getVertex(v.successors[i])
				});
			}
		}
		while( edges.length > 0 ){
			var new_edges = [];
			for( var i=0; i<edges.length; i++ ){
				var e = edges[i];
				e.tail.visited++;
				if( e.tail.visited > e.tail.limit ){
					this.removeVertex(v.index);
					return false;
				}
				for( var j=0; j<e.tail.successors.length; j++ ){
					if( v.successors[i] != v1.index && v.successors[i] != v2.index && e.tail.visited == 1 ){
						new_edges.push({
							head: e.tail,
							tail: this.getVertex(e.tail.successors[j])
						});
					}
				}
			}
			edges = new_edges;
		}
		this.removeVertex(v1.index);
		this.removeVertex(v2.index);
		return v;
	}

	this.isAcyclic = function(){
		do {
			var n = this.vertices.length;
			for( var i=0; i<this.vertices.length; i++ ){
				var vi = this.vertices[i];
				var suc = vi.successors, pred = vi.predecessors;
				if( suc.length == 0 ){
					for( var j=0; j<pred.length; j++ ){
						this.vertexMap[pred[j]].removeSuccessor(vi.index);
					}
					vi.predecessors = [];
				}
			}
			for( var i=this.vertices.length; i>0; i-- ){
				var vi = this.vertices[i-1];
				if( vi.successors.length == 0 && vi.predecessors.length == 0 ){
					this.removeVertex(vi.index);
				}
			}
		}
		while( n > this.vertices.length );
		if( this.vertices.length > 0 ){
			return false;
		}
		return true;
	}

}

function Vertex(graph,index,token) {
	this.graph = graph;
	this.token = token;
	this.successors = [];
	this.predecessors = [];
	this.count = 1;
	this.traced = false;
	this.linked = true;
	this.sources = [];
	this.index = index;
	this.removeSuccessor = function(suc){
		for( var i=0; i<this.successors.length; i++ ){
			if( this.successors[i] == suc ){
				this.successors.splice(i,1);
				return;
			}
		}		
	}
	this.removePredecessor = function(pred){
		for( var i=0; i<this.predecessors.length; i++ ){
			if( this.predecessors[i] == pred ){
				this.predecessors.splice(i,1);
				return;
			}
		}		
	}
	this.addSuccessor = function(suc){
		var found = false;
		for( var i=0; i<this.successors.length; i++ ){
			if( suc == this.successors[i] ){
				found = true;
				break;
			}
		}
		if( !found ){
			this.successors.push(suc);
		}
	}
	this.addPredecessor = function(pred){
		var found = false;
		for( var i=0; i<this.predecessors.length; i++ ){
			if( pred == this.predecessors[i] ){
				found = true;
				break;
			}
		}
		if( !found ){
			this.predecessors.push(pred);
		}
	}
	this.increase = function(pos){
		this.count++;
	}
	this.merge = function(vertex){
		this.sources = this.sources.concat(vertex.sources);
		for( var i=0; i<vertex.successors.length; i++ ){
			this.addSuccessor(vertex.successors[i]);
			this.graph.vertexMap[vertex.successors[i]].addPredecessor(this.index);
			this.graph.vertexMap[vertex.successors[i]].removePredecessor(vertex.index);
		}
		for( var i=0; i<vertex.predecessors.length; i++ ){
			this.addPredecessor(vertex.predecessors[i]);
			this.graph.vertexMap[vertex.predecessors[i]].addSuccessor(this.index);
			this.graph.vertexMap[vertex.predecessors[i]].removeSuccessor(vertex.index);
		}
		this.count += vertex.count;
	}
}

function Edge(head,tail) {
	this.head = head;
	this.tail = tail;
}

var SentenceAlignerProperties = new function(){

	this.colors = [ "red", "blue", "green", "rgb(230,230,0)", "orange", "brown", "purple" ];
	this.sources = [ "ASV", "BasicEnglish", "Darby", "KJV", "Webster", "WEB", "YLT" ];

	var vid = 0;
	this.getVertexIndex = function(){
		return ++vid;
	}

	this.getColor = function(id){
		if( this.colors.length-1 < id ){
			this.colors.push( "rgb(" + Math.floor((Math.random()*255)+1) + "," + Math.floor((Math.random()*255)+1) + "," + Math.floor((Math.random()*255)+1) + ")");
		}
		return this.colors[id];
	}

}

function SentenceAligner(){

	var globalAdjacent = false;

	var threshold = 0.1;

	this.graph = new Graph();

	this.startVertex = new Vertex(this.graph,SentenceAlignerProperties.getVertexIndex(),'>>');
	this.endVertex = new Vertex(this.graph,SentenceAlignerProperties.getVertexIndex(),'<<');
	this.graph.addVertex(this.startVertex);
	this.graph.addVertex(this.endVertex);

	this.startVertex.id = 'first';
	this.endVertex.id = 'last';
	this.vertices = [];
	this.sentencePaths = [];
	this.connections = [];

	this.alignSentences = function(sentences){
		var sal = this;
		var words = [];
		var wordVertices = [];
		var tokenized = [];
		var lastVertex = undefined;
		var wordlist = [];
		for( var i=0; i<sentences.length; i++ ){
			var sword = [];
			lastVertex = undefined;
			var sentence = this.clean(sentences[i]);
			var tokens = sentence.split(" ");
			var t = [];
			for( var j=0; j<tokens.length; j++ ){
				var word = {
					id: i+"-"+j,
					word: tokens[j],
					sid: i,
					wid: j,
					gid: words.length
				};
				words.push(word);
				sword.push(word);
				t.push(word);

					var v = new Vertex(this.graph,SentenceAlignerProperties.getVertexIndex(),tokens[j]);
					v.sources.push({
						sourceId: i,
						token: tokens[j]
					});
					this.graph.addVertex(v);
					if( typeof lastVertex != 'undefined' ){
						lastVertex.addSuccessor(v.index);
						v.addPredecessor(lastVertex.index);
					}
					lastVertex = v;
					wordVertices[word.id] = v;

			}
			wordlist.push(sword);
			tokenized.push(t);
		}
		var sortBySize = function(s1,s2){
			if( s1.length > s2.length ){
				return -1;
			}
			return 1;
		}
		var pairs = [];
		var wordMatches = [];
		var nodes = [];
		var assignments = [];
		for( var i=0; i<words.length; i++ ){
			wordMatches.push([]);
			nodes.push(false);
			assignments.push(false);
		}
		for( var i=0; i<tokenized.length-1; i++ ){
			for( var j=i+1; j<tokenized.length; j++ ){
				this.matches = [];
				this.pairAlignment(tokenized[i],tokenized[j],[]);
				if( this.matches.length == 0 ){
					continue;
				}
				this.matches.sort(sortBySize);
				var ms = "";
				for( var k=0; k<this.matches[0].length; k++ ){
					pairs.push({
						pair: this.matches[0][k]
					});
					var w1 = this.matches[0][k].w1;
					var w2 = this.matches[0][k].w2;
					wordMatches[w1.gid].push(w2);
					wordMatches[w2.gid].push(w1);
				}
			}
		}
		for( var i=0; i<pairs.length; i++ ){
			var w1 = pairs[i].pair.w1;
			var w2 = pairs[i].pair.w2;
			pairs[i].value = 2;
			for( var j=0; j<wordMatches[w1.gid].length; j++ ){
				if( wordMatches[w1.gid][j] == w2 ){
					continue;
				}
				for( var k=0; k<wordMatches[w2.gid].length; k++ ){
					if( wordMatches[w2.gid][k] == w1 ){
						continue;
					}
					if( wordMatches[w1.gid][j] == wordMatches[w2.gid][k] ){
						pairs[i].value++;
					}
				}				
			}
		}
		var sortBySize2 = function(p1,p2){
			if( p1.value > p2.value ){
				return -1;
			}
			return 1;
		}
		pairs.sort(sortBySize2);
		var checkMerge = function(w1,w2){
			var v1 = sal.graph.getVertex(wordVertices[w1.id].index), v2 = sal.graph.getVertex(wordVertices[w2.id].index);
			if( v1 == v2 ){
				return;
			}
			var v = sal.graph.isAcyclicFromVertex(v1,v2);
			if( v ){
				for( var i=0; i<words.length; i++ ){
					if( wordVertices[words[i].id] == v1 || wordVertices[words[i].id] == v2 ){
						wordVertices[words[i].id] = v;
					}
				}
			}
			else {
			}
		}
		for( var i=0; i<pairs.length; i++ ){
			checkMerge(pairs[i].pair.w1,pairs[i].pair.w2);
		}
		for( var i=0; i<wordlist.length; i++ ){
			var sp = [];
			for( var j=0; j<wordlist[i].length; j++ ){
				var v = wordVertices[wordlist[i][j].id];
				if( j == 0 ){
					this.startVertex.addSuccessor(v.index);
					v.addPredecessor(this.startVertex.index);
				}
				if( j == wordlist[i].length-1 ){
					v.addSuccessor(this.endVertex.index);						
					this.endVertex.addPredecessor(v.index);
				}
				sp.push(v);
			}
			this.sentencePaths.push(sp);
		}
		this.vertices = this.graph.vertices;
	};

	this.pairAlignment = function(s1,s2){
		var matches = [];
		for( var i=0; i<s1.length; i++ ){
			matches.push([]);
			for( var j=0; j<s2.length; j++ ){
				if( s1[i].word == s2[j].word ){
					matches[i].push(s2[j]);
				}
			}			
		}
		var paths = [];
		for( var i=0; i<matches.length; i++ ){
			var newPaths = [];
			var addPath = function(path1){
				var lNode1 = path1[path1.length-1];
				var found = false;
				var np = [];
				for( var j=newPaths.length; j>0; j-- ){
					var path2 = newPaths[j-1];
					var lNode2 = path2[path2.length-1];
					if( lNode1.w2 == lNode2.w2 && path1.length != path2.length ){
						if( path1.length <= path2.length ){
							np.push(path2);
							found = true;
						}
					}
					else if( lNode1.w2 == lNode2.w2 && path1.length == path2.length ){
						np.push(path2);
						found = true;
					}
					else {
						np.push(path2);
					}
				}
				if( !found ){
					np.push(path1);
				}
				newPaths = np;
			}
			for( var k=0; k<paths.length; k++ ){
				var path = paths[k];
				addPath(path);
				var lNode = path[path.length-1].w2;
				for( var j=0; j<matches[i].length; j++ ){
					var node = matches[i][j];
					if( node.wid > lNode.wid ){
						addPath(path.concat([{ w1: s1[i], w2: node}]));
					}
				}
			}
			for( var j=0; j<matches[i].length; j++ ){
				addPath([{ w1: s1[i], w2: matches[i][j]}]);
			}
			paths = newPaths;
		}
		this.matches = paths;
	};

	this.clean = function(sentence){
		sentence = sentence.toLowerCase();
		sentence = sentence.replace(/--/g, "");
		sentence = sentence.replace(/  /g, " ");
		sentence = sentence.replace(/,/g, "");
		sentence = sentence.replace(/\./g, "");
		sentence = sentence.replace(/;/g, "");
		sentence = sentence.replace(/:/g, "");
		sentence = sentence.replace(/\(/g, "");
		sentence = sentence.replace(/\)/g, "");
		sentence = sentence.replace(/\[/g, "");
		sentence = sentence.replace(/\]/g, "");
		sentence = sentence.replace(/\'/g, "");
		sentence = sentence.replace(/\"/g, "");
		sentence = sentence.replace(/´/g, "");
		sentence = sentence.replace(/`/g, "");
		if( sentence.lastIndexOf(" ") == sentence.length - 1 ){
			sentence = sentence.substring(0,sentence.length-1);
		}
		return sentence;
	}

	this.strongestShortestPath = function(s){
		var strength = 0, length = 1000000;
		var path;
		for( var i=0; i<s.successors.length; i++ ){
			var pi = [s];
			var si = s.count;
			var li = 1;
			var vertex = this.graph.getVertex(s.successors[i]);
			pi.push(vertex);
			if( !vertex.traced ){
				var vs = vertex.successors;
				si += vertex.count;
				while( vs.length > 0 ){
					var lc = this.graph.getVertex(vs[0]).count;
					var v = this.graph.getVertex(vs[0]);
					for( var j=1; j<vs.length; j++ ){
						if( this.graph.getVertex(vs[j]).count > lc ){
							lc = this.graph.getVertex(vs[j]).count;
							v = this.graph.getVertex(vs[j]);
						}
					}
					li++;
					pi.push(v);
					if( !v.traced ){
						si += lc;
						vs = v.successors;
					}
					else {
						vs = [];
					}
				}
			}
			if( li < length || li == length && si > strength ){
				length = li;
				strength = si;
				path = pi;
			}
		}
		return {
			strength: strength,
			length: length,
			path: path
		}
	}	

	this.getPaths = function(sid){
		for( var i=0; i<this.vertices.length; i++ ){
			this.vertices[i].traced = false;
		}
		var p = this.sentencePaths[sid].concat([this.endVertex]);
		p.splice(0,0,this.startVertex);
		for( var i=0; i<p.length; i++ ){
			p[i].traced = true;
		}
		var paths = [p];
		var traverse = true;
		var runs = 0;
		while( traverse ){
			runs++;
			traverse = false;
			var c = undefined;
			for( var i=0; i<this.vertices.length; i++ ){
				var v = this.vertices[i];
				if( v.traced ){
					for( var k=0; k<v.successors.length; k++ ){
						if( !this.graph.getVertex(v.successors[k]).traced ){
							var p = this.strongestShortestPath(this.graph.getVertex(v.successors[k]));
							p.path.splice(0,0,v);
							if( typeof c == "undefined" || c.length > p.length || 
								c.length == p.length && p.strength > c.strength ){
								c = p;
							}
						}
					}
				}
				else {
					traverse = true;
				}
			}
			if( typeof c != "undefined" ){
				for( var i=0; i<c.path.length; i++ ){
					c.path[i].traced = true;
				}
				paths.push(c.path);
			}
		}
		return paths;
	}

	this.generatePath = function(v1,v2,sy1,sy2){
		if( typeof sy == 'undefined' ){
			sy = 0;
		}
		var v1x2 = v1.x2 + 3;
		var v2x1 = v2.x1 - 3;
		var y1o = (v1.y1+v1.y2)/2;
		var y2o = (v2.y1+v2.y2)/2;
		var y1 = (v1.y1+v1.y2)/2 + sy1;
		var y2 = (v2.y1+v2.y2)/2 + sy2;	
		var path = "M "+v1x2+" "+y1+" ";
		if( v1x2 > v2x1 ){
			var x1 = v1x2;
			var x2 = v2x1;
			var x12 = v1x2+20;
			var x34 = v2x1-20;
			var y12 = (y1+y2)/2;
			var y_a = y1 + (y12-y1)/2;
			var y_b = y12 + (y12-y1)/2;
			path += "C "+x1+" "+y1+" ";
			path += x12+" "+y1+" ";
			path += x12+" "+y_a+" ";
			path += "C "+x12+" "+y_a+" ";
			path += x12+" "+y12+" ";
			path += x1+" "+y12+" ";
			path += "L "+x1+" "+y12+" "+x2+" "+y12+" ";
			path += "C "+x2+" "+y12+" ";
			path += x34+" "+y12+" ";
			path += x34+" "+y_b+" ";
			path += "C "+x34+" "+y_b+" ";
			path += x34+" "+y2+" ";
			path += x2+" "+y2+" ";
		}
		else if( v1x2 > v2x1 - 20 ){
			var x1 = v1x2;
			var x2 = v2x1;
			var x12 = (v1x2+v2x1)/2;
			var y12 = (y1+y2)/2;
			var y_a = y1 + (y12-y1)/2;
			var y_b = y12 + (y12-y1)/2;
			path += "C "+x1+" "+y1+" ";
			path += (x1+20)+" "+y1+" ";
			path += (x1+20)+" "+y_a+" ";
			path += "C "+(x1+20)+" "+y_a+" ";
			path += (x1+20)+" "+y12+" ";
			path += x12+" "+y12+" ";
			path += "C "+x12+" "+y12+" ";
			path += (x2-20)+" "+y12+" ";
			path += (x2-20)+" "+y_b+" ";
			path += "C "+(x2-20)+" "+y_b+" ";
			path += (x2-20)+" "+y2+" ";
			path += x2+" "+y2+" ";
		}
		else {
			if( y1o == y2o ){
				var overlaps = [];
				for( var k=0; k<this.vertices.length; k++ ){
					if( this.overlap(v1x2,v2x1,this.vertices[k].x1,this.vertices[k].x2,y1o,y2o,this.vertices[k].y1,this.vertices[k].y2) ){
						overlaps.push(this.vertices[k]);
					}
				}
				var x_min = 10000, x_max = -1, y_min = 10000, y_max = -1;
				if( overlaps.length > 0 ){
					for( var k=0; k<overlaps.length; k++ ){
						if( overlaps[k].x1 < x_min ){
							x_min = overlaps[k].x1;
						}
						if( overlaps[k].x2 > x_max ){
							x_max = overlaps[k].x2;
						}
						if( overlaps[k].y1 < y_min ){
							y_min = overlaps[k].y1;
						}
						if( overlaps[k].y2 > y_max ){
							y_max = overlaps[k].y2;
						}
					}
					var x1 = v1x2;
					var x12 = v1x2+10;
					var x2 = v1x2+20;
					var y1 = y1;
					var y2 = y_min - 10 + sy1;
					var y12 = (y1+y2)/2;
					var xa = v2x1-20;
					var xab = v2x1-10;
					var xb = v2x1;
					path += "C "+x1+" "+y1+" ";
					path += x12+" "+y1+" ";
					path += x12+" "+y12+" ";
					path += "C "+x12+" "+y12+" ";
					path += x12+" "+y2+" ";
					path += x2+" "+y2+" ";
					path += "L "+x2+" "+y2+" "+xa+" "+y2+" ";
					y1 = (v1.y1+v1.y2)/2 + sy2;
					y12 = (y1+y2)/2;
					path += "C "+xa+" "+y2+" ";
					path += xab+" "+y2+" ";
					path += xab+" "+y12+" ";
					path += "C "+xab+" "+y12+" ";
					path += xab+" "+y1+" ";
					path += xb+" "+y1+" ";
				}
				else {
					path += "L "+v1x2+" "+y1+" "+v2x1+" "+y2+" ";
				}
			}
			else {
				var y12 = (y1+y2)/2;
				var x1, x2, x12;
				if( v2x1 - v1x2 == 20 ){
					x1 = v1x2;
					x12 = v1x2+10;
					x2 = v2x1;
					path += "C "+x1+" "+y1+" ";
					path += x12+" "+y1+" ";
					path += x12+" "+y12+" ";
					path += "C "+x12+" "+y12+" ";
					path += x12+" "+y2+" ";
					path += x2+" "+y2+" ";
				}	
				else {				
					if( v2.count > v1.count ){
						var overlap = false;
						for( var k=0; k<this.vertices.length; k++ ){
							if( this.overlap(v1x2,v2x1-20,this.vertices[k].x1,this.vertices[k].x2,y1,y1,this.vertices[k].y1,this.vertices[k].y2) ){
								overlap = true;
							}
						}
						if( !overlap ){
							path += "L "+v1x2+" "+y1+" ";
							path += (v2x1-20)+" "+y1+" ";
							x1 = v2x1-20;
							x12 = v2x1-10;
							x2 = v2x1;
							path += "C "+x1+" "+y1+" ";
							path += x12+" "+y1+" ";
							path += x12+" "+y12+" ";
							path += "C "+x12+" "+y12+" ";
							path += x12+" "+y2+" ";
							path += x2+" "+y2+" ";
						}
						else {
							x1 = v1x2;
							x12 = v1x2+10;
							x2 = v1x2+20;
							path += "C "+x1+" "+y1+" ";
							path += x12+" "+y1+" ";
							path += x12+" "+(y1+y12)/2+" ";
							path += "C "+x12+" "+(y1+y12)/2+" ";
							path += x12+" "+y12+" ";
							path += x2+" "+y12+" ";
							path += "L "+x2+" "+y12+" ";
							path += (v2x1-20)+" "+y12+" ";
							x1 = v2x1-20;
							x12 = v2x1-10;
							x2 = v2x1;
							path += "C "+x1+" "+y12+" ";
							path += x12+" "+y12+" ";
							path += x12+" "+(y12+y2)/2+" ";
							path += "C "+x12+" "+(y12+y2)/2+" ";
							path += x12+" "+y2+" ";
							path += x2+" "+y2+" ";
						}
					}
					else {
						var overlap = false;
						for( var k=0; k<this.vertices.length; k++ ){
							if( this.overlap(v1x2+20,v2x1,this.vertices[k].x1,this.vertices[k].x2,y2,y2,this.vertices[k].y1,this.vertices[k].y2) ){
								overlap = true;
							}
						}
						if( !overlap ){
							x1 = v1x2;
							x12 = v1x2+10;
							x2 = v1x2+20;
							path += "C "+x1+" "+y1+" ";
							path += x12+" "+y1+" ";
							path += x12+" "+y12+" ";
							path += "C "+x12+" "+y12+" ";
							path += x12+" "+y2+" ";
							path += x2+" "+y2+" ";
							path += "L "+x2+" "+y2+" ";
							path += v2x1+" "+y2+" ";
						}
						else {
							x1 = v1x2;
							x12 = v1x2+10;
							x2 = v1x2+20;
							path += "C "+x1+" "+y1+" ";
							path += x12+" "+y1+" ";
							path += x12+" "+(y1+y12)/2+" ";
							path += "C "+x12+" "+(y1+y12)/2+" ";
							path += x12+" "+y12+" ";
							path += x2+" "+y12+" ";
							path += "L "+x2+" "+y12+" ";
							path += (v2x1-20)+" "+y12+" ";
							x1 = v2x1-20;
							x12 = v2x1-10;
							x2 = v2x1;
							path += "C "+x1+" "+y12+" ";
							path += x12+" "+y12+" ";
							path += x12+" "+(y12+y2)/2+" ";
							path += "C "+x12+" "+(y12+y2)/2+" ";
							path += x12+" "+y2+" ";
							path += x2+" "+y2+" ";
						}
					}
				}
			}
		}
		return path;
	};

	this.drawSentencePath = function(i){
		var p = this.sentencePaths[i];
		var path = "";
		for( var j=1; j<p.length; j++ ){
			path += this.generatePath(p[j-1],p[j],0,0);
		}
		var pvis = this.paper.path(path).attr({stroke: SentenceAlignerProperties.getColor(i), "stroke-width": 4, "stroke-linecap": "round", "opacity": "1.0"});
		return pvis;
	}

	this.drawConnections = function(node,vertex){
		this.connections = [];
		for( var i=0; i<this.vertices.length; i++ ){
			this.vertices[i].ins = [];
			this.vertices[i].outs = [];
		}
		for( var i=0; i<this.sentencePaths.length; i++ ){
			var p = this.sentencePaths[i];
			for( var j=0; j<p.length; j++ ){
				if( p[j] == vertex ){
					for( var j=0; j<p.length; j++ ){
						if( j>0 ){
							p[j].ins.push({
								v: p[j-1],
								id: i
							});
						}
						if( j<p.length-1 ){
							p[j].outs.push({
								v: p[j+1],
								id: i
							});
						}
					}
				}
			}
		}
		for( var i=0; i<this.vertices.length; i++ ){
			var v = this.vertices[i];
			var yv = (v.y1+v.y2)/2;
			this.vertices[i].ins.sort(function(t1,t2){
				var y1 = (t1.v.y1+t1.v.y2)/2;
				var y2 = (t2.v.y1+t2.v.y2)/2;
				if( y1 > y2 ){
					return 1;
				}
				if( y1 == y2 && yv == y1 && t1.v.x2 > t2.v.x2 ){
					return 1;
				}
				if( y1 == y2 && yv > y1 && t1.v.x2 < t2.v.x2 ){
					return 1;
				}
				if( y1 == y2 && yv < y1 && t1.v.x2 > t2.v.x2 ){
					return 1;
				}
				return -1;
			});
			this.vertices[i].outs.sort(function(t1,t2){
				var y1 = (t1.v.y1+t1.v.y2)/2;
				var y2 = (t2.v.y1+t2.v.y2)/2;
				if( y1 > y2 ){
					return 1;
				}
				if( y1 == y2 && yv == y1 && t1.v.x1 < t2.v.x1 ){
					return 1;
				}
				if( y1 == y2 && yv > y1 && t1.v.x1 > t2.v.x1 ){
					return 1;
				}
				if( y1 == y2 && yv < y1 && t1.v.x1 < t2.v.x1 ){
					return 1;
				}
				return -1;
			});
			/*
			if( this.vertices[i].token == "the" && this.vertices[i].ins.length == 4 ){
				console.info(this.vertices[i].ins,this.vertices[i].outs);
			}
			*/
		}
		var getShift = function(id,array){
			if( array.length == 1 ){
				return 0;
			}
			for( var i=0; i<array.length; i++ ){
				if( array[i].id == id ){
					return i*4 - array.length*4/2;
				}
			}
		}
		for( var i=0; i<this.sentencePaths.length; i++ ){
			var p = this.sentencePaths[i];
			for( var j=0; j<p.length; j++ ){
				if( p[j] == vertex ){
					var path = "";
					for( var j=1; j<p.length; j++ ){
						path += this.generatePath(p[j-1],p[j],getShift(i,p[j-1].outs),getShift(i,p[j].ins));
					}
					var pvis = this.paper.path(path).attr({stroke: SentenceAlignerProperties.getColor(i), "stroke-width": 3, "stroke-linecap": "round", "opacity": "1.0"});
					this.connections.push(pvis);
					break;
				}
			}
		}
	};

	this.generalConnections = function(){
		this.gc = [];
		for( var i=0; i<this.vertices.length; i++ ){
			var v = this.vertices[i];
			if( v == this.startVertex ){
				continue;
			}
			var p = v.successors;
			for( var j=0; j<p.length; j++ ){
				if( this.graph.getVertex(p[j]) == this.endVertex ){
					continue;
				}
				var pvis = this.paper.path(this.generatePath(v,this.graph.getVertex(p[j]),0,0)).attr({stroke: "#3E576F", "stroke-width": 3, "stroke-linecap": "round", "opacity": "0.8"});
				this.gc.push(pvis);
			}
		}
	};

	this.setXFlow = function(){
		var gap = 30;
		var edges = [];
		for( var i=0; i<this.startVertex.successors.length; i++ ){
			edges.push({
				head: this.startVertex,
				tail: this.graph.getVertex(this.startVertex.successors[i])
			});
		}
		var widthS = this.startVertex.textNode.getBBox().width;
		this.startVertex.x1 = gap;
		this.startVertex.x2 = gap + widthS;
		this.startVertex.textNode.attr({ x: gap });
		while( edges.length > 0 ){
			var new_edges = [];
			for( var i=0; i<edges.length; i++ ){
				var e = edges[i];
				if( e.tail.x1 < e.head.x2 + gap ){
					var widthT = e.tail.textNode.getBBox().width;
					e.tail.x1 = e.head.x2 + gap;
					e.tail.x2 = e.head.x2 + gap + widthT;
					e.tail.textNode.attr({ x: e.head.x2 + gap });
					for( var j=0; j<e.tail.successors.length; j++ ){
						new_edges.push({
							head: e.tail,
							tail: this.graph.getVertex(e.tail.successors[j])
						});
					}
				}
			}
			edges = new_edges;
		}
		var largestMove = 3;
		while( largestMove > 2 ){
			largestMove = 0;
			for( var i=0; i<this.vertices.length; i++ ){
				var v = this.vertices[i];
				if( v == this.startVertex || v == this.endVertex ){
					continue;
				}
				var x_old = Math.floor(( v.x2 + v.x1 ) / 2);
				var w = v.textNode.getBBox().width;
				var x_left = undefined, x_right = undefined;
				var xl = undefined, xr = undefined;
				for( var j=0; j<v.predecessors.length; j++ ){
					var vp = this.graph.getVertex(v.predecessors[j]);
					var xp = vp.x2;
					if( typeof x_left == "undefined" || xp > x_left ){
						x_left = xp;
						xl = vp;
					}
				}
				for( var j=0; j<v.successors.length; j++ ){
					var vs = this.graph.getVertex(v.successors[j]);
					var xs = vs.x1;
					if( typeof x_right == "undefined" || xs < x_right ){
						x_right = xs;
						xr = vs;
					}
				}
				var x_new = Math.floor(( x_left + x_right ) / 2);
				if( isNaN(x_new) ){
					x_new = x_old;
				}
				if( x_new != x_old ){
					v.x1 = x_new - w/2;
					v.x2 = v.x1 + w;
					if( largestMove < Math.abs(x_new-x_old) ){
						largestMove = Math.abs(x_new-x_old);
					}
				}
			}
		}
		for( var i=0; i<this.vertices.length; i++ ){
			this.vertices[i].textNode.attr({ x: this.vertices[i].x1 });
		}
	};

	this.overlap = function(x1_min,x1_max,x2_min,x2_max,y1_min,y1_max,y2_min,y2_max){
		if( x1_min >= x2_max || x1_max <= x2_min || y1_min >= y2_max || y1_max <= y2_min ){
			return false;
		}
		return true;
	}

	this.visualize = function(div,sid){
//return;
		var qtips = $('.qtip');
		for( var i=0; i<qtips.length; i++ ){
			$(qtips[i]).remove();
		}
		var sal = this;
		var overlapDir = function(node,south){
			var overlapD = true;
			var k = 0;
			var x1 = node.x1;
			var x2 = node.x2;
			var y1 = node.y1;
			var y2 = node.y2;
			while( overlapD && k < 10 ){
				k++;
				overlapD = false;
				for( var i=0; i<layout.length; i++ ){
					var v = layout[i];
					if( sal.overlap(x1,x2,v.x1,v.x2,y1,y2,v.y1,v.y2) ){
						overlapD = true;
						if( south ){
							y1 += ( v.y2 - v.y1 ) + gap;
							y2 += ( v.y2 - v.y1 ) + gap;
						}
						else {
							y1 -= ( v.y2 - v.y1 ) + gap;
							y2 -= ( v.y2 - v.y1 ) + gap;
						}
					}
				}
			}
			return {
				y1: y1,
				y2: y2
			};
		}
		var layerHeights = [];
		var getY = function(node,s,e){			
			var ys = overlapDir(node,true);
			var yn = overlapDir(node,false);
			var c_ys = ( ys.y1 + ys.y2 ) / 2;
			var c_yn = ( yn.y1 + yn.y2 ) / 2;
			var dist = function(x1,x2,y1,y2){
				return Math.sqrt( (x1-x2)*(x1-x2) + (y1-y2)*(y1-y2) );
			}
			var ds = dist(s.x2,node.x1,(s.y1+s.y2)/2,c_ys) + dist(e.x1,node.x2,(e.y1+e.y2)/2,c_ys);
			var dn = dist(s.x2,node.x1,(s.y1+s.y2)/2,c_yn) + dist(e.x1,node.x2,(e.y1+e.y2)/2,c_yn);
			if( ds > dn ){
				return c_yn;
			}
			return c_ys;
		}
		var paths = this.getPaths(sid);
		var widths = [], heights = [];
		$("#"+div).empty();
		var r = Raphael(div,"2500","500");
		this.paper = r;
		var width = r.canvas.clientWidth;
		var bg = r.rect(0, 0, width, 500, 5).attr({fill: "white", stroke: "none"});
		var x, y = 1000, gap = 20;
		var on;
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
		var dragLock = false, click = true;
		var dragNode = function(evt,node,vertex){
			var startPos = getMousePosition(evt);
			var nodeX1 = vertex.x1;
			var nodeX2 = vertex.x2;
			var nodeY1 = vertex.y1;
			var nodeY2 = vertex.y2;
			var mergeNode = false, rec1, rec2;
			document.onmouseup = function(){
				if(document.selection && document.selection.empty) {
					document.selection.empty();
				}
				else if(window.getSelection){
					var sel = window.getSelection();
					sel.removeAllRanges();
				}
				document.onmousemove = null;
				document.onmouseup = null;
				dragLock = false;
				if( mergeNode ){
					var g_test = sal.graph.clone();
					g_test.mergeVertices(mergeNode,vertex,globalAdjacent);
					if( !g_test.isAcyclic() ){
						alert('Invalid Merge!');
						sal.visualize(div,sid);
						return;
					}
					var newVertex = sal.graph.mergeVertices(mergeNode,vertex,globalAdjacent);
//console.info(sal.graph);
//console.info(blub);
					for( var i=0; i<sal.sentencePaths.length; i++ ){
						var p = sal.sentencePaths[i];
						for( var j=0; j<p.length; j++ ){
							if( p[j] == vertex || p[j] == mergeNode ){
								p[j] = newVertex;
							}
						}
					}					
					for( var i=0; i<sal.sentencePaths.length; i++ ){
						var p = sal.sentencePaths[i];
						for( var j=0; j<p.length-1; j++ ){
							if( p[j] == p[j+1] ){
								p.splice(j,1);
								break;
							}
						}
					}					
					sal.visualize(div,sid);
				}
			}
			document.onmousemove = function(e){
				var qtips = $('.qtip');
				for( var i=0; i<qtips.length; i++ ){
					$(qtips[i]).hide();
				}
				click = false;
				dragLock = node;
				if(document.selection && document.selection.empty) {
					document.selection.empty();
				}
				else if(window.getSelection){
					var sel = window.getSelection();
					sel.removeAllRanges();
				}
				var pos = getMousePosition(e);
				vertex.x1 = nodeX1+pos.left-startPos.left;
				vertex.x2 = nodeX2+pos.left-startPos.left;
				vertex.y1 = nodeY1+pos.top-startPos.top;
				vertex.y2 = nodeY2+pos.top-startPos.top;
				vertex.textNode.attr({ x: vertex.x1, y: ( vertex.y1 + vertex.y2 )/2 });
				for( var i=0; i<sal.connections.length; i++ ){
					$(sal.connections[i].node).remove();
				}
				sal.drawConnections(node,vertex,r);
				if( mergeNode ){
					$(rec1.node).remove();
					$(rec2.node).remove();
				}
				mergeNode = false;
				var d = 0;
				for( var i=0; i<sal.vertices.length; i++ ){
					var v1 = vertex;
					var v2 = sal.vertices[i];
					if( v1 != v2 && sal.overlap(v1.x1,v1.x2,v2.x1,v2.x2,v1.y1,v1.y2,v2.y1,v2.y2) ){
						var x1 = (v1.x1+v1.x2)/2;
						var x2 = (v2.x1+v2.x2)/2;
						var y1 = (v1.y1+v1.y2)/2;
						var y2 = (v2.y1+v2.y2)/2;
						var dist = Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
						if( !mergeNode || mergeNode && dist < d ){
							mergeNode = v2;
							d = dist;
						}
					}
				}
				if( mergeNode ){
					var color;
					var g_test = sal.graph.clone();
					g_test.mergeVertices(mergeNode,vertex,globalAdjacent);
//console.info(g_test);
//console.info(blub);
					g_test.isAcyclic() ? color = "#99E6FF" : color = "#FF8AA7";
					rec1 = r.rect(mergeNode.x1,mergeNode.y1,mergeNode.x2-mergeNode.x1, mergeNode.y2-mergeNode.y1, 5).attr({fill: color, stroke: "none", "fill-opacity": 0.5 });
					rec2 = r.rect(vertex.x1,vertex.y1,vertex.x2-vertex.x1, vertex.y2-vertex.y1, 5).attr({fill: "#99E6FF", stroke: "none", "fill-opacity": 0.5 });
					mergeNode.textNode.toFront();
					vertex.textNode.toFront();
				}
			}
		}
		var createBranch = function(vertex,sourceId){
			var sp = sal.sentencePaths[sourceId];
			var token = "";
			for( var i=0; i<vertex.sources.length; i++ ){
				if( vertex.sources[i].sourceId == sourceId ){
					token = vertex.sources[i].token;
					vertex.sources.splice(i,1);
					break;
				}
			}
			var v = new Vertex(sal.graph,SentenceAlignerProperties.getVertexIndex(),token);
			v.sources.push({
				sourceId: sourceId,
				token: token
			});
			var id = undefined;
			for( var i=0; i<sp.length; i++ ){
				if( vertex == sp[i] ){
					id = i;
				}
			}
			var prev = false, next = false;
			for( var i=0; i<sal.sentencePaths.length; i++ ){
				if( i != sourceId ){
					for( var j=0; j<sal.sentencePaths[i].length; j++ ){
						if( sal.sentencePaths[i][j] == vertex ){
							if( sal.sentencePaths[i][j-1] == sp[id-1] ){
								prev = true;
							}
							if( sal.sentencePaths[i][j+1] == sp[id+1] ){
								next = true;
							}
						}
					}
				}
			}
			if( !prev ){
				sp[id-1].removeSuccessor(vertex.index);
				vertex.removePredecessor(sp[id-1].index);
			}
			if( !next ){
				sp[id+1].removePredecessor(vertex.index);
				vertex.removeSuccessor(sp[id+1].index);
			}
			v.addPredecessor(sp[id-1].index);
			sp[id-1].addSuccessor(v.index);
			v.addSuccessor(sp[id+1].index);
			sp[id+1].addPredecessor(v.index);
			sp[id] = v;
			vertex.count--;
			sal.graph.addVertex(v);
			sal.visualize(div,sid);
		}
		var setTooltip = function(node,vertex,paper){
			var attachedLinks = false;
			node.connections = [];
			$(node).mouseenter(function(){
				if( dragLock ){
					return;
				}
				for( var i=0; i<sal.gc.length; i++ ){
					$(sal.gc[i].node).remove();
				}
				for( var i=0; i<sal.connections.length; i++ ){
					$(sal.connections[i].node).remove();
				}
				sal.drawConnections(node,vertex,paper);
				on.attr({ 'x': (vertex.x2+vertex.x1)/2 });
				if( vertex.count > 1 ){
					on.attr('text',vertex.token+': '+vertex.count+' occurences');
				}
				else {
					on.attr('text',vertex.token+': '+vertex.count+' occurence');
				}
			});
			$(node).mouseleave(function(){
				if( dragLock ){
					return;
				}
				for( var i=0; i<sal.connections.length; i++ ){
					$(sal.connections[i].node).remove();
				}
				sal.generalConnections();
				on.attr('text','');
			});
			$(node).mousedown(function(evt){
				dragNode(evt,node,vertex);
			});
			var tiptext = "<table>";
			for( var i=0; i<vertex.sources.length; i++ ){
				tiptext += "<tr>";
				tiptext += "<td style='padding-top:5px;padding-bottom:5px;font-size: 12px;text-align:right;color:"+SentenceAlignerProperties.getColor(vertex.sources[i].sourceId)+";'>"+SentenceAlignerProperties.sources[vertex.sources[i].sourceId]+"</td>";
				tiptext += "<td style='font-size: 12px;padding-left:10px;color:"+SentenceAlignerProperties.getColor(vertex.sources[i].sourceId)+";'>"+vertex.sources[i].token+"</td>";
				if( vertex.sources.length > 1 ){
					tiptext += "<td style='padding-left:10px;'><div title='Remove token and create new branch!' name="+vertex.sources[i].sourceId+" class='unlink unlink"+vertex.index+"'/></td>";
				}
				tiptext += "</tr>";
			}
			tiptext += "</table>";
			var qtip = $(node).qtip({
				content: {	
					text: tiptext
				},
				style: {
					tip: true,
					border: { width: 0, radius: 4 }
				},
				position: {
					corner: {
						tooltip: "topMiddle",
						target: "bottomMiddle"
					}
				},
				show: { 
					when: 'click', 
					solo: true
				},
				hide: 'unfocus'
			});
			$(node).click(function(evt){
				var links = $('.unlink'+vertex.index);
				if( links.length > 0 && !attachedLinks ){
					for( var i=0; i<links.length; i++ ){
						$(links[i]).click(function(){
							createBranch(vertex,$(this).attr('name'));
						});
					}
					attachedLinks = true;
				}
			});
/*
			paper.freeTransform(tn, { rotate: false, scale: false, drag: true, size: 0 }, function(ft, events){
				for( var i=0; i<sal.gc.length; i++ ){
					$(sal.gc[i].node).remove();
				}
			        sal.drawConnections(node,vertex,paper);
console.info(tn.node);
			});
			/*
			$(node).click(function(){
				if( node.connections.length == 0 ){

				}
				for( var i=0; i<sal.connections.length; i++ ){
					$(sal.connections[i].node).remove();
				}
			});
			*/
		}
		var layout = [];
		for( var i=0; i<paths.length; i++ ){
			x = 0;
			var j=0, k=paths[i].length;
			if( i > 0 ){
				j++;
				k--;
				//x = paths[i][0].x2 + gap;
				//y = 100 + i*20;
			}
			var width = 0, height = 0;
			var sizes = [ 12, 17, 23, 30, 38, 47, 57 ];
			for( j; j<k; j++ ){
				var v = paths[i][j];
				var fs = 10 + 4*v.count;
				var tn = r.text(0, y, v.token).attr({font: fs+"px Droid Sans, Helvetica, sans-serif"}).attr({color:"#3E576F",fill:"#3E576F","text-anchor":"start","cursor":"move"});
				v.x1 = x;
				var widthN = tn.getBBox().width;
				var heightN = tn.getBBox().height;
				v.x2 = widthN + x;
				v.y1 = y - heightN/2;
				v.y2 = y + heightN/2;
				v.textNode = tn;
				width += widthN;
				if( j > 0 ){
					width += gap;
				}
				if( heightN > height ){
					height = heightN;
				}
				layout.push(v);
				setTooltip(tn.node,v,r);
			}
			widths.push(width + 2 * gap);
			heights.push(height);
		}
		this.setXFlow();
		layout = [];
		var layers = [];
		var lh = 0;
		for( var i=0; i<paths[0].length; i++ ){
			layout.push(paths[0][i]);
			paths[0][i].layer = 0;
			if( paths[0][i].textNode.getBBox().height > lh ){
				lh = paths[0][i].textNode.getBBox().height;
			}
		}
		layers.push({
			index: 0,
			height: lh,
			vertices: paths[0]
		});
		var getLayer = function(layer0,layerN,v,path){
			var destinationS = layerN;
			if( Math.abs(layer0) > Math.abs(layerN) ){
				destinationS = layer0;
			}
			var add = 0;
			var switcher = false;
			if( destinationS == 0 ){
				destinationS = 1;
				switcher = true;
			}
			var destinationE = destinationS;
			do {
				var nospace = false;
				for( var i=0; i<layout.length; i++ ){
					if( layout[i].layer == destinationE ){
						if( !( layout[i].x1 > v.x2 || v.x1 > layout[i].x2 ) ){
							if( switcher ){
								add = -1;
								destinationS = 0;
								switcher = false;
							}
							else if( add > 0 ){
								add = add*-1;
							}
							else {
								add = add*-1+1;
							}
							nospace = true;							
							break;
						}
					}
				}
				destinationE = destinationS + add;
			}
			while( nospace );
			var layer = undefined;
			for( var i=0; i<layers.length; i++ ){
				if( destinationE == layers[i].index ){
					layer = layers[i];
					break;
				}
			}
			if( typeof layer == "undefined" ){
				layer = {
					index: destinationE,
					height: 0,
					vertices: []
				};
				layers.push(layer);
			}
			if( Math.abs(v.y2-v.y1) > layer.height ){
				layer.height = Math.abs(v.y2-v.y1);
			}
			layer.vertices = layer.vertices.concat(path.slice(1,path.length-1));
			return destinationE;
		}
		for( var i=1; i<paths.length; i++ ){
			var path = paths[i];
			var s = path[0];
			var e = path[path.length-1];
			var s1 = path[1];
			var e1 = path[path.length-2];
			var vertex = new Vertex();
			vertex.x1 = s1.x1 - gap;
			vertex.x2 = e1.x2 + gap;
			vertex.y1 = (s.y1 + s.y2)/2 - heights[i]/2;
			vertex.y2 = (s.y1 + s.y2)/2 + heights[i]/2;
			//var y = getY(vertex,path[0],path[path.length-1]);
			var y = getLayer(s.layer,e.layer,vertex,path);
			for( var j=1; j<path.length-1; j++ ){
				/*
				var heightN = path[j].textNode.getBBox().height;
				path[j].textNode.attr({ y: y });
				path[j].y1 = y - heightN / 2;
				path[j].y2 = y + heightN / 2;
				*/
				path[j].layer = y;
				layout.push(path[j]);
			}
		}
		gap = 10;
		for( var i=1; i<layers.length; i++ ){
			var y = 1000;
			if( layers[i].index > 0 ){
				for( var j=0; j<layers.length; j++ ){
					if( layers[j].index < layers[i].index && layers[j].index > -1 ){
						y += layers[j].height + gap;
					}
				}
				y += (layers[i].height + gap)/2;
			}
			if( layers[i].index < 0 ){
				for( var j=0; j<layers.length; j++ ){
					if( layers[j].index > layers[i].index && layers[j].index < 1 ){
						y -= layers[j].height + gap;
					}
				}
				y -= (layers[i].height + gap)/2;
			}
			for( var j=0; j<layers[i].vertices.length; j++ ){
				var heightN = layers[i].vertices[j].textNode.getBBox().height;
				layers[i].vertices[j].textNode.attr({ y: y });
				layers[i].vertices[j].y1 = y - heightN / 2;
				layers[i].vertices[j].y2 = y + heightN / 2;
			}
		}
		var x_min = 1000000, x_max = 0, y_min = 1000000, y_max = 0;
		for( var i=0; i<layout.length; i++ ){
			var v = layout[i];
			if( v.x1 < x_min ){
				x_min = v.x1;
			}
			if( v.x2 > x_max ){
				x_max = v.x2;
			}
			if( v.y1 < y_min ){
				y_min = v.y1;
			}
			if( v.y2 > y_max ){
				y_max = v.y2;
			}
		}
		var x_diff = x_min - 40;
		var y_diff = y_min - 40;		
		for( var i=0; i<layout.length; i++ ){
			var v = layout[i];
			v.x1 = v.x1 - x_diff;
			v.x2 = v.x2 - x_diff;
			v.y1 = v.y1 - y_diff;
			v.y2 = v.y2 - y_diff;
			v.textNode.attr({ x: v.x1, y: ( v.y1 + v.y2 )/2 });
		}
		var w = x_max - x_min + 40;
		var h = y_max - y_min + 100;
		r.setSize((w+20)+"px",h+"px");
		bg.attr({ 'height': h, 'width': w+20 });
		var on = r.text(w/2, h-20,'').attr({font: "16px Droid Sans, Helvetica, sans-serif"}).attr({color:"#3E576F",fill:"#3E576F","text-anchor":"middle"});
		$(this.startVertex.textNode.node).remove();
		$(this.endVertex.textNode.node).remove();
		this.generalConnections();
	}

	this.printVertices = function(){
		for( var i=0; i<this.vertices.length; i++ ){
			var v = this.vertices[i];
			for( var j=0; j<v.successors.length; j++ ){
				console.info(v.id+" ---> "+v.successors[j].id,v.token+" ---> "+v.successors[j].token);
//				console.info(v.token+" ---> "+v.successors[j].token);
			}
			/*
			for( var j=0; j<v.predecessors.length; j++ ){
				console.info(v.predecessors[j].token+" ---> "+v.token);
			}
			*/
			console.info("--------------------------------------------------");
		}
	}

};
