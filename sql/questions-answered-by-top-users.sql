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
	,group_concat(answersbyusers.userid SEPARATOR ';') as gurus
	,group_concat(answersbyusers.Body SEPARATOR ';') as question_answers
INTO OUTFILE '/Users/mirzasikander/data_files_2/questions-answered-by-top-10.csv' FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n'
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
			SELECT users.id AS userid
				,Count(posts.id) AS postscount
			FROM posts
				,users
			WHERE posts.owneruserid = users.id
				AND posts.posttypeid = 2
			GROUP BY posts.owneruserid
			ORDER BY postscount DESC
			LIMIT 10
			) AS userswithanswers ON userswithanswers.userid = answers.owneruserid
	) AS answersbyusers ON questions.id = answersbyusers.parentid
Group by questions.id;