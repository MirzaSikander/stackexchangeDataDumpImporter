var fs = require('fs');

var test = function(){
	console.log("Done Draining");
}

fs.readFile('/Users/mirzasikander/Dropbox/school/CSCI 599/Data Files/TagsGreaterThan10.csv', {
	encoding: "utf8"
}, function(err, data) {
	if (err) throw err;

	//Make an array containing tags.
	var tags = data.split('\n');

	//write to a file.
	var fvfileStream = fs.createWriteStream('/Users/mirzasikander/Desktop/TagFeatureVectors.csv');

	//read in the question posts
	var qfileStream = fs.createReadStream('/Users/mirzasikander/Dropbox/school/CSCI 599/Data Files/QuestionsWithTags.csv', {
		encoding: "utf8"
	});

	var partialRow = null;
	var writable = true;
	var count = 0;

	var doRead = function() {
		var qData = qfileStream.read();
		var questions = qData.split('\n');

		if (partialRow != null) {
			questions[0] = partialRow + questions[0];
			partialRow = null;
		}

		var lastRow = questions[questions.length - 1];
		if (lastRow.charAt(lastRow.length - 1) != '\n') {
			partialRow = lastRow;
			questions.splice(questions.length-1, 1);
		}

		questions.forEach(function(row, index, array) {
			count++;

			var fields = row.split(',');

			console.log("Processing question number: " + count + " id: " + fields[0]);
			var tagString = fields[1];

			var regex = new RegExp(/<([^>]+)>/g);

			tags.forEach(function(tag, index, array) {
				var found = false;
				var questionTags;

				while ((questionTags = regex.exec(tagString)) != null) {
					var currentTag = questionTags[1]

					if (currentTag === tag) {
						found = true;
						break;
					}
				};

				if (found) {
					writable = fvfileStream.write("1,", "utf8");
				} else {
					writable = fvfileStream.write("0,","utf8");
				}
			});
		});

		fvfileStream.write("\n");
	}

	qfileStream.on('readable', function() {
		if (writable) {
			doRead();
		} else {
			fvfileStream.once('drain', test);
		}
	});

	qfileStream.on('end', function() {
		fvfileStream.end();
	});
});