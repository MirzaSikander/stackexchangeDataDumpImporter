var fs = require('fs');

fs.readFile('/Users/mirzasikander/Dropbox/school/CSCI 599/Data Files/TagsGreaterThan10.csv', {
	encoding: "utf8"
}, function(err, data) {
	if (err) throw err;

	//Make an array containing tags.
	var tags = data.split('\n');

	//write to a file.
	var fvfileStream = fs.createWriteStream('/Users/mirzasikander/Dropbox/school/CSCI 599/Data Files/TagFeatureVectors.csv');
	var testFileStream = fs.createWriteStream('/Users/mirzasikander/Dropbox/school/CSCI 599/Data Files/TestFile.csv');

	tags.forEach(function(tag, index, array) {
		testFileStream.write(tag + "\n", "utf8");

		//read in the question posts
		var qfileStream = fs.createReadStream('/Users/mirzasikander/Dropbox/school/CSCI 599/Data Files/QuestionsWithTags.csv', {
			encoding: "utf8"
		});

		qfileStream.on('readable', function() {
			var qData = qfileStream.read();

			var questions = qData.split('\n');

			questions.forEach(function(row, index, array) {
				var fields = row.split(',');

				var questionTags = fields[1];

				questionTags = questionTags.match(/<([^>]+)>/g);

				if (questionTags == null) throw new Error(fields, index);

				var found = false;
				for (var i = questionTags - 1; i >= 1; i--) {
					var currentTag = questionTags[i]

					if (currentTag === tag) {
						found = true;
						break;
					}
				};

				if (found) {
					fvfileStream.write("1,");
				} else {
					fvfileStream.write("0,")
				}
			})
		});

		qfileStream.on('end', function() {
			fvfileStream.write("\n");
		});
	});
});