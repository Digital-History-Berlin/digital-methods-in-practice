var GraphMLViewer=GraphMLViewer||{};GraphMLViewer.DOMRenderer=function(a,c,e,b){a=XMLObjectifier.xmlToJSON(a);this.doc=GraphMLViewer.parseGraphml(a);this.graphs=this.doc.graphs;this.gcontainer=c;this.plotElement=e;this.layouter=b;this.scale=1;this.dragStartHandler=this.handleDragStart.bind(this);this.dragHandler=this.handleDrag.bind(this);this.releaseHandler=this.handleRelease.bind(this);this.zoomHandler=this.handleZoom.bind(this);this.onModel={left:0,top:0};this.lastC={left:0,top:0}};
GraphMLViewer.DOMRenderer.prototype.init=function(){for(var a=0;a<this.graphs.length;a++)this.layouter&&(new this.layouter(this.graphs[a])).layout();this.render()};
GraphMLViewer.DOMRenderer.prototype.render=function(){this.gcontainer.innerHTML="";this.container=document.createElement("div");this.container.classList.add("container");this.container.addEventListener("mousedown",this.dragStartHandler);this.container.addEventListener("mousewheel",this.zoomHandler);this.container.addEventListener("DOMMouseScroll",this.zoomHandler);this.gcontainer.appendChild(this.container);for(var a=0;a<this.graphs.length;a++)this.renderModel(this.graphs[a])};
GraphMLViewer.DOMRenderer.prototype.renderModel=function(a){var c=document.createElement("div");c.classList.add("model");c.gmlId=a.id;this.renderGraph(a,c);this.container.appendChild(c);a=c.getBoundingClientRect();var e=this.findUpperLeft(c,{left:Infinity,top:Infinity});this.transform={left:a.left-e.left+10,top:a.top-e.top+10};c.style[transformprop]="translate("+this.transform.left+"px,"+this.transform.top+"px)"};
GraphMLViewer.DOMRenderer.prototype.handleZoom=function(a){a.preventDefault();this.onModel={left:this.onModel.left-(this.lastC.left-a.offsetX)/this.scale,top:this.onModel.top-(this.lastC.top-a.offsetY)/this.scale};this.scale=0<a.wheelDeltaY||0>a.detail?1.1*this.scale:this.scale/1.1;this.lastC={left:a.offsetX,top:a.offsetY};this.container.firstChild.style[transformprop]="scale("+this.scale+") translate("+(this.transform.left+(this.lastC.left-this.onModel.left)/this.scale)+"px,"+(this.transform.top+
(this.lastC.top-this.onModel.top)/this.scale)+"px)";this.container.firstChild.style[transformoriginprop]=this.onModel.left+"px "+this.onModel.top+"px"};
GraphMLViewer.DOMRenderer.prototype.handleDragStart=function(a){if(0===a.button){this.draggedTarget=a.target;this.draggedNode=void 0;a:do for(var c=0;c<this.graphs.length;c++)if(this.graphs[c].getNodeById(this.draggedTarget.gmlId)){this.draggedNode=this.graphs[c].getNodeById(this.draggedTarget.gmlId);this.nsX=this.draggedNode.position.x;this.nsY=this.draggedNode.position.y;break a}while(this.draggedTarget!=this.container&&(this.draggedTarget=this.draggedTarget.parentNode));this.sX=a.clientX;this.sY=
a.clientY;window.addEventListener("mousemove",this.dragHandler);window.addEventListener("mouseup",this.releaseHandler)}};
GraphMLViewer.DOMRenderer.prototype.handleDrag=function(a){function c(a){a.path.sx=0;a.path.sy=0;a.path.tx=0;a.path.ty=0}function e(a,c){for(var e=c.getElementsByClassName("edge"),f=0;f<e.length;f++)if(e[f].gmlId==a)return e[f]}if(this.draggedNode){this.draggedTarget.style[transformprop]="translate("+(a.clientX-this.sX)/this.scale+"px,"+(a.clientY-this.sY)/this.scale+"px)";this.draggedNode.position.x=1*this.nsX+(a.clientX-this.sX)/this.scale;this.draggedNode.position.y=1*this.nsY+(a.clientY-this.sY)/
this.scale;a=this.draggedTarget.parentNode;for(var b=0;b<this.draggedNode.sourceOf.length;b++){c(this.draggedNode.sourceOf[b]);var f=e(this.draggedNode.sourceOf[b].id,a);f.parentNode.removeChild(f);this.renderEdge(this.draggedNode.sourceOf[b],a)}for(b=0;b<this.draggedNode.targetOf.length;b++)c(this.draggedNode.targetOf[b]),f=e(this.draggedNode.targetOf[b].id,a),f.parentNode.removeChild(f),this.renderEdge(this.draggedNode.targetOf[b],a)}else this.draggedTarget.firstChild.style[transformprop]="scale("+
this.scale+") translate("+(this.transform.left+(a.clientX-this.sX+this.lastC.left-this.onModel.left)/this.scale)+"px,"+(this.transform.top+(a.clientY-this.sY+this.lastC.top-this.onModel.top)/this.scale)+"px)"};
GraphMLViewer.DOMRenderer.prototype.handleRelease=function(a){window.removeEventListener("mousemove",this.dragHandler);window.removeEventListener("mouseup",this.releaseHandler);this.draggedNode?(a=this.draggedTarget.parentNode,this.draggedTarget.parentNode.removeChild(this.draggedTarget),this.renderNode(this.draggedNode,a)):(this.transform.left+=(a.clientX-this.sX)/this.scale,this.transform.top+=(a.clientY-this.sY)/this.scale)};
GraphMLViewer.DOMRenderer.prototype.findUpperLeft=function(a,c){for(var e=a.childNodes,b=0;e&&b<e.length;b++){var f=e[b].getBoundingClientRect();f.left<c.left&&(c.left=f.left);f.top<c.top&&(c.top=f.top)}return{left:c.left,top:c.top}};GraphMLViewer.DOMRenderer.prototype.renderGraph=function(a,c){for(var e=0;e<a.nodes.length;e++)this.renderNode(a.nodes[e],c);for(e=0;e<a.edges.length;e++)this.renderEdge(a.edges[e],c)};
GraphMLViewer.DOMRenderer.prototype.renderNode=function(a,c){this.plotElement(a,c,this.doc);for(var e=0;e<a.graphs.length;e++)this.renderGraph(a.graphs[e],c)};
GraphMLViewer.DOMRenderer.prototype.renderEdge=function(a,c){var e,b,f,j,g=function(a,e,c,f,b,q,p,g){var x=(a-c)*(q-g)-(e-f)*(b-p);if(0!==x)return{x:((a*f-e*c)*(b-p)-(a-c)*(b*g-q*p))/x,y:((a*f-e*c)*(q-g)-(e-f)*(b*g-q*p))/x}};a.path.Point?(f=1*a.path.Point[0].x,j=1*a.path.Point[0].y):(f=1*a.path.tx+1*a.target.position.x+1*a.target.geometry.width/2,j=1*a.path.ty+1*a.target.position.y+1*a.target.geometry.height/2);e=1*a.path.sx+1*a.src.position.x+1*a.src.geometry.width/2;b=1*a.path.sy+1*a.src.position.y+
1*a.src.geometry.height/2;d={x:f-e,y:j-b};bp={x1:1*a.src.position.x,y1:1*a.src.position.y,x2:1*a.src.position.x+1*a.src.geometry.width,y2:1*a.src.position.y+1*a.src.geometry.height};var h,k;h=0<=d.y?g(e,b,f,j,bp.x1,bp.y2,bp.x2,bp.y2):g(e,b,f,j,bp.x1,bp.y1,bp.x2,bp.y1);k=0<=d.x?g(e,b,f,j,bp.x2,bp.y1,bp.x2,bp.y2):g(e,b,f,j,bp.x1,bp.y1,bp.x1,bp.y2);h&&k&&(h&&h.x>bp.x1&&h.x<bp.x2?(a.path.sx=h.x-e+1*a.path.sx,a.path.sy=h.y-b+1*a.path.sy):(a.path.sx=k.x-e+1*a.path.sx,a.path.sy=k.y-b+1*a.path.sy));a.path.Point?
(e=1*a.path.Point[a.path.Point.length-1].x,b=1*a.path.Point[a.path.Point.length-1].y):(e=1*a.path.sx+1*a.src.position.x+1*a.src.geometry.width/2,b=1*a.path.sy+1*a.src.position.y+1*a.src.geometry.height/2);f=1*a.path.tx+1*a.target.position.x+1*a.target.geometry.width/2;j=1*a.path.ty+1*a.target.position.y+1*a.target.geometry.height/2;d={x:f-e,y:j-b};bp={x1:1*a.target.position.x,y1:1*a.target.position.y,x2:1*a.target.position.x+1*a.target.geometry.width,y2:1*a.target.position.y+1*a.target.geometry.height};
h=0>=d.y?g(e,b,f,j,bp.x1,bp.y2,bp.x2,bp.y2):g(e,b,f,j,bp.x1,bp.y1,bp.x2,bp.y1);k=0>=d.x?g(e,b,f,j,bp.x2,bp.y1,bp.x2,bp.y2):g(e,b,f,j,bp.x1,bp.y1,bp.x1,bp.y2);h&&k&&(h.x>bp.x1&&h.x<bp.x2?(a.path.tx=h.x-f+1*a.path.tx,a.path.ty=h.y-j+1*a.path.ty):(a.path.tx=k.x-f+1*a.path.tx,a.path.ty=k.y-j+1*a.path.ty));this.plotElement(a,c,this.doc)};GraphMLViewer=GraphMLViewer||{};GraphMLViewer.Layout=GraphMLViewer.Layout||{};GraphMLViewer.Layout.yEd=function(a){this.graph=a};
GraphMLViewer.Layout.yEd.prototype.layout=function(){for(var a=0;a<this.graph.nodes.length;a++)for(var c=this.graph.nodes[a],e=0;e<c.datas.length;e++){var b=c.datas[e];if("object"==typeof b.value){var f=Object.keys(b.value)[0];b.value[f][0].Geometry&&(c.position.x=b.value[f][0].Geometry[0].x,c.position.y=b.value[f][0].Geometry[0].y,c.geometry.width=b.value[f][0].Geometry[0].width,c.geometry.height=b.value[f][0].Geometry[0].height)}}for(a=0;a<this.graph.edges.length;a++){c=this.graph.edges[a];for(e=
0;e<c.datas.length;e++)b=c.datas[e],"object"==typeof b.value&&(f=Object.keys(b.value)[0],b.value[f][0].Path&&(c.path=b.value[f][0].Path[0]))}};GraphMLViewer.Layout.Spring=function(a){this.graph=a;this.iterations=500;this.maxRepulsiveForceDistance=1E6;this.k=100;this.c=0.01;this.maxVertexMovement=0.5};
GraphMLViewer.Layout.Spring.prototype={layout:function(){this.layoutPrepare();for(var a=0;a<this.iterations;a++)this.layoutIteration()},layoutPrepare:function(){for(var a=0;a<this.graph.nodes.length;a++){var c=this.graph.nodes[a];c.geometry.width=20;c.geometry.height=20;c.position.x=10*Math.random();c.position.y=10*Math.random();c.layoutForceX=0;c.layoutForceY=0}},layoutIteration:function(){for(var a=0;a<this.graph.nodes.length;a++)for(var c=this.graph.nodes[a],e=a+1;e<this.graph.nodes.length;e++)this.layoutRepulsive(c,
this.graph.nodes[e]);for(a=0;a<this.graph.edges.length;a++)this.layoutAttractive(this.graph.edges[a]);for(a=0;a<this.graph.nodes.length;a++){var c=this.graph.nodes[a],e=this.c*c.layoutForceX,b=this.c*c.layoutForceY,f=this.maxVertexMovement;e>f&&(e=f);e<-f&&(e=-f);b>f&&(b=f);b<-f&&(b=-f);c.position.x+=e;c.position.y+=b;c.layoutForceX=0;c.layoutForceY=0}},layoutRepulsive:function(a,c){var e=c.position.x-a.position.x,b=c.position.y-a.position.y,f=e*e+b*b;0.01>f&&(e=0.1*Math.random()+0.1,b=0.1*Math.random()+
0.1,f=e*e+b*b);f=Math.sqrt(f);if(f<this.maxRepulsiveForceDistance){var j=this.k*this.k/f;c.layoutForceX+=j*e/f;c.layoutForceY+=j*b/f;a.layoutForceX-=j*e/f;a.layoutForceY-=j*b/f}},layoutAttractive:function(a){var c=a.src,e=a.target,b=e.position.x-c.position.x,f=e.position.y-c.position.y,j=b*b+f*f;0.01>j&&(b=0.1*Math.random()+0.1,f=0.1*Math.random()+0.1,j=b*b+f*f);var g=Math.sqrt(j);g>this.maxRepulsiveForceDistance&&(g=this.maxRepulsiveForceDistance,j=g*g);j=(j-this.k*this.k)/this.k;if(void 0==a.weight||
1>a.weight)a.weight=1;j*=0.5*Math.log(a.weight)+1;e.layoutForceX-=j*b/g;e.layoutForceY-=j*f/g;c.layoutForceX+=j*b/g;c.layoutForceY+=j*f/g}};GraphMLViewer=GraphMLViewer||{};GraphMLViewer.GmlDoc=function(){this.keys=[];this.graphs=[];this.datas=[]};GraphMLViewer.GmlGraph=function(a,c){this.id=a;this.edgedefault=c;this.nodes=[];this.edges=[];this.datas=[];this.parent};GraphMLViewer.GmlGraph.prototype.getNodeById=function(a){for(var c=0;c<this.nodes.length;c++){if(this.nodes[c].id==a)return this.nodes[c];for(var e=0;e<this.nodes[c].graphs.length;e++){var b=this.nodes[c].graphs[e].getNodeById(a);if(b)return b}}};
GraphMLViewer.GmlEdge=function(a,c,e,b){this.id=a;this.src=c;this.target=e;this.directed=b;this.datas=[];this.parent;this.path={sx:0,sy:0,tx:0,ty:0}};GraphMLViewer.GmlNode=function(a){this.id=a;this.datas=[];this.graphs=[];this.sourceOf=[];this.targetOf=[];this.parent;this.position={};this.geometry={width:10,height:10}};GraphMLViewer.GmlKey=function(a,c,e,b){this.id=a;this.fortype=c;this.attr_name=e;this.attr_type=b};GraphMLViewer.GmlData=function(a,c){this.key=a;this.value=c;this.parent};GraphMLViewer=GraphMLViewer||{};
GraphMLViewer.parseGraphml=function(a){function c(a,b){for(var g=0,h=a.key;void 0!=h&&g<h.length;g++){var k=new GraphMLViewer.GmlKey(h[g].id,h[g]["for"],e(h[g],".name"),e(h[g],".type"));b.keys.push(k)}g=0;for(h=a.data;void 0!=h&&g<h.length;g++)k=new GraphMLViewer.GmlData(h[g].key),h[g]._children&&0<h[g]._children.length?(k.value={},k.value[h[g]._children[0]]=h[g][h[g]._children[0]]):k.value=h[g].Text,b.datas.push(k),k.parent=b;g=0;for(h=a.node;void 0!=h&&g<h.length;g++)k=new GraphMLViewer.GmlNode(h[g].id),
c(h[g],k),b.nodes.push(k),k.parent=b;g=0;for(h=a.graph;void 0!=h&&g<h.length;g++)k=new GraphMLViewer.GmlGraph(h[g].id,h[g].edgedefault),c(h[g],k),b.graphs.push(k),k.parent=b;g=0;for(h=a.edge;void 0!=h&&g<h.length;g++)k=new GraphMLViewer.GmlEdge(h[g].id,b.getNodeById(h[g].source),b.getNodeById(h[g].target),h[g].directed),c(h[g],k),b.edges.push(k),b.getNodeById(h[g].source).sourceOf.push(k),b.getNodeById(h[g].target).targetOf.push(k),k.parent=b}function e(a,e){for(var c=0,b=a._attributes,k=b.length;c<
k;c++)if(-1!==b[c].indexOf(e))return a[b[c]];return""}var b=new GraphMLViewer.GmlDoc;c(a,b);return b};var XMLObjectifier=function(){var a=function(a){var e="";a&&"string"===typeof a&&(e=a);return/^((-)?([0-9]*)((\.{0,1})([0-9]+))?$)/.test(e)};return{xmlToJSON:function(c){try{if(!c)return null;var e={typeOf:"JSXBObject"},b=9==c.nodeType?c.documentElement:c;e.RootName=b.nodeName||"";if(3==c.nodeType||4==c.nodeType)return c.nodeValue;var f=function(a){return a.replace(/^\s+|\s+$/gm,"")},j=function(a,e){if(0<e.attributes.length){var c=e.attributes.length-1,b;a._attributes=[];do b=String(String(e.attributes[c].name).replace(/-/g,
"_")),a._attributes.push(b),a[b]=f(e.attributes[c].value);while(c--)}},g=function(e){e.getNodeByAttribute=function(a,e){if(0<this.length){var c,b=this.length-1;try{do if(c=this[b],c[a]==e)return c;while(b--)}catch(q){}return!1}};e.contains=function(a,e){if(0<this.length){var c=this.length-1;try{do if(this[c][a]==e)return!0;while(c--)}catch(b){}return!1}};e.indexOf=function(a,e){var c=-1;if(0<this.length){var b=this.length-1;try{do this[b][a]==e&&(c=b);while(b--)}catch(q){return-1}return c}};e.SortByAttribute=
function(e,c){if(this.length){var b=function(e,c){var b=e[c];return b=a(b)?parseFloat(b):b};this.sort(function(a,q){var p=0,f,x;f=b(a,e);x=b(q,e);f<x?p=-1:x<f&&(p=1);c&&(p="DESC"==c.toUpperCase()?0-p:p);return p})}};e.SortByValue=function(e){if(this.length){var c=function(e){e=e.Text;return e=a(e)?parseFloat(e):e};this.sort(function(a,b){var q=0,p,f;p=c(a);f=c(b);p<f?q=-1:f<p&&(q=1);e&&(q="DESC"==e.toUpperCase()?0-q:q);return q})}};e.SortByNode=function(e,c){if(this.length){var b=function(e,c){var b=
e[c][0].Text;return b=a(b)?parseFloat(b):b};this.sort(function(a,q){var p=0,f,x;f=b(a,e);x=b(q,e);f<x?p=-1:x<f&&(p=1);c&&(p="DESC"==c.toUpperCase()?0-p:p);return p})}}},h=function(a,e){var c,b,k,q="";if(!e)return null;0<e.attributes.length&&j(a,e);a.Text="";if(e.hasChildNodes()){var p=e.childNodes.length-1,D=0;do switch(b=e.childNodes[D],b.nodeType){case 1:a._children=[];c=b.localName?b.localName:b.baseName;c=String(c).replace(/-/g,"_");q!=c&&a._children.push(c);a[c]||(a[c]=[]);k={};a[c].push(k);
0<b.attributes.length&&j(k,b);a[c].contains||g(a[c]);q=c;b.hasChildNodes()&&h(k,b);break;case 3:a.Text+=f(b.nodeValue);break;case 4:a.Text+=b.text?f(b.text):f(b.nodeValue)}while(D++<p)}};h(e,b);b=c=null;return e}catch(k){return null}},textToXML:function(a){var e=null;try{e=document.all?new ActiveXObject("Microsoft.XMLDOM"):new DOMParser,e.async=!1}catch(b){throw Error("XML Parser could not be instantiated");}var f;try{f=document.all?e.loadXML(a)?e:!1:e.parseFromString(a,"text/xml")}catch(j){throw Error("Error parsing XML string");
}return f}}}();var transformprop=getsupportedprop(["transform","MozTransform","webkitTransform"]),transformoriginprop=getsupportedprop(["transformOrigin","MozTransformOrigin","webkitTransformOrigin"]);function getsupportedprop(a){for(var c=document.documentElement,e=0;e<a.length;e++)if("string"==typeof c.style[a[e]])return a[e]};GraphMLViewer=GraphMLViewer||{};GraphMLViewer.Plotter=GraphMLViewer.Plotter||{};GraphMLViewer.Plotter.Simple=GraphMLViewer.Plotter.Simple||{};
GraphMLViewer.Plotter.Simple.plotElement=function(a,c){if(a.geometry){var e=document.createElement("div");e.classList.add("node");e.style.left=a.position.x;e.style.top=a.position.y;e.style.width=a.geometry.width;e.style.height=a.geometry.height;e.gmlId=a.id;e.classList.add("simple");c.appendChild(e)}else if(a.path){e=document.createElement("div");e.classList.add("edge");var b=parseInt(a.src.position.x,10)+parseInt(a.src.geometry.width/2,10)+parseInt(a.path.sx,10),f=parseInt(a.src.position.y,10)+parseInt(a.src.geometry.height/
2,10)+parseInt(a.path.sy,10),j=parseInt(a.target.position.x,10)+parseInt(a.target.geometry.width/2,10)+parseInt(a.path.tx,10),g=parseInt(a.target.position.y,10)+parseInt(a.target.geometry.height/2,10)+parseInt(a.path.ty,10);e.style.left=b+"px";e.style.top=f+"px";var h=document.createElement("div"),b=j-b,g=g-f,f=Math.sqrt(b*b+g*g),g=Math.atan2(g,b)-Math.PI/2;h.style.height=f+"px";h.style.left="0px";h.style.top="0px";h.style[transformprop]="rotate("+g+"rad)";h.className="line";h.angle=g;h.style.borderLeft=
"1px solid black";if("true"==a.directed||"directed"==a.parent.edgedefault)f=document.createElement("div"),f.classList.add("arrow"),f.classList.add("standard"),f.style.bottom="0px",h.appendChild(f);e.appendChild(h);e.gmlId=a.id;c.appendChild(e)}};GraphMLViewer.Plotter.yEd=GraphMLViewer.Plotter.yEd||{};
GraphMLViewer.Plotter.yEd.plotElement=function(a,c,e){function b(a,e){if("object"==typeof a.value){var c=Object.keys(a.value)[0],b,l;if("GenericNode"==c||"TableNode"==c)b=a.value[c][0],a.parent.geometry.width=b.Geometry[0].width,a.parent.geometry.height=b.Geometry[0].height,l=f(b.BorderStyle,b.Fill,a.parent.geometry,a.parent.position,b.NodeLabel,b.DropShadow,b.Shape,b.StyleProperties,b.configuration),l.gmlId=a.parent.id,e.appendChild(l);"ShapeNode"==c&&(b=a.value[c][0],a.parent.geometry.width=b.Geometry[0].width,
a.parent.geometry.height=b.Geometry[0].height,l=f(b.BorderStyle,b.Fill,a.parent.geometry,a.parent.position,b.NodeLabel,b.DropShadow,b.Shape,b.StyleProperties),l.gmlId=a.parent.id,e.appendChild(l));"SVGNode"==c&&(b=a.value[c][0],a.parent.geometry.width=b.Geometry[0].width,a.parent.geometry.height=b.Geometry[0].height,l=f(b.BorderStyle,b.Fill,a.parent.geometry,a.parent.position,b.NodeLabel,b.DropShadow,b.Shape,b.StyleProperties,void 0,b.SVGModel,a.parent.id),l.gmlId=a.parent.id,e.appendChild(l));if("ProxyAutoBoundsNode"==
c){l=parseInt(a.value[c][0].Realizers[0].active,10);b=a.value[c][0].Realizers[0].GroupNode[l];a.parent.geometry.width=b.Geometry[0].width;a.parent.geometry.height=b.Geometry[0].height;l=b.BorderStyle;var r=b.Fill,v=a.parent.geometry,t=a.parent.position,z=b.NodeLabel,n=b.DropShadow;b=b.Shape;var s=document.createElement("div");s.classList.add("groupnode");k(s,v,t);z&&B(s,z[0]);A(s,r[0]);h(s,l[0]);n&&g(s,n[0]);b&&s.classList.add(b[0].type);l=s;l.gmlId=a.parent.id;e.appendChild(l)}if(-1<c.indexOf("Edge")){b=
a.value[c][0];t=a.parent;l=b.Arrows;n=b.EdgeLabel;z=b.LineStyle;b=a.parent.path;c=document.createElement("div");c.classList.add("edge");r={x:parseInt(t.src.position.x,10)+parseInt(t.src.geometry.width/2,10)+parseInt(b.sx,10),y:parseInt(t.src.position.y,10)+parseInt(t.src.geometry.height/2,10)+parseInt(b.sy,10)};v=parseInt(t.target.position.x,10)+parseInt(t.target.geometry.width/2,10)+parseInt(b.tx,10);t=parseInt(t.target.position.y,10)+parseInt(t.target.geometry.height/2,10)+parseInt(b.ty,10);c.style.left=
r.x+"px";c.style.top=r.y+"px";if(n){n=n[0];s=document.createElement("span");s.style.position="absolute";var m=document.createTextNode(n.Text);s.appendChild(m);C(s,n);c.appendChild(s)}var z=z[0],w,y,n=r;if(b.Point)for(var u=0;u<b.Point.length;u++)w=b.Point[u].x-n.x,y=b.Point[u].y-n.y,s=Math.sqrt(w*w+y*y),w=Math.atan2(y,w)-Math.PI/2,m=document.createElement("div"),m.style.height=s+"px",m.style.left=1*n.x-1*r.x+"px",m.style.top=1*n.y-1*r.y+"px",m.style[transformprop]="rotate("+w+"rad)",m.className="line",
m.angle=w,j(m,z),c.appendChild(m),n=b.Point[u];m=document.createElement("div");w=v-n.x;y=t-n.y;s=Math.sqrt(w*w+y*y);w=Math.atan2(y,w)-Math.PI/2;m.style.height=s+"px";m.style.left=1*n.x-1*r.x+"px";m.style.top=1*n.y-1*r.y+"px";m.style[transformprop]="rotate("+w+"rad)";m.className="line";m.angle=w;j(m,z);c.appendChild(m);l&&(l=l[0],b=c.getElementsByClassName("line"),"none"!=l.source&&(r=document.createElement("div"),r.classList.add("arrow"),r.classList.add(l.source),r.style.top="0px",r.style[transformprop]=
"rotate(180deg)",b[0].appendChild(r)),"none"!=l.target&&(r=document.createElement("div"),r.classList.add("arrow"),r.classList.add(l.target),r.style.bottom="0px",b[b.length-1].appendChild(r)));l=c;l.gmlId=a.parent.id;e.appendChild(l)}}}function f(a,b,c,f,l,j,u,t,v,n,s){var m=document.createElement("div");m.classList.add("node");v&&m.classList.add(v);k(m,c,f);l&&B(m,l[0]);b&&!n&&A(m,b[0]);a&&!n&&h(m,a[0]);j&&g(m,j[0]);u&&m.classList.add(u[0].type);if(t){a=t[0];for(b=0;b<a.Property.length;b++)"ModernNodeRadius"==
a.Property[b].name&&(m.style.borderRadius=a.Property[b].value+"px")}if(n){n=n[0];a=document.createElement("div");b=e.datas[0].value.Resources[0].Resource;for(f=0;f<b.length;f++)if(b[f].id==n.SVGContent[0].refid){l=b[f].Text;for(reg=RegExp(/id="(.*?)"/g);null!==(j=reg.exec(l));)l=l.replace(RegExp(j[1],"g"),s+"_"+j[1]);a.innerHTML=l;l=a.getElementsByTagName("svg")[0];delete a;l.style[transformoriginprop]="0px 0px";l.style[transformprop]="scale("+c.width/l.width.baseVal.value+","+c.height/l.height.baseVal.value+
")";m.appendChild(l)}}return m}function j(a,b){"false"!=b.hasColor&&(a.style.borderLeft="line"==b.type?b.width+"px solid "+b.color:b.width+"px "+b.type+" "+b.color)}function g(a,b){a.style.boxShadow=b.offsetX+"px "+b.offsetY+"px 4px "+b.color}function h(a,b){"false"!=b.hasColor&&(a.style.borderColor=b.color,a.style.borderWidth=b.width+"px",a.style.borderStyle="line"==b.type?"solid":b.type)}function k(a,b,c){a.style.width=b.width+"px";a.style.height=b.height+"px";a.style.left=c.x+"px";a.style.top=
c.y+"px"}function A(a,b){"false"!=b.hasColor&&(a.style.backgroundColor=u(b.color),b.color2&&(a.style.background="-moz-linear-gradient(top , "+u(b.color)+", "+u(b.color2)+")",a.style.background="-webkit-linear-gradient(top, "+u(b.color)+", "+u(b.color2)+")"),"true"==b.transparent&&(a.style.background="transparent"))}function B(a,b){var c=document.createElement("span");c.style.position="absolute";var e=document.createTextNode(b.Text);c.appendChild(e);C(c,b);a.appendChild(c)}function C(a,b){"false"!=
b.visible&&(b.alignment&&(a.style.textAlign=b.alignment,a.style.verticalAlign="middle"),b.fontFamily&&(a.style.fontFamily=b.fontFamily),b.fontSize&&(a.style.fontSize=b.fontSize),b.fontStyle&&("plain"==b.fontStyle&&(a.style.fontStyle="normal"),"italic"==b.fontStyle&&(a.style.fontStyle="italic"),"bold"==b.fontStyle&&(a.style.fontStyle="normal"),"bolditalic"==b.fontStyle&&(a.style.fontStyle="italic",a.style.fontWeight="bold")),"false"!=b.hasBackgroundColor&&b.backgroundColor&&(a.style.backgroundColor=
b.backgroundColor),b.height&&(a.style.height=b.height+"px"),"true"==b.hasLineColor&&b.lineColor&&(a.style.borderColor=b.lineColor,a.style.bordeWidth="1px"),b.rotationAngle&&(a.style[transformprop]="rotate("+b.rotationAngle+"deg)"),b.textColor&&(a.style.color=b.textColor),b.width&&(a.style.width=b.width+"px"),b.x&&(a.style.left=b.x+"px"),b.y&&(a.style.top=b.y+"px"))}function u(a){a=a.replace("#","");var b=parseInt(a.substring(0,2),16),c=parseInt(a.substring(2,4),16),e=parseInt(a.substring(4,6),16),
f=1;6<a.length&&(f=parseInt(a.substring(6,8),16)/255);return"rgba("+b+","+c+","+e+","+f+")"}if(a.geometry)for(var v=0;v<a.datas.length;v++)b(a.datas[v],c);else if(a.path)for(v=0;v<a.datas.length;v++)b(a.datas[v],c)};