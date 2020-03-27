function Vertex(token,position) {
	this.token = token;
	this.position = position;
	this.positions = [position];
	this.successors = [];
	this.predecessors = [];
	this.count = 1;
	this.x1;
	this.x2;
	this.y1;
	this.y2;
	this.traced = false;
	this.linked = true;
	this.sources = [];
	this.index = ReuseViz.getVertexIndex();
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
		this.positions.push(position);
		this.count++;
		var p = 0;
		for( var i=0; i<this.positions.length; i++ ){
			p += this.positions[i];
		}
		this.position = p/this.positions.length;
	}
	this.merge = function(vertex){
		this.sources = this.sources.concat(vertex.sources);
		for( var i=0; i<vertex.successors.length; i++ ){
			this.addSuccessor(vertex.successors[i]);
			vertex.successors[i].addPredecessor(this);
			vertex.successors[i].removePredecessor(vertex);
		}
		for( var i=0; i<vertex.predecessors.length; i++ ){
			this.addPredecessor(vertex.predecessors[i]);
			vertex.predecessors[i].addSuccessor(this);
			vertex.predecessors[i].removeSuccessor(vertex);
		}
		this.count += vertex.count;
	}
}

function Edge(head,tail) {
	this.head = head;
	this.tail = tail;
}

function SentenceAligner(){

	var threshold = 0.1;

	this.startVertex = new Vertex('>>');
	this.endVertex = new Vertex('<<');
	this.startVertex.id = 'first';
	this.endVertex.id = 'last';
	this.vertices = [];
	this.sentencePaths = [];
	this.connections = [];

	this.alignSentences = function(verses){
		if( ReuseViz.algo == 'pair' ){
			this.setSentences(verses);
		}
		else {
			for( var i=0; i<verses.length; i++ ){
				this.addSentence(verses[i],i);
			}
		}
	}

	this.setSentences = function(sentences){
		var words = [];
		var tokenized = [];
		for( var i=0; i<sentences.length; i++ ){
			var sentence = this.clean(sentences[i]);
			var tokens = sentence.split(" ");
			var t = [];
			for( var j=0; j<tokens.length; j++ ){
				var word = {
					word: tokens[j],
					sid: i,
					wid: j,
					gid: words.length
				};
				words.push(word);
				t.push(word);
			}
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
		var words = [];
		var conflict = function(wordArray){
			var sids = [];
			for( var i=0; i<sentences.length; i++ ){
				sids.push(false);
			}
			for( var i=0; i<wordArray.length; i++ ){
				if( sids[wordArray[i].sid] ){
					return true;
				}
				sids[wordArray[i].sid] = true;
			}
			for( var i=0; i<words.length; i++ ){
				var leftOf = false, rightOf = false;
				for( var j=0; j<words[i].length; j++ ){
					var w1 = words[i][j];
					for( var k=0; k<wordArray.length; k++ ){
						var w2 = wordArray[k];
						if( w1.sid == w2.sid && w1.wid < w2.wid ){
							rightOf = true;
						}
						if( w1.sid == w2.sid && w1.wid > w2.wid ){
							leftOf = true;
						}
					}
				}
				if( leftOf && rightOf ){
					return true;
				}
			}
			return false;
		}
		for( var i=0; i<pairs.length; i++ ){
			var w1 = pairs[i].pair.w1;
			var w2 = pairs[i].pair.w2;
			var m1 = undefined, m2 = undefined;
			for( var j=0; j<words.length; j++ ){
				for( var k=0; k<words[j].length; k++ ){
					if( words[j][k] == w1 ){
						m1 = j;
					}
					if( words[j][k] == w2 ){
						m2 = j;
					}
				}
			}
			if( typeof m1 =='undefined' && typeof m2 =='undefined' ){
				var newArray = [w1,w2];
				if( !conflict(newArray) ){
					words.push(newArray);
				}
			}
			else if( typeof m1 =='undefined' ){
				var oldArray = words[m2];
				var newArray = words[m2].concat([w1]);
				words.splice(m2,1);
				if( conflict(newArray) ){
					words.push(oldArray);
				}
				else {
					words.push(newArray);
				}
			}
			else if( typeof m2 =='undefined' ){
				var oldArray = words[m1];
				var newArray = words[m1].concat([w2]);
				words.splice(m1,1);
				if( conflict(newArray) ){
					words.push(oldArray);
				}
				else {
					words.push(newArray);
				}
			}
			else if( m1 != m2 ){
				var oldArray1 = words[m1];
				var oldArray2 = words[m2];
				var newArray = words[m1].concat(words[m2]);
				if( m1 > m2 ){
					words.splice(m1,1);
					words.splice(m2,1);
				}
				else {
					words.splice(m2,1);
					words.splice(m1,1);
				}
				if( conflict(newArray) ){
					words.push(oldArray1);
					words.push(oldArray2);
				}
				else {
					words.push(newArray);
				}
			}
		}
		for( var i=0; i<words.length; i++ ){
			for( var j=0; j<words[i].length; j++ ){
				nodes[words[i][j].gid] = words[i];
			}
		}
		this.vertices.push(this.startVertex);
		for( var k=0; k<tokenized.length; k++ ){
			var tokens = tokenized[k];
			var coverage = [];
			for( var j=0; j<tokens.length; j++ ){
				coverage.push(assignments[tokens[j].gid]);
			}
			var vertices = [];
			var sp = [];
			for( var i=1; i<coverage.length; i++ ){
				if( coverage[i] && coverage[i-1] ){
					var found = false;
					for( var j=0; j<coverage[i-1].successors.length; j++ ){
						if( coverage[i-1].successors[j] == coverage[i] ){
							found = true;
							break;
						}
					}
					if( !found ){
						coverage[i-1].addSuccessor(coverage[i]);
						coverage[i].addPredecessor(coverage[i-1]);
					}
				}
			}
			for( var i=0; i<coverage.length; i++ ){
				if( coverage[i] ){
					sp.push(coverage[i]);
					coverage[i].increase(i);
					coverage[i].sources.push({
						sourceId: k,
						token: tokens[i].word
					});
					vertices.push(coverage[i]);
					if( i > 0 && !coverage[i-1] ){
						vertices[i-1].addSuccessor(coverage[i]);
						coverage[i].addPredecessor(vertices[i-1]);
					}
				}
				else {
					var v = new Vertex(tokens[i].word,i/tokens.length);
					v.sources.push({
						sourceId: k,
						token: tokens[i].word
					});
					var merged = nodes[tokens[i].gid];
					for( var j=0; j<merged.length; j++ ){
						assignments[merged[j].gid] = v;
					}
					v.id = this.vertices.length;
					sp.push(v);
					vertices.push(v);
					this.vertices.push(v);
					if( i > 0 ){
						vertices[i-1].addSuccessor(v);
						v.addPredecessor(vertices[i-1]);
					}
					else {
						this.startVertex.addSuccessor(v);
						v.addPredecessor(this.startVertex);
					}
					if( i == tokens.length-1 ){
						v.addSuccessor(this.endVertex);						
						this.endVertex.addPredecessor(v);
					}
				}
			}
			this.sentencePaths.push(sp);
		}
		this.setPosIds();
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
				for( var j=newPaths.length; j>0; j-- ){
					var path2 = newPaths[j-1];
					var lNode2 = path2[path2.length-1];
					if( lNode1.w2 == lNode2.w2 && path1.length != path2.length ){
						if( path1.length > path2.length ){
							newPaths.splice(j-1,1);
						}
						else {
							found = true;
						}
					}
				}
				if( !found ){
					newPaths.push(path1);
				}
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

	this.trace = function(tokens,position){
		for( var i=0; i<this.vertices.length; i++ ){
			if( this.vertices[i].token == tokens[0] && Math.abs(this.vertices[i].position-position) <= threshold ){
				var found = true;
				var successors = this.vertices[i].successors;
				var vs = [this.vertices[i]];
				for( var j=1; j<tokens.length; j++ ){
					found = false;
					for( var k=0; k<successors.length; k++ ){
						if( successors[k].token == tokens[j] ){
							vs.push(successors[k]);
							successors = successors[k].successors;
							found = true;
						}
					}
					if( !found ){
						break;
					}
				}
				if( found ){
					return vs;
				}
			}
		}
		return false;
	};

	this.addSentence = function(sentence,sid){
		sentence = this.clean(sentence);
		if( this.vertices.length == 0 ){
			var tokens = sentence.split(" ");
			var sp = [];
			for( var i=0; i<tokens.length; i++ ){
				var v = new Vertex(tokens[i],i/tokens.length);
				v.sources.push({
					sourceId: sid,
					token: tokens[i]
				});
				sp.push(v);
				this.vertices.push(v);
				if( i > 0 ){
					this.vertices[i-1].addSuccessor(v);
					v.addPredecessor(this.vertices[i-1]);
				}
				else {
					this.startVertex.addSuccessor(v);
					v.addPredecessor(this.startVertex);
				}
				if( i == tokens.length-1 ){
					v.addSuccessor(this.endVertex);
					this.endVertex.addPredecessor(v);
				}
			}
			this.vertices.splice(0,0,this.startVertex);
			this.sentencePaths.push(sp);
		}
		else {
			var tokens = sentence.split(" ");
			var coverage = [];
			for( var i=0; i<tokens.length; i++ ){
				coverage.push(false);
			}
			if( ReuseViz.algo == 'fence' ){
				var matches = this.bestMatch(tokens);
				for( var i=0; i<matches.length; i++ ){				
					coverage[matches[i].index] = matches[i].vertex;
				}
			}
			else if( ReuseViz.algo == 'trigram' ){
				for( var i=0; i<tokens.length; i++ ){
					var tl = tokens.length-i;
					for( var j=0; j<tokens.length-tl+1; j++ ){
						var ts = tokens.slice(j,j+tl);
						if( tl < 3 ){
							continue;
						}
						if( tl == 1 && ReuseViz.isStopword(ts[0]) ){
							continue;
						}
						if( coverage[j] || coverage[j+tl-1] ){
							continue;
						}
						var traced = this.trace(ts,j/tokens.length);
						if( traced ){						
							for( var k=j; k<j+tl; k++ ){
								coverage[k] = traced[k-j];
							}						
						}
					}
				}
			}
			var vertices = [];
			var sp = [];
			for( var i=1; i<coverage.length; i++ ){
				if( coverage[i] && coverage[i-1] ){
					var found = false;
					for( var j=0; j<coverage[i-1].successors.length; j++ ){
						if( coverage[i-1].successors[j] == coverage[i] ){
							found = true;
							break;
						}
					}
					if( !found ){
						coverage[i-1].addSuccessor(coverage[i]);
						coverage[i].addPredecessor(coverage[i-1]);
					}
				}
			}
			for( var i=0; i<coverage.length; i++ ){
				if( coverage[i] ){
					sp.push(coverage[i]);
					coverage[i].increase(i);
					coverage[i].sources.push({
						sourceId: sid,
						token: tokens[i]
					});
					vertices.push(coverage[i]);
					if( i > 0 && !coverage[i-1] ){
						vertices[i-1].addSuccessor(coverage[i]);
						coverage[i].addPredecessor(vertices[i-1]);
					}
				}
				else {
					var v = new Vertex(tokens[i],i/tokens.length);
					v.sources.push({
						sourceId: sid,
						token: tokens[i]
					});
					sp.push(v);
					vertices.push(v);
					this.vertices.push(v);
					if( i > 0 ){
						vertices[i-1].addSuccessor(v);
						v.addPredecessor(vertices[i-1]);
					}
					else {
						this.startVertex.addSuccessor(v);
						v.addPredecessor(this.startVertex);
					}
					if( i == tokens.length-1 ){
						v.addSuccessor(this.endVertex);						
						this.endVertex.addPredecessor(v);
					}
				}
			}
			this.sentencePaths.push(sp);
		}
		this.setPosIds();
	}

	this.setPosIds = function(){
		for( var i=0; i<this.vertices.length; i++ ){
			this.vertices[i].posId = -1;
		}
		this.startVertex.posId = 0;
		var edges = [];
		for( var i=0; i<this.startVertex.successors.length; i++ ){
			edges.push({
				head: this.startVertex,
				tail: this.startVertex.successors[i]
			});
		}
		while( edges.length > 0 ){
			var new_edges = [];
			for( var i=0; i<edges.length; i++ ){
				var e = edges[i];
				if( e.tail.posId <= e.head.posId ){
					e.tail.posId = e.head.posId + 1;
					for( var j=0; j<e.tail.successors.length; j++ ){
						new_edges.push({
							head: e.tail,
							tail: e.tail.successors[j]
						});
					}
				}
			}
			edges = new_edges;
		}
	}

	this.bestMatch = function(tokens){
		var sal = this;
		var possibleAlignments = [];
		var getMatches = function(token,start){
			var matches = [];
			for( var i=0; i<sal.vertices.length; i++ ){
				if( token == sal.vertices[i].token && sal.vertices[i].posId > start.posId ){
					matches.push(sal.vertices[i]);
				}
			}
			return matches;
		}
		var getMatchesOld = function(token,start){
			var matches = [];
			var s = start.successors;
			while( s.length > 0 ){
				var news = [];
				for( var i=0; i<s.length; i++ ){
					if( token == s[i].token ){
						matches.push(s[i]);
					}
					for( var j=0; j<s[i].successors.length; j++ ){ 
						var found = false;
						for( var k=0; k<news.length; k++ ){ 
							if( news[k] == s[i].successors[j] ){
								found = true;
								break;
							}
						}
						if( !found ){
							news.push(s[i].successors[j]);
						}
					}
				}
				s = news;
			}
			return matches;
		}
		for( var i=0; i<tokens.length; i++ ){
			var pa = [];
			for( var j=0; j<possibleAlignments.length; j++ ){
				var p = possibleAlignments[j];
				var m = getMatches(tokens[i],p[p.length-1].vertex);
				for( var k=0; k<m.length; k++ ){
					pa.push(p.concat([{ index: i, vertex: m[k]}]));
				}
			}
			for( var j=0; j<this.vertices.length; j++ ){
				this.vertices[j].mlib = undefined;
			}
			var m = getMatches(tokens[i],this.startVertex);
			for( var k=0; k<m.length; k++ ){
				pa.push([{ index: i, vertex: m[k]}]);
			}
			possibleAlignments = possibleAlignments.concat(pa);
			var relevantAlignments = [];
			for( var j=0; j<possibleAlignments.length; j++ ){
				var p = possibleAlignments[j];
				var found = false;
				for( var k=0; k<relevantAlignments.length; k++ ){
					var r = relevantAlignments[k];
					if( r[r.length-1].vertex == p[p.length-1].vertex ){
						if( p.length > r.length ){
							relevantAlignments[k] = p;
						}
						if( p.length == r.length ){
							relevantAlignments.push(p);
						}
						found = true;
						break;
					}
				}
				if( !found ){
					relevantAlignments.push(p);
				}
			}
			possibleAlignments = relevantAlignments;
		}
		var sortBySize = function(s1,s2){
			if( s1.length > s2.length ){
				return -1;
			}
			return 1;
		}
		possibleAlignments.sort(sortBySize);
		//console.info(possibleAlignments);
		return possibleAlignments[0];
	};

		this.getMaxFlow = function(){
			var s = this.startVertex;
			var e = this.endVertex;
			var paths = [];
			var completedPaths = [];
			for( var i=0; i<s.successors.length; i++ ){
				paths.push([s.successors[i]]);
			}
			var traverse = true;
			while( traverse ){
				traverse = false;
				var new_paths = [];
				for( var i=0; i<paths.length; i++ ){
					var path = paths[i];
					var es = path[path.length-1];
					for( var j=0; j<es.successors.length; j++ ){
						if( es.successors[j] == e ){
							completedPaths.push(path);
						}
						else if( es.successors[j] != this.endVertex ){
							traverse = true;
							new_paths.push(path.concat([es.successors[j]]));
						}
					}
				}
				paths = new_paths;
			}
			var maxPath;
			var strength = 0;
			for( var i=0; i<completedPaths.length; i++ ){
				var p = completedPaths[i];
				var st = 0;
				for( var j=0; j<p.length; j++ ){
					st += p[j].count;
				}
				if( st > strength ){
					strength = st;
					maxPath = p;	
				}
			}
			maxPath.splice(0,0,s);
			maxPath.push(e);
			for( var i=0; i<maxPath.length; i++ ){
				maxPath[i].traced = true;
			}
			return maxPath;
		};

	this.strongestPath = function(s){
		var strength = 0;
		var path = [];
		for( var i=0; i<s.successors.length; i++ ){
			var pi = [];
			var si = 0;
			var vertex = s.successors[i];
			var vs = vertex.successors;
			pi.push(vertex);
			si += vertex.count;
			while( vs.length > 0 ){
				var lc = vs[0].count;
				var v = vs[0];
				for( var j=1; j<vs.length; j++ ){
					if( vs[j].count > lc ){
						lc = vs[j].count;
						v = vs[j];							
					}
				}
				pi.push(v);
				if( !v.traced ){
					si += lc;
					vs = v.successors;
				}
				else {
					vs = [];
				}
			}
			if( si > strength ){
				strength = si;
				path = pi;
			}
		}
		path.splice(0,0,s);
		for( var i=0; i<path.length; i++ ){
			path[i].traced = true;
		}
		return path;
	}

	this.strongestShortestPath = function(s){
		var strength = 0, length = 1000000;
		var path;
		for( var i=0; i<s.successors.length; i++ ){
			var pi = [s];
			var si = s.count;
			var li = 1;
			var vertex = s.successors[i];
			pi.push(vertex);
			if( !vertex.traced ){
				var vs = vertex.successors;
				si += vertex.count;
				while( vs.length > 0 ){
					var lc = vs[0].count;
					var v = vs[0];
					for( var j=1; j<vs.length; j++ ){
						if( vs[j].count > lc ){
							lc = vs[j].count;
							v = vs[j];							
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
//		var path = this.getMaxFlow();
//		var paths = [path];
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
						if( !v.successors[k].traced ){
							var p = this.strongestShortestPath(v.successors[k]);
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
		var y1o = (v1.y1+v1.y2)/2;
		var y2o = (v2.y1+v2.y2)/2;
		var y1 = (v1.y1+v1.y2)/2 + sy1;
		var y2 = (v2.y1+v2.y2)/2 + sy2;	
		var path = "M "+v1.x2+" "+y1+" ";
		if( v1.x2 > v2.x1 ){
			var x1 = v1.x2;
			var x2 = v2.x1;
			var x12 = v1.x2+20;
			var x34 = v2.x1-20;
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
		else if( v1.x2 > v2.x1 - 20 ){
			var x1 = v1.x2;
			var x2 = v2.x1;
			var x12 = (v1.x2+v2.x1)/2;
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
					if( this.overlap(v1.x2,v2.x1,this.vertices[k].x1,this.vertices[k].x2,y1o,y2o,this.vertices[k].y1,this.vertices[k].y2) ){
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
					var x1 = v1.x2;
					var x12 = v1.x2+10;
					var x2 = v1.x2+20;
					var y1 = y1;
					var y2 = y_min - 10 + sy1;
					var y12 = (y1+y2)/2;
					var xa = v2.x1-20;
					var xab = v2.x1-10;
					var xb = v2.x1;
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
					path += "L "+v1.x2+" "+y1+" "+v2.x1+" "+y2+" ";
				}
			}
			else {
				var y12 = (y1+y2)/2;
				var x1, x2, x12;
				if( v2.x1 - v1.x2 == 20 ){
					x1 = v1.x2;
					x12 = v1.x2+10;
					x2 = v2.x1;
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
							if( this.overlap(v1.x2,v2.x1-20,this.vertices[k].x1,this.vertices[k].x2,y1,y1,this.vertices[k].y1,this.vertices[k].y2) ){
								overlap = true;
							}
						}
						if( !overlap ){
							path += "L "+v1.x2+" "+y1+" ";
							path += (v2.x1-20)+" "+y1+" ";
							x1 = v2.x1-20;
							x12 = v2.x1-10;
							x2 = v2.x1;
							path += "C "+x1+" "+y1+" ";
							path += x12+" "+y1+" ";
							path += x12+" "+y12+" ";
							path += "C "+x12+" "+y12+" ";
							path += x12+" "+y2+" ";
							path += x2+" "+y2+" ";
						}
						else {
							x1 = v1.x2;
							x12 = v1.x2+10;
							x2 = v1.x2+20;
							path += "C "+x1+" "+y1+" ";
							path += x12+" "+y1+" ";
							path += x12+" "+(y1+y12)/2+" ";
							path += "C "+x12+" "+(y1+y12)/2+" ";
							path += x12+" "+y12+" ";
							path += x2+" "+y12+" ";
							path += "L "+x2+" "+y12+" ";
							path += (v2.x1-20)+" "+y12+" ";
							x1 = v2.x1-20;
							x12 = v2.x1-10;
							x2 = v2.x1;
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
							if( this.overlap(v1.x2+20,v2.x1,this.vertices[k].x1,this.vertices[k].x2,y2,y2,this.vertices[k].y1,this.vertices[k].y2) ){
								overlap = true;
							}
						}
						if( !overlap ){
							x1 = v1.x2;
							x12 = v1.x2+10;
							x2 = v1.x2+20;
							path += "C "+x1+" "+y1+" ";
							path += x12+" "+y1+" ";
							path += x12+" "+y12+" ";
							path += "C "+x12+" "+y12+" ";
							path += x12+" "+y2+" ";
							path += x2+" "+y2+" ";
							path += "L "+x2+" "+y2+" ";
							path += v2.x1+" "+y2+" ";
						}
						else {
							x1 = v1.x2;
							x12 = v1.x2+10;
							x2 = v1.x2+20;
							path += "C "+x1+" "+y1+" ";
							path += x12+" "+y1+" ";
							path += x12+" "+(y1+y12)/2+" ";
							path += "C "+x12+" "+(y1+y12)/2+" ";
							path += x12+" "+y12+" ";
							path += x2+" "+y12+" ";
							path += "L "+x2+" "+y12+" ";
							path += (v2.x1-20)+" "+y12+" ";
							x1 = v2.x1-20;
							x12 = v2.x1-10;
							x2 = v2.x1;
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
		var pvis = this.paper.path(path).attr({stroke: ReuseViz.colors[i], "stroke-width": 4, "stroke-linecap": "round", "opacity": "0.5"});
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
					var pvis = this.paper.path(path).attr({stroke: ReuseViz.colors[i], "stroke-width": 3, "stroke-linecap": "round", "opacity": "0.5"});
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
				if( p[j] == this.endVertex ){
					continue;
				}
				var pvis = this.paper.path(this.generatePath(v,p[j],0,0)).attr({stroke: "#3E576F", "stroke-width": 3, "stroke-linecap": "round", "opacity": "0.5"});
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
				tail: this.startVertex.successors[i]
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
							tail: e.tail.successors[j]
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
				var x_old = ( v.x2 + v.x1 ) / 2;
				var w = v.textNode.getBBox().width;
				var x_left = undefined, x_right = undefined;
				var xl = undefined, xr = undefined;
				for( var j=0; j<v.predecessors.length; j++ ){
					var vp = v.predecessors[j];
					var xp = vp.x2;
					if( typeof x_left == "undefined" || xp > x_left ){
						x_left = xp;
						xl = vp;
					}
				}
				for( var j=0; j<v.successors.length; j++ ){
					var vs = v.successors[j];
					var xs = vs.x1;
					if( typeof x_right == "undefined" || xs < x_right ){
						x_right = xs;
						xr = vs;
					}
				}
				var x_new = ( x_left + x_right ) / 2;
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
/*
		var qtips = $('.qtip');
		for( var i=0; i<qtips.length; i++ ){
			$(qtips[i]).remove();
		}
*/
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
		var width = 2500;
		var bg = r.rect(0, 0, width, 500, 5).attr({fill: "white", stroke: "none"});
		var x, y = 250, gap = 20;
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
		var invalidMerge = function(v1,v2){
			var nodes = v1.successors;
			while( nodes.length > 0 ){
				var newNodes = [];
				for( var i=0; i<nodes.length; i++ ){
					if( nodes[i] == v2 ){
						return true;
					}
					newNodes = newNodes.concat(nodes[i].successors);
				}
				nodes = newNodes;
			}
			nodes = v2.successors;
			while( nodes.length > 0 ){
				var newNodes = [];
				for( var i=0; i<nodes.length; i++ ){
					if( nodes[i] == v1 ){
						return true;
					}
					newNodes = newNodes.concat(nodes[i].successors);
				}
				nodes = newNodes;
			}
			return false;
		}
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
					if( invalidMerge(vertex,mergeNode) ){
						alert('Invalid Merge!');
						sal.visualize(div,sid);
						return;
					}
					for( var i=0; i<sal.sentencePaths.length; i++ ){
						var p = sal.sentencePaths[i];
						for( var j=0; j<p.length; j++ ){
							if( p[j] == vertex ){
								p[j] = mergeNode;
							}
						}
					}
					mergeNode.merge(vertex);
					for( var i=0; i<sal.vertices.length; i++ ){
						if( sal.vertices[i] == vertex ){
							sal.vertices.splice(i,1);
							break;
						}
					}
					sal.visualize(div,sid);
				}
			}
			document.onmousemove = function(e){
				/*
				var qtips = $('.qtip');
				for( var i=0; i<qtips.length; i++ ){
					$(qtips[i]).hide();
				}
				*/
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
					invalidMerge(vertex,mergeNode) ? color = "#FF8AA7" : color = "#99E6FF";
					rec1 = r.rect(mergeNode.x1,mergeNode.y1,mergeNode.x2-mergeNode.x1, mergeNode.y2-mergeNode.y1, 5).attr({fill: color, stroke: "none", "fill-opacity": 0.5 });
					rec2 = r.rect(vertex.x1,vertex.y1,vertex.x2-vertex.x1, vertex.y2-vertex.y1, 5).attr({fill: "#99E6FF", stroke: "none", "fill-opacity": 0.5 });
					mergeNode.textNode.toFront();
					vertex.textNode.toFront();
				}
			}
		}
		var createBranch = function(vertex,sourceId){
			var sp = sal.sentencePaths[sourceId];
			var token;
			for( var i=0; i<vertex.sources.length; i++ ){
				if( vertex.sources[i].sourceId == sourceId ){
					token = vertex.sources[i].token;
					vertex.sources.splice(i,1);
					break;
				}
			}
			var v = new Vertex(token,vertex.position);
			v.sources.push({
				sourceId: sourceId,
				token: token
			});
			var id;
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
				sp[id-1].removeSuccessor(vertex);
				vertex.removePredecessor(sp[id-1]);
			}
			if( !next ){
				sp[id+1].removePredecessor(vertex);
				vertex.removeSuccessor(sp[id+1]);
			}
			v.addPredecessor(sp[id-1]);
			sp[id-1].addSuccessor(v);
			v.addSuccessor(sp[id+1]);
			sp[id+1].addPredecessor(v);
			sp[id] = v;
			vertex.count--;
			sal.vertices.push(v);
			sal.visualize(div,sid);
		}
		var setTooltip = function(node,vertex,paper){
			var attachedLinks = false;
			node.connections = [];
			$(node).mouseenter(function(){				
				var links = $('.unlink'+vertex.index);
				if( links.length > 0 && !attachedLinks ){
					for( var i=0; i<links.length; i++ ){
						$(links[i]).click(function(){
							createBranch(vertex,$(this).attr('name'));
						});
					}
					attachedLinks = true;
				}
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
/*
				on.attr({ 'x': (vertex.x2+vertex.x1)/2 });
				if( vertex.count > 1 ){
					on.attr('text',vertex.token+': '+vertex.count+' occurences');
				}
				else {
					on.attr('text',vertex.token+': '+vertex.count+' occurence');
				}
*/
			});
			$(node).mouseleave(function(){
				if( dragLock ){
					return;
				}
				for( var i=0; i<sal.connections.length; i++ ){
					$(sal.connections[i].node).remove();
				}
				sal.generalConnections();
				//on.attr('text','');
			});
			$(node).mousedown(function(evt){
				//dragNode(evt,node,vertex);
			});
/*
			var tiptext = "<table>";
			for( var i=0; i<vertex.sources.length; i++ ){
				tiptext += "<tr>";
				tiptext += "<td style='padding-top:5px;padding-bottom:5px;font-size: 12px;text-align:right;color:"+ReuseViz.colors[vertex.sources[i].sourceId]+";'>"+ReuseViz.sources[vertex.sources[i].sourceId]+"</td>";
				tiptext += "<td style='font-size: 12px;padding-left:10px;color:"+ReuseViz.colors[vertex.sources[i].sourceId]+";'>"+vertex.sources[i].token+"</td>";
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
					background: '#fff',
					color: '#000',
					border: { width: 2, radius: 2, color: '#777' }
				},
				position: {
					corner: {
						tooltip: "topMiddle",
						target: "bottomMiddle"
					}
				},
				hide: { when: 'mouseout', fixed: true }
			});
			$(node).click(function(evt){
				if( click ){
				}
				else {
					click = true;
				}
			});
*/
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
				y = 100 + i*20;
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
		for( var i=0; i<paths[0].length; i++ ){
			layout.push(paths[0][i]);
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
			var y = getY(vertex,path[0],path[path.length-1]);
			for( var j=1; j<path.length-1; j++ ){
				var heightN = path[j].textNode.getBBox().height;
				path[j].textNode.attr({ y: y });
				path[j].y1 = y - heightN / 2;
				path[j].y2 = y + heightN / 2;
				layout.push(path[j]);
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
		var h = y_max - y_min + 40;
		r.setSize((w+20)+"px",(h+20)+"px");
		bg.attr({ 'height': h, 'width': w+20 });
//		var on = r.text(w/2, h-20,'').attr({font: "16px Droid Sans, Helvetica, sans-serif"}).attr({color:"#3E576F",fill:"#3E576F","text-anchor":"middle"});
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
