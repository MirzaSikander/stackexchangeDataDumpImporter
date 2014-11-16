SELECT questions.id
	,questions.Title
	,questions.Body
	,questions.tags
	,questions.score
	,questions.FavoriteCount
	,questions.ViewCount
	,questions.CreationDate
	,questions.LastActivityDate
	,questions.LastEditDate
	,group_concat(answersbyusersatleast100.userid SEPARATOR ',') as gurus
	,group_concat(answersbyusersatleast100.Body SEPARATOR ' |\| ') as question_answers
INTO OUTFILE '/Users/mirzasikander/data_files/questions-answered-by-users-with-atleast-100.csv' FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n'
FROM (
	SELECT *
	FROM posts
	WHERE posttypeid = 1
	) AS questions
INNER JOIN (
	SELECT *
	FROM (
		SELECT *
		FROM posts
		WHERE posttypeid = 2
		) AS answers
	INNER JOIN (
		SELECT userid
		FROM (
			SELECT users.id AS userid
				,Count(posts.id) AS postscount
			FROM posts
				,users
			WHERE posts.owneruserid = users.id
				AND posts.posttypeid = 2
			GROUP BY posts.owneruserid
			) AS userswithanswers
		WHERE postscount > 100
		) AS usersWithAtleast100Answers ON usersWithAtleast100Answers.userid = answers.owneruserid
	) AS answersbyusersatleast100 ON questions.id = answersbyusersatleast100.parentid
Group by questions.id;