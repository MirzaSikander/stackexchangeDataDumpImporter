var fs = require('fs');
var stream = require('stream');
var util = require('util');

var Transform = stream.Transform;

function FVCreator(options) {
	// allow use without new
	if (!(this instanceof FVCreator)) {
		return new FVCreator(options);
	}

	// init Transform
	Transform.call(this, options);
}

util.inherits(FVCreator, Transform);

var partialRow = null;
var count = 0;
var tags;

FVCreator.prototype._transform = function(chunk, enc, cb) {
	var that = this;
	var questions = chunk.toString().split('\n');

	if (partialRow != null) {
		questions[0] = partialRow + questions[0];
		partialRow = null;
	}

	var lastRow = questions[questions.length - 1];
	if (lastRow.charAt(lastRow.length - 1) != '\n') {
		partialRow = lastRow;
		questions.splice(questions.length - 1, 1);
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
				that.push("1,", "utf8");
			} else {
				that.push("0,", "utf8");
			}
		});
	});

	this.push("\n", "utf8");
	cb();
};

fs.readFile('/Users/mirzasikander/Dropbox/school/CSCI 599/Data Files/TagsGreaterThan10.csv', {
	encoding: "utf8"
}, function(err, data) {
	if (err) throw err;

	//Make an array containing tags.
	tags = data.split('\n');

	//write to a file.
	var fvfileStream = fs.createWriteStream('/Users/mirzasikander/Desktop/TagFeatureVectors.csv');

	//read in the question posts
	var qfileStream = fs.createReadStream('/Users/mirzasikander/Dropbox/school/CSCI 599/Data Files/QuestionsWithTags.csv', {
		encoding: "utf8"
	});

	var fvc = new FVCreator();

	qfileStream.pipe(fvc).pipe(fvfileStream);
});