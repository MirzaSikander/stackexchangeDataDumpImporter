var fs = require('fs');
var lineReader = require('line-reader');

fs.readFile('/Users/mirzasikander/Dropbox/school/CSCI 599/Data Files/TagsGreaterThan10.csv', {
	encoding: "utf8"
}, function(err, data) {
	if (err) throw err;

	//Make an array containing tags.
	var tags = data.split('\n');

	//write to a file.
	var fvfileStream = fs.createWriteStream('/Users/mirzasikander/Dropbox/school/CSCI 599/Data Files/TagFeatureVectors.csv');

	var index = 0;
	//read in the question posts
	lineReader.eachLine('/Users/mirzasikander/Dropbox/school/CSCI 599/Data Files/QuestionsWithTags.csv', function(line, last) {
		var fields = line.split(',');

		index++;
		console.log("Processing question number: " + index + " id: " + fields[0]);

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
				fvfileStream.write("1,");
			} else {
				fvfileStream.write("0,")
			}
		})

		fvfileStream.write("\n");

		if (last) {
			fvfileStream.end();
		}
	});
});