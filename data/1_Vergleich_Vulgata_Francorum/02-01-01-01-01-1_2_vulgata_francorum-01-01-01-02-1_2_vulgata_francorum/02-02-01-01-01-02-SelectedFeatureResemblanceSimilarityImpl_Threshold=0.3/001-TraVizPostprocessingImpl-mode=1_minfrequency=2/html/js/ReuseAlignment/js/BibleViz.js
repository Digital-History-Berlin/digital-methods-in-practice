var BibleViz = new function(){

	var prefix = "";///tpp/req?url=http://localhost:8983";

	this.initialize = function(){
		this.container = $('<div class="container"/>').appendTo($('#container')[0]);
		var biblePos = $("<table class='biblePos'></table>").appendTo(this.container);
		$(this.container).append('<div id="Pagination" class="pagination"></div>');
		this.verseData = $("<h3></h3>").appendTo(this.container);
		this.book = $("<span></span>").appendTo(this.verseData);
		this.chapter = $("<span></span>").appendTo(this.verseData);
		this.verse = $("<span></span>").appendTo(this.verseData);
		this.translations = $("<div></div>").appendTo(this.container);
		this.alignment = $("<div style='text-align:center;overflow-y:auto;margin-top:20px;'></div>").appendTo(this.container);
		this.alignmentViz = $("<div id='alignment' style='display:block;'></div>").appendTo(this.alignment);

		var selectVerse = function(verse){
			$("#Pagination").pagination(BibleViz.verseId,{
				items_per_page:1,
				num_display_entries:10,
				current_page:verse-1,
				num_edge_entries:1,
				link_to:"#",
				ellipse_text:"...",
				prev_show_always:true,
				next_show_always:true,
				prev_text:"Previous Verse",
				next_text:"Next Verse",
				callback : function(){
					BibleViz.loadVerse();
				}
			});
		}

		var selectChapter = function(chapter){
			BibleViz.chapterId = chapter;
			$(BibleViz.selectVerse).empty();
			BibleViz.verseId = parseInt(getJson(prefix+"/solr/select/?wt=json&indent=on&q=bookId:"+BibleViz.bookId+" AND chapterId:"+chapter+"&rows=1000").response.docs[0].verses);
			for( var i=0; i<BibleViz.verseId; i++ ){
				$("<option>"+(i+1)+"</option>").appendTo(BibleViz.selectVerse);
			}
		}

		var selectBook = function(book){
			BibleViz.bookId = book;
			$(BibleViz.selectChapter).empty();
			var chapterData = getJson(prefix+"/solr/select/?wt=json&indent=on&q=bookId:"+book+"&rows=1000").response.docs;
			for( var i=0; i<chapterData.length; i++ ){
				$("<option>"+chapterData[i].chapterId+"</option>").appendTo(BibleViz.selectChapter);
			}
			selectChapter("1");
		}

		var titles = $("<tr></tr>").appendTo(biblePos);
		$("<td>Book</td>").appendTo(titles);
		$("<td>Chapter</td>").appendTo(titles);
		$("<td>Verse</td>").appendTo(titles);

		var dropdowns = $("<tr></tr>").appendTo(biblePos);
		var cell1 = $('<td/>').appendTo(dropdowns);
		var cell2 = $('<td/>').appendTo(dropdowns);
		var cell3 = $('<td/>').appendTo(dropdowns);
		var cell4 = $('<td/>').appendTo(dropdowns);
		this.selectBook = $('<select id="bookSelect"></select>').appendTo(cell1);
		this.selectChapter = $('<select id="chapterSelect"></select>').appendTo(cell2);
		this.selectVerse = $('<select id="verseSelect"></select>').appendTo(cell3);
		this.setVerse = $('<input type="button" value="Go to verse!">').appendTo(cell4);

		var bookData = getJson(prefix+"/solr/select/?wt=json&indent=on&q=title:*&rows=100").response.docs;
		for( var i=0; i<bookData.length; i++ ){
			$("<option>"+bookData[i].title+"</option>").appendTo(this.selectBook);
		}

		$(this.selectBook).click(function(){
			$('#bookSelect option:selected').each(function(){
				selectBook($(this).val());
			});
		});
		$(this.selectChapter).click(function(){
			$('#chapterSelect option:selected').each(function(){
				selectChapter($(this).val());
			});
		});

		selectBook("Genesis");
		selectVerse(1);

		$(this.setVerse).click(function(){
			$('#verseSelect option:selected').each(function(){
				selectVerse($(this).val());
			});
		});
		
	}

	var getJson = function(url) {
		var data;
		$.ajax({
			url : url,
			async : false,
			dataType : 'json',
			success : function(json) {
				data = json;
			}
		});
		return data;
	}

	this.setData = function(verse,list,sources){
		this.verse1 = verse;

		$(this.book).html(verse[0].book);
		$(this.chapter).html(" "+verse[0].chapter);
		$(this.verse).html(":"+verse[0].verse);
		$(this.alignmentViz).empty();
		$(this.translations).empty();
		var table = $("<table style='margin:auto;background-color:#FFF;'></table>").appendTo(this.translations);

		for( var i=0; i<sources.length; i++ ){
			var row = $("<tr></tr>").appendTo(table);
			if( i == 0 ){
				$("<td><input type='radio' name='baseSentence' class='baseRadio' value='"+i+"' checked='checked'></td>").appendTo(row);
			}
			else {
				$("<td><input type='radio' name='baseSentence' class='baseRadio' value='"+i+"'></td>").appendTo(row);
			}
			if( i < 10 ){
				$("<td><input type='checkbox' class='selectCheck' value='"+i+"' checked='checked'></td>").appendTo(row);
			}
			else {
				$("<td><input type='checkbox' class='selectCheck' value='"+i+"'></td>").appendTo(row);
			}
			$("<td style='text-align:right;color:"+SentenceAlignerProperties.getColor(i)+";'>"+sources[i].source+"/"+sources[i].verse+"</td>").appendTo(row);
			$("<td style='padding-left:20px;text-align:left;cursor:pointer;color:"+SentenceAlignerProperties.getColor(i)+";' class='sourceSentence'>"+list[i]+"</td>").appendTo(row);
		}

		var alignList = function(){
			var buttons = $('.baseRadio');
			var selection = $('.selectCheck');
			var sentences = $('.sourceSentence');
			var nlist = [];
			var appendFunctions = function(button,sentence,value){
				$(button).click(function(){
					BibleViz.aligner.visualize("alignment",value);
				});				
				$(sentence).mouseenter(function(){
					var dp = BibleViz.aligner.drawSentencePath(value);
					$(sentence).mouseleave(function(){
						$(dp.node).remove();
					});
				});
			}
			var selcount = 0;
			for( var i=0; i<selection.length; i++ ){
				if( $(selection[i]).attr('checked') ){
					$(buttons[i]).css('display','block');
					appendFunctions(buttons[i],sentences[i],selcount);
					selcount++;
					nlist.push(list[parseInt($(selection[i]).val())]);
				}
				else {
					$(buttons[i]).css('display','none');
				}
				$(selection[i]).click(function(){
					alignList();
				});
			}
			BibleViz.align(nlist);
		}
		alignList();

	};

	this.align = function(verses){
		this.aligner = new SentenceAligner();
		this.aligner.alignSentences(verses);
//		this.aligner.printVertices();
		this.aligner.visualize("alignment",0);
	};

	this.loadVerse = function(){
		var verseIds = $(".current","#Pagination");
		var id = 0;
		for( var i=0; i<verseIds.length; i++ ){
			if( $(verseIds[i]).html() != "Previous Verse" && $(verseIds[i]).html() != "Next Verse" ){
				id = $(verseIds[i]).html();
			}
		}
		var verseData = getJson(prefix+"/solr/select/?wt=json&indent=on&q=book:"+BibleViz.bookId+" AND chapter:"+BibleViz.chapterId+" AND verse:"+id);
		var id = verseData.response.docs[0].verseid;
		var query = "(unit1:"+id+" OR unit2:"+id+")";
		var reuses = getJson(prefix+"/solr/select/?wt=json&indent=on&q="+query+"&rows=10000").response.docs;
		var verses = [];
		var sources = [];
		for( var i=0; i<reuses.length; i++ ){
			var add1 = true, add2 = true;
			for( var j=0; j<sources.length; j++ ){
				if( sources[j].source == reuses[i].source1 && sources[j].verse == reuses[i].unit1 ){
					add1 = false;
				}
				if( sources[j].source == reuses[i].source2 && sources[j].verse == reuses[i].unit2 ){
					add2 = false;
				}
			}	
			if( add1 ){
				verses.push(reuses[i].verse1);
				sources.push({
					source: reuses[i].source1,
					verse: reuses[i].unit1
				});
			}
			if( add2 ){
				verses.push(reuses[i].verse2);
				sources.push({
					source: reuses[i].source2,
					verse: reuses[i].unit2
				});
			}
		}

//		this.align(verses);
		this.setData(verseData.response.docs,verses,sources);
	}

	this.isStopword = function(word){
		var stopwords = [
			"a","about","above","after","again","against","all","am","an","and","any","are","aren't","as","at","be","because",
			"been","before","being","below","between","both","but","by","can't","cannot","could","couldn't","did","didn't","do",
			"does","doesn't","doing","don't","down","during","each","few","for","from","further","had","hadn't","has","hasn't",
			"have","haven't","having","he","he'd","he'll","he's","her","here","here's","hers","herself","him","himself","his",
			"how","how's","i","i'd","i'll","i'm","i've","if","in","into","is","isn't","it","it's","its","itself","let's","me",
			"more","most","mustn't","my","myself","no","nor","not","of","off","on","once","only","or","other","ought","our","ours",
			"ourselves","out","over","own","same","shan't","she","she'd","she'll","she's","should","shouldn't","so","some","such",
			"than","that","that's","the","their","theirs","them","themselves","then","there","there's","these","they","they'd",
			"they'll","they're","they've","this","those","through","to","too","under","until","up","very","was","wasn't","we",
			"we'd","we'll","we're","we've","were","weren't","what","what's","when","when's","where","where's","which","while",
			"who","who's","whom","why","why's","with","won't","would","wouldn't","you","you'd","you'll","you're","you've","your",
			"yours","yourself","yourselves"
		];
		for( var i=0; i<stopwords.length; i++ ){
			if( stopwords[i] == word.toLowerCase() ){
				return true;
			}
		}
		return false;
	}

};
