var infinity = 100000000000; // !!! beware, not allowable for many applications

function KruskalEdge(a, b, w) { this.v1=a; this.v2=b; this.wt=w; }

function setEdgeAdjMatrix(i, j, w)
 { if(j > i) { var t=i; i=j; j=t; }//swap so that i >= j
   this.matrix[i][j] = w;
 }


function randomAdjMatrix(pr) // allocate random edges with probability 'pr'; LA
 { var i, j;
   for(i=0; i < this.Vertices; i++)
    { for(j=0; j < i; j++)
       { if(Math.random() <= pr)//LAllison, roll dice
            this.setEdge(i, j, 1+Math.round(Math.random()*8));  // wt in [1..9]
 }  }  }//randomAdjMatrix


function edgeWeightAdjMatrix(i,j)
 { return i >= j ? this.matrix[i][j] : this.matrix[j][i]; }

function adjacentAdjMatrix(i, j)
 { return this.edgeWeight(i,j) < infinity; }


function AdjMatrix(Vrtcs)  // constructor for a Graph as an Adjacency Matrix
 { this.Vertices   = Vrtcs;                    // Vertices [0..Vrtcs-1]
   this.edgeWeight = edgeWeightAdjMatrix;      // These functions
   this.adjacent   = adjacentAdjMatrix;        // and values
   this.random     = randomAdjMatrix;
   this.setEdge    = setEdgeAdjMatrix;

   this.matrix = new Array(Vrtcs);             // the Graph representation...
   var i, j;
   for(i=0; i < Vrtcs; i++)                    // undirected graph so...
    { this.matrix[i] = new Array(i+1);         // symmetric => triangular !!!
      for(j=0; j <= i; j++)
         this.setEdge(i, j, infinity);         // no edges, yet
    }
 }//AdjMatrix

//-----------------------------------------------------------------------------

function Prim(G)
// Post: T[] is a minimum spanning tree of G and
//       T[i-1] is the edge linking Vertex i into the tree
// Time complexity is O(|V|**2)
 { var T = new Array(G.Vertices-1);
   var i;
   var done = new Array(G.Vertices);

   done[0] = true;                               // initially T=<{0},{ }>
   for(i = 1; i < G.Vertices; i++)
    { T[i-1] = new KruskalEdge(0, i, G.edgeWeight(0,i));
      done[i]=false;
    }

   var dontCare;
   for(dontCare = 1; dontCare < G.Vertices; dontCare++)// |V|-1 times...      L
   // Invariant: T is a min' spanning sub-Tree of vertices in done            A
    { // find the undone vertex that is closest to the Spanning (sub-)Tree    l
      var minDist = infinity, closest = -1;                                // l
      for(i = 1; i < G.Vertices; i++)                                      // i
         if(! done[i] && T[i-1].wt <= minDist)                             // s
          { minDist = T[i-1].wt; closest = i; }                            // o
      done[closest] = true;                                                // n

      for(i=1; i < G.Vertices; i++) // recompute undone proximities to T
         if(! done[i])
          { var Gci = G.edgeWeight(closest, i);
            if(Gci < T[i-1].wt)
             { T[i-1].wt = Gci;
               T[i-1].v1 = closest;
    }     }  }

   return T;
 }//Prim
